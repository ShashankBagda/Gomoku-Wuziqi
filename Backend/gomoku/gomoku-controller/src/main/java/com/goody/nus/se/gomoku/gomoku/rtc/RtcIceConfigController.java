package com.goody.nus.se.gomoku.gomoku.rtc;

import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Provides ICE server configuration for WebRTC.
 * Supports TURN REST credentials generation using a shared secret.
 */
@RestController
@RequestMapping("/rtc")
public class RtcIceConfigController {

    @Value("${rtc.turn.host:}")
    private String turnHostProp;

    @Value("${rtc.turn.secret:}")
    private String turnSecretProp;

    @Value("${rtc.turn.ttl:3600}")
    private long ttlSecondsProp;

    @GetMapping("/ice-config")
    public ApiResult<Map<String, Object>> iceConfig(@RequestParam(required = false) String playerId) {
        // Prefer environment variables if present
        String turnHost = Optional.ofNullable(System.getenv("TURN_HOST")).orElse(turnHostProp);
        String turnSecret = Optional.ofNullable(System.getenv("TURN_SECRET")).orElse(turnSecretProp);
        long ttlSeconds = Optional.ofNullable(System.getenv("TURN_TTL")).map(Long::parseLong).orElse(ttlSecondsProp);

        List<Map<String, Object>> servers = new ArrayList<>();
        Map<String, Object> stun = new HashMap<>();
        stun.put("urls", Collections.singletonList("stun:stun.l.google.com:19302"));
        servers.add(stun);

        if (turnHost != null && !turnHost.isBlank()) {
            Map<String, Object> turn = new HashMap<>();
            List<String> urls = new ArrayList<>();
            urls.add("turn:" + turnHost + "?transport=udp");
            urls.add("turn:" + turnHost + "?transport=tcp");
            turn.put("urls", urls);

            if (turnSecret != null && !turnSecret.isBlank()) {
                // TURN REST: username = expiry:playerId, credential = HMAC-SHA1(secret, username)
                long expiry = (System.currentTimeMillis() / 1000L) + Math.max(60, ttlSeconds);
                String userPart = Optional.ofNullable(playerId).orElse("anon");
                String username = expiry + ":" + userPart;
                String credential = hmacSha1Base64(turnSecret, username);
                turn.put("username", username);
                turn.put("credential", credential);
            }
            servers.add(turn);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("iceServers", servers);
        return ApiResult.success(payload);
    }

    private static String hmacSha1Base64(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            return "";
        }
    }
}

