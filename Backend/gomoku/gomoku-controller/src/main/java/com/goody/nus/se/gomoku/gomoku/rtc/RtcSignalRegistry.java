package com.goody.nus.se.gomoku.gomoku.rtc;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks active SSE emitters (subscribers) per room for WebRTC signaling
 * and provides simple presence metrics.
 */
public class RtcSignalRegistry {

    private static final long DEFAULT_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes

    private final Map<String, Map<String, SseEmitter>> rooms = new ConcurrentHashMap<>();

    public SseEmitter register(String roomId, String playerId) {
        rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        rooms.get(roomId).put(playerId, emitter);
        emitter.onCompletion(() -> unregister(roomId, playerId));
        emitter.onTimeout(() -> unregister(roomId, playerId));
        return emitter;
    }

    public void unregister(String roomId, String playerId) {
        Map<String, SseEmitter> map = rooms.get(roomId);
        if (map != null) {
            map.remove(playerId);
            if (map.isEmpty()) {
                rooms.remove(roomId);
            }
        }
    }

    public Optional<SseEmitter> getEmitter(String roomId, String playerId) {
        Map<String, SseEmitter> map = rooms.get(roomId);
        if (map == null) return Optional.empty();
        return Optional.ofNullable(map.get(playerId));
    }

    public Collection<SseEmitter> getRoomEmitters(String roomId) {
        Map<String, SseEmitter> map = rooms.get(roomId);
        return map != null ? new ArrayList<>(map.values()) : Collections.emptyList();
    }

    public Set<String> getRoomParticipants(String roomId) {
        Map<String, SseEmitter> map = rooms.get(roomId);
        return map != null ? new HashSet<>(map.keySet()) : Collections.emptySet();
    }

    public int getTotalConnections() {
        return rooms.values().stream().mapToInt(Map::size).sum();
    }
}

