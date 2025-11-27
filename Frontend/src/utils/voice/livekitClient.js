// Minimal LiveKit voice client wrapper for Gomoku
// Usage:
//   const voice = createLivekitVoice({ roomId, playerId, onRemoteTrack });
//   await voice.connect();
//   await voice.setMic(true/false);
//   voice.disconnect();

import { Room, RoomEvent, createLocalAudioTrack, DataPacket_Kind } from "livekit-client";
import { api } from "../../api/client";

export function createLivekitVoice(options) {
  const { roomId, playerId, onRemoteTrack, onChat, onEmote, onConnected, onDisconnected, onMicChanged } = options || {};
  if (!roomId || !playerId) throw new Error("roomId and playerId required");

  let room = null;
  let micTrack = null;
  let connected = false;

  const attachRemote = (publication, participant) => {
    const track = publication?.track;
    if (track && track.attach) {
      // Attach to DOM-less audio (auto-play)
      try { track.attach(); } catch (_) {}
    }
    if (typeof onRemoteTrack === "function") {
      onRemoteTrack(publication, participant);
    }
  };

  return {
    async connect() {
      if (room) return;
      const res = await api.get(`/api/gomoku/voice/token?roomId=${encodeURIComponent(String(roomId))}&playerId=${encodeURIComponent(String(playerId))}`);
      const payload = res?.data || res;
      const url = payload?.url;
      const token = payload?.token;
      if (!url || !token) throw new Error("Missing LiveKit url/token from backend");

      room = new Room();
      room.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
        attachRemote(publication, participant);
      });
      room.on(RoomEvent.TrackPublished, (publication, participant) => {
        // auto-subscribe to audio
        if (publication.kind === "audio") {
          participant?.subscribe?.(publication?.trackSid);
        }
      });
      room.on(RoomEvent.DataReceived, (payload, participant, kind) => {
        try {
          const text = new TextDecoder().decode(payload);
          const data = JSON.parse(text);
          if (data?.type === "chat" && typeof onChat === "function") {
            onChat({ senderId: participant?.identity || "", message: data.message, sentAt: Date.now() });
          } else if (data?.type === "emote" && typeof onEmote === "function") {
            onEmote(data.emote);
          }
        } catch (_) {}
      });
      room.on(RoomEvent.Disconnected, () => {
        connected = false;
        if (typeof onDisconnected === "function") onDisconnected();
      });
      await room.connect(url, token);
      connected = true;
      if (typeof onConnected === "function") onConnected();
    },
    async setMic(on) {
      if (!room) throw new Error("voice not connected");
      if (on) {
        if (!micTrack) {
          micTrack = await createLocalAudioTrack();
        }
        await room.localParticipant.publishTrack(micTrack);
        if (typeof onMicChanged === "function") onMicChanged(true);
      } else {
        if (micTrack) {
          try { await room.localParticipant.unpublishTrack(micTrack); } catch (_) {}
          try { micTrack.stop(); } catch (_) {}
          micTrack = null;
        }
        if (typeof onMicChanged === "function") onMicChanged(false);
      }
    },
    async sendChat(message) {
      if (!room || !message) return;
      const payload = new TextEncoder().encode(JSON.stringify({ type: "chat", message: String(message) }));
      try { await room.localParticipant.publishData(payload, { reliable: true }); } catch (_) {}
    },
    async sendEmote(emote) {
      if (!room || !emote) return;
      const payload = new TextEncoder().encode(JSON.stringify({ type: "emote", emote }));
      try { await room.localParticipant.publishData(payload, { reliable: true }); } catch (_) {}
    },
    async disconnect() {
      try { if (micTrack) micTrack.stop(); } catch (_) {}
      micTrack = null;
      try { await room?.disconnect(); } catch (_) {}
      room = null;
      connected = false;
    }
  };
}

export default createLivekitVoice;
