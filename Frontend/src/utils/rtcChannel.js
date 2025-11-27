import { api } from "../api/client";
import { API_BASE_URL } from "../api/client";

function buildSubscribeUrl(baseUrl, roomId, playerId, token) {
  try {
    const url = new URL("/api/gomoku/rtc/signal/subscribe", baseUrl);
    url.searchParams.set("roomId", String(roomId));
    url.searchParams.set("playerId", String(playerId));
    if (token) url.searchParams.set("token", token);
    return url.toString();
  } catch (_e) {
    return undefined;
  }
}

export function createRtcChannel(options) {
  const {
    roomId,
    playerId,
    token,
    onChat,
    onEmote,
    onPresence,
    onRemoteAudio,
  } = options || {};

  if (!roomId || !playerId) throw new Error("roomId and playerId required");

  const pc = new RTCPeerConnection({
    iceServers: [{ urls: ["stun:stun.l.google.com:19302"] }],
  });
  let iceConfigLoaded = false;
  const loadIceConfig = async () => {
    try {
      const res = await api.get(`/api/gomoku/rtc/ice-config?playerId=${encodeURIComponent(String(playerId))}`);
      const iceServers = res?.data?.iceServers || res?.iceServers || [];
      if (Array.isArray(iceServers) && iceServers.length) {
        pc.setConfiguration({ iceServers });
        iceConfigLoaded = true;
      }
    } catch (_e) {}
  };
  const iceConfigPromise = loadIceConfig();

  let chatChannel = null;
  let micStream = null;
  let closed = false;
  let remotePlayerId = null;
  const myId = String(playerId);

  const subscribeUrl = buildSubscribeUrl(API_BASE_URL, roomId, playerId, token);
  let es = null;
  let esBackoff = 1000;
  const connectEventSource = () => {
    if (!subscribeUrl || typeof EventSource === "undefined") return;
    es = new EventSource(subscribeUrl, { withCredentials: true });
    es.addEventListener("signal", (ev) => {
      try { handleSignal(JSON.parse(ev.data)); } catch (_e) {}
    });
    es.onerror = () => {
      try { es?.close(); } catch (_) {}
      if (!closed) {
        const wait = esBackoff; esBackoff = Math.min(esBackoff * 2, 10000);
        setTimeout(connectEventSource, wait);
      }
    };
  };
  connectEventSource();

  const postSignal = async (type, payload, targetId) => {
    try {
      await api.post("/api/gomoku/rtc/signal", {
        type,
        roomId,
        senderId: String(playerId),
        targetId: targetId || remotePlayerId || undefined,
        payload,
      });
    } catch (e) {
      // ignore
    }
  };

  const ensureChat = () => {
    if (chatChannel && chatChannel.readyState === "open") return;
    chatChannel = pc.createDataChannel("chat", { ordered: true });
    chatChannel.onmessage = (ev) => {
      try {
        const data = JSON.parse(ev.data);
        if (data?.type === "chat" && typeof onChat === "function") {
          onChat({ senderId: remotePlayerId, message: data.message, sentAt: Date.now() });
        } else if (data?.type === "emote" && typeof onEmote === "function") {
          onEmote(data.emote);
        }
      } catch (_e) {
        // ignore
      }
    };
  };

  pc.ondatachannel = (ev) => {
    const channel = ev.channel;
    if (channel.label === "chat") {
      chatChannel = channel;
      channel.onmessage = (e) => {
        try {
          const data = JSON.parse(e.data);
          if (data?.type === "chat" && typeof onChat === "function") {
            onChat({ senderId: remotePlayerId, message: data.message, sentAt: Date.now() });
          } else if (data?.type === "emote" && typeof onEmote === "function") {
            onEmote(data.emote);
          }
        } catch (_e) {}
      };
    }
  };

  pc.onicecandidate = (ev) => {
    if (ev.candidate) postSignal("candidate", ev.candidate);
  };

  pc.ontrack = (ev) => {
    if (typeof onRemoteAudio === "function" && ev.streams && ev.streams[0]) {
      onRemoteAudio(ev.streams[0]);
    }
  };

  const handleSignal = async (msg) => {
    const { type, senderId, payload } = msg;
    if (String(senderId) === myId) return;
    if (type === "offer") {
      remotePlayerId = String(senderId);
      await pc.setRemoteDescription(payload);
      const answer = await pc.createAnswer();
      await pc.setLocalDescription(answer);
      await postSignal("answer", pc.localDescription, senderId);
    } else if (type === "answer") {
      remotePlayerId = String(senderId);
      await pc.setRemoteDescription(payload);
    } else if (type === "candidate") {
      try { await pc.addIceCandidate(payload); } catch (_e) {}
    } else if (type === "presence") {
      if (payload?.action === "join" && !remotePlayerId) {
        remotePlayerId = String(senderId);
        await createOfferIfNeeded();
      }
      if (typeof onPresence === "function") onPresence(payload);
    } else if (type === "welcome") {
      // When joining second, pick an existing participant as remote and start negotiation
      const participants = Array.isArray(payload?.participants) ? payload.participants.map((p) => String(p)) : [];
      const candidate = participants.find((p) => p && p !== myId);
      if (candidate && !remotePlayerId) {
        remotePlayerId = candidate;
        await createOfferIfNeeded();
      }
    }
  };

  // listeners wired by connectEventSource

  const createOfferIfNeeded = async () => {
    if (!remotePlayerId) return; // wait until we know who to call
    await iceConfigPromise; // ensure ICE config before initial offer
    if (pc.signalingState === "stable" && (!pc.localDescription || pc.localDescription.type !== "offer")) {
      ensureChat();
      const offer = await pc.createOffer();
      await pc.setLocalDescription(offer);
      await postSignal("offer", pc.localDescription, remotePlayerId);
    }
  };

  pc.oniceconnectionstatechange = async () => {
    const state = pc.iceConnectionState;
    if (state === "failed" || state === "disconnected") {
      try {
        await iceConfigPromise;
        const offer = await pc.createOffer({ iceRestart: true });
        await pc.setLocalDescription(offer);
        await postSignal("offer", pc.localDescription, remotePlayerId);
      } catch (_e) {}
    }
  };

  return {
    async sendChat(message) {
      if (!message) return;
      ensureChat();
      if (chatChannel?.readyState !== "open") await createOfferIfNeeded();
      try { chatChannel?.send(JSON.stringify({ type: "chat", message: String(message) })); } catch (_e) {}
    },
    async sendEmote(emote) {
      if (!emote) return;
      ensureChat();
      if (chatChannel?.readyState !== "open") await createOfferIfNeeded();
      try { chatChannel?.send(JSON.stringify({ type: "emote", emote })); } catch (_e) {}
    },
    async setMic(on) {
      if (on) {
        try {
          if (!micStream) {
            micStream = await navigator.mediaDevices.getUserMedia({ audio: true });
            micStream.getTracks().forEach((t) => pc.addTrack(t, micStream));
          }
          await createOfferIfNeeded();
        } catch (_e) {}
      } else {
        if (micStream) {
          micStream.getTracks().forEach((t) => t.stop());
          micStream = null;
        }
        // Removing tracks is optional; peer will stop receiving when tracks stop
      }
    },
    close() {
      closed = true;
      try { es?.close(); } catch (_) {}
      try { chatChannel?.close(); } catch (_) {}
      try { pc.close(); } catch (_) {}
      if (micStream) {
        micStream.getTracks().forEach((t) => t.stop());
        micStream = null;
      }
      // best-effort leave notification (fire-and-forget)
      Promise.resolve(
        api.post("/api/gomoku/rtc/signal/leave", null, { method: "DELETE" })
      ).catch(() => {})
    },
  };
}

export default createRtcChannel;
