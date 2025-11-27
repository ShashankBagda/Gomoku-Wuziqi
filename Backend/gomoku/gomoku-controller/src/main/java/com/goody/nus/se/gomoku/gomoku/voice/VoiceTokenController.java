package com.goody.nus.se.gomoku.gomoku.voice;

import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Issues LiveKit access tokens for joining voice rooms.
 *
 * Reads credentials from env or application config:
 *  - LIVEKIT_URL: ws(s) URL to LiveKit server (e.g. wss://your.livekit.cloud)
 *  - LIVEKIT_API_KEY: API key (will be set as JWT header kid and issuer)
 *  - LIVEKIT_API_SECRET: API secret (HMAC-SHA256 signing key)
 */
@RestController
@RequestMapping("/voice")
public class VoiceTokenController {

    // Hardcoded defaults for test environment when CI/CD env vars are not set.
    // NOTE: For production, move these to protected environment variables.
    private static final String DEFAULT_LIVEKIT_URL = "wss://gomoku-exmm9iyu.livekit.cloud";
    private static final String DEFAULT_LIVEKIT_API_KEY = "APIyBhBbiCsmcXK";
    private static final String DEFAULT_LIVEKIT_API_SECRET = "0xM8qGRb0yuKWnqN5knujwjwYyIDJ3nBopyPzlaAC2K";

    @Value("${livekit.url:}")
    private String livekitUrlProp;

    @Value("${livekit.apiKey:}")
    private String apiKeyProp;

    @Value("${livekit.apiSecret:}")
    private String apiSecretProp;

    @GetMapping("/token")
    public ApiResult<TokenResponse> token(@RequestParam String roomId,
                                          @RequestParam String playerId) {
        String url = coalesce(System.getenv("LIVEKIT_URL"), livekitUrlProp, DEFAULT_LIVEKIT_URL);
        String apiKey = coalesce(System.getenv("LIVEKIT_API_KEY"), apiKeyProp, DEFAULT_LIVEKIT_API_KEY);
        String apiSecret = coalesce(System.getenv("LIVEKIT_API_SECRET"), apiSecretProp, DEFAULT_LIVEKIT_API_SECRET);

        if (isBlank(url) || isBlank(apiKey) || isBlank(apiSecret)) {
            return ApiResult.failed(ErrorCodeEnum.BAD_REQUEST.getErrorCode(),
                    "LIVEKIT configuration missing: set LIVEKIT_URL, LIVEKIT_API_KEY, LIVEKIT_API_SECRET");
        }

        // Build LiveKit-style JWT: header kid=apiKey, HS256 signed with apiSecret, with video grants
        Map<String, Object> video = new HashMap<>();
        video.put("room", roomId);
        video.put("roomJoin", true);
        video.put("canPublish", true);
        video.put("canSubscribe", true);
        video.put("canPublishData", true);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(3600);

        SecretKey key = Keys.hmacShaKeyFor(apiSecret.getBytes(StandardCharsets.UTF_8));
        String jwt = Jwts.builder()
                .setHeaderParam("kid", apiKey)
                .setIssuer(apiKey)
                .setSubject(playerId)
                .claim("name", playerId)
                .claim("video", video)
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        TokenResponse resp = new TokenResponse();
        resp.setUrl(url);
        resp.setToken(jwt);
        return ApiResult.success(resp);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private static String coalesce(String a, String b) { return !isBlank(a) ? a : b; }
    private static String coalesce(String a, String b, String c) { return !isBlank(a) ? a : (!isBlank(b) ? b : c); }

    @Data
    public static class TokenResponse {
        private String url;
        private String token;
    }
}
