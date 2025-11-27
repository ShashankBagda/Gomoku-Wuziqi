package com.goody.nus.se.gomoku.gateway.filter;

import com.goody.nus.se.gomoku.user.api.response.UserVerifyResponse;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Authentication filter to verify JWT token and add user info to request headers
 *
 * @author Goody
 * @version 1.0, 2025/10/8
 */
@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<Object> {

    private static final ParameterizedTypeReference<ApiResult<UserVerifyResponse>> VERIFY_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private final WebClient webClient;
    @Value("${user-service.verify-url}")
    private String verifyUrl;

    public AuthenticationFilter() {
        super(Object.class);
        this.webClient = WebClient.builder().build();
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            // Support token via query param for SSE/EventSource where custom headers are not available
            if (authHeader == null || authHeader.isEmpty()) {
                String path = exchange.getRequest().getPath().value();
                String token = exchange.getRequest().getQueryParams().getFirst("token");
                if (token != null && !token.isBlank() && (path != null && path.contains("/api/gomoku/rtc/signal/subscribe"))) {
                    authHeader = "Bearer " + token;
                }
            }

            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("Missing Authorization (header or token query)");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return webClient.get()
                    .uri(verifyUrl)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(VERIFY_RESPONSE_TYPE)
                    .flatMap(result -> {
                        if (!result.isSuccess() || result.getData() == null) {
                            log.warn("Verify endpoint returned unsuccessful result or null data: {}", result.getErrorMsg());
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        UserVerifyResponse verifyResponse = result.getData();

                        if (!Boolean.TRUE.equals(verifyResponse.getValid())) {
                            log.warn("Invalid token");
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        var modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", String.valueOf(verifyResponse.getUserId()))
                                .header("X-User-Email", verifyResponse.getEmail())
                                .header("X-User-Nickname", verifyResponse.getNickname())
                                .build();

                        log.debug("User authenticated: userId={}, email={}, nickname={}",
                                verifyResponse.getUserId(), verifyResponse.getEmail(), verifyResponse.getNickname());

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(error -> {
                        log.error("Error calling verify endpoint", error);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }
}
