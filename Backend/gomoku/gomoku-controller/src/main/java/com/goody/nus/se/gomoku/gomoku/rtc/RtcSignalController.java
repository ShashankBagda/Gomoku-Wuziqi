package com.goody.nus.se.gomoku.gomoku.rtc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

/**
 * REST + SSE signaling for WebRTC.
 *
 * Endpoints live under the gomoku service context-path (e.g. /api/gomoku/rtc/...).
 */
@Slf4j
@RestController
@RequestMapping("/rtc")
@RequiredArgsConstructor
public class RtcSignalController {

    private final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private final RtcSignalRegistry registry = new RtcSignalRegistry();

    /**
     * Subscribe to receive signaling events for a given room/player via SSE.
     * A token query param may be provided for gateway auth compatibility; it is not processed here.
     */
    @GetMapping(path = "/signal/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam String roomId,
                                @RequestParam String playerId,
                                @RequestParam(required = false) String token) throws IOException {
        SseEmitter emitter = registry.register(roomId, playerId);
        // Send initial welcome with current participants
        SignalMessage welcome = SignalMessage.welcome(roomId, playerId, registry.getRoomParticipants(roomId));
        trySend(emitter, welcome);
        // Broadcast presence join to others
        broadcast(roomId, SignalMessage.presence(roomId, playerId, "join"), Set.of(playerId));
        return emitter;
    }

    /**
     * Unified signaling endpoint. The payload is forwarded to target participant or broadcast to others.
     */
    @PostMapping(path = "/signal", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<String> signal(@RequestBody SignalMessage message) {
        if (message == null || isBlank(message.getRoomId()) || isBlank(message.getSenderId()) || isBlank(message.getType())) {
            return ApiResult.failed(ErrorCodeEnum.BAD_REQUEST);
        }
        String roomId = message.getRoomId();
        String senderId = message.getSenderId();
        String targetId = message.getTargetId();
        log.debug("Signal: type={}, room={}, from={}, to={}", message.getType(), roomId, senderId, targetId);

        // Presence events are generated server-side; ignore if posted
        if (Objects.equals(message.getType(), "presence")) {
            return ApiResult.success("ignored");
        }

        if (isBlank(targetId)) {
            broadcast(roomId, message, Set.of(senderId));
        } else {
            registry.getEmitter(roomId, targetId).ifPresent(em -> trySend(em, message));
        }
        return ApiResult.success("ok");
    }

    /**
     * Presence and cleanup hooks.
     */
    @DeleteMapping("/signal/leave")
    public ApiResult<String> leave(@RequestParam String roomId, @RequestParam String playerId) {
        registry.unregister(roomId, playerId);
        broadcast(roomId, SignalMessage.presence(roomId, playerId, "leave"), Collections.emptySet());
        return ApiResult.success("ok");
    }

    @GetMapping("/online-count")
    public ApiResult<Integer> onlineCount() {
        return ApiResult.success(registry.getTotalConnections());
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------
    private void broadcast(String roomId, SignalMessage message, Set<String> exclude) {
        for (String pid : registry.getRoomParticipants(roomId)) {
            if (exclude != null && exclude.contains(pid)) continue;
            registry.getEmitter(roomId, pid).ifPresent(em -> trySend(em, message));
        }
    }

    private void trySend(SseEmitter emitter, SignalMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            emitter.send(SseEmitter.event().name("signal").data(json));
        } catch (IOException e) {
            // The emitter may be closed; nothing to do
            log.debug("Failed to send SSE: {}", e.getMessage());
        }
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }

    @Data
    public static class SignalMessage {
        private String type;      // offer|answer|candidate|chat|emote|presence|welcome
        private String roomId;
        private String senderId;
        private String targetId;  // optional
        private Object payload;   // SDP/candidate or app payload

        public static SignalMessage presence(String roomId, String playerId, String action) {
            SignalMessage m = new SignalMessage();
            m.setType("presence");
            m.setRoomId(roomId);
            m.setSenderId(playerId);
            Map<String, Object> p = new HashMap<>();
            p.put("action", action);
            m.setPayload(p);
            return m;
        }

        public static SignalMessage welcome(String roomId, String playerId, Collection<String> participants) {
            SignalMessage m = new SignalMessage();
            m.setType("welcome");
            m.setRoomId(roomId);
            m.setSenderId(playerId);
            Map<String, Object> p = new HashMap<>();
            p.put("participants", participants);
            m.setPayload(p);
            return m;
        }
    }
}
