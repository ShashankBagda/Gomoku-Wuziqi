/**
 * Tests for rtcChannel.js
 */

// Use real timers; async handlers rely on microtasks/timeouts
jest.setTimeout(10000);

// Mock api client module used by rtcChannel
jest.mock("../../api/client", () => {
  return {
    api: { post: jest.fn(() => Promise.resolve({})) },
    API_BASE_URL: "http://api.test",
  };
});

const { api } = require("../../api/client");

// Minimal fake implementations for browser APIs
class FakeDataChannel {
  constructor(label) {
    this.label = label;
    this.readyState = "open";
    this.onmessage = null;
    this.close = jest.fn();
    this.send = jest.fn();
  }
}

class FakeRTCPeerConnection {

  constructor() {
    this.ondatachannel = null;
    this.onicecandidate = null;
    this.ontrack = null;
    this.localDescription = null;
    this.signalingState = "stable";
    this.createOffer = jest.fn(async () => ({ type: "offer", sdp: "offer-sdp" }));
    this.createAnswer = jest.fn(async () => ({ type: "answer", sdp: "answer-sdp" }));
    this.setLocalDescription = jest.fn(async (desc) => { this.localDescription = desc; });
    this.setRemoteDescription = jest.fn(async () => {});
    this.addIceCandidate = jest.fn(async () => {});
    this.addTrack = jest.fn();
    this.close = jest.fn();
    FakeRTCPeerConnection.instances.push(this);
  }

  createDataChannel = jest.fn((label) => {
    this._dc = new FakeDataChannel(label);
    return this._dc;
  });
}
FakeRTCPeerConnection.instances = [];

class FakeEventSource {
  constructor(url, opts) {
    this.url = url;
    this.opts = opts;
    this._listeners = {};
    this.close = jest.fn();
    FakeEventSource.instances.push(this);
  }
  addEventListener(name, cb) {
    this._listeners[name] = cb;
  }
  dispatch(name, data) {
    const cb = this._listeners[name];
    if (cb) cb({ data: JSON.stringify(data) });
  }
}
FakeEventSource.instances = [];

// Attach fakes to global
beforeAll(() => {
  global.RTCPeerConnection = FakeRTCPeerConnection;
  global.EventSource = FakeEventSource;
});

beforeEach(() => {
  const track = { stop: jest.fn() };
  const stream = { getTracks: () => [track] };
  global.__testTrack = track;
  if (!global.navigator) global.navigator = {};
  global.navigator.mediaDevices = {
    getUserMedia: jest.fn(async () => stream),
  };
});

afterEach(() => {
  jest.clearAllMocks();
});

describe("rtcChannel", () => {
  test("throws if required params missing", () => {
    const { createRtcChannel } = require("../rtcChannel");
    expect(() => createRtcChannel({ roomId: null, playerId: "1" })).toThrow();
    expect(() => createRtcChannel({ roomId: "r", playerId: null })).toThrow();
  });

  test("subscribes via EventSource with proper URL", () => {
    const { createRtcChannel } = require("../rtcChannel");
    createRtcChannel({ roomId: "room1", playerId: "me", token: "tkn" });
    const es = FakeEventSource.instances[FakeEventSource.instances.length - 1];
    expect(es).toBeDefined();
    expect(es.url).toBe("http://api.test/api/gomoku/rtc/signal/subscribe?roomId=room1&playerId=me&token=tkn");
  });

  test("sendChat sends data over DataChannel", async () => {
    const { createRtcChannel } = require("../rtcChannel");
    const onChat = jest.fn();
    const channel = createRtcChannel({ roomId: "room1", playerId: "me", onChat });
    await channel.sendChat("Hello");
    const pc = FakeRTCPeerConnection.instances[FakeRTCPeerConnection.instances.length - 1];
    expect(pc._dc).toBeDefined();
    expect(pc._dc.send).toHaveBeenCalledTimes(1);
    const payload = JSON.parse(pc._dc.send.mock.calls[0][0]);
    expect(payload).toEqual({ type: "chat", message: "Hello" });
    // Second send should reuse same channel, not recreate
    const createCalls = pc.createDataChannel.mock.calls.length;
    await channel.sendChat("Again");
    expect(pc.createDataChannel.mock.calls.length).toBe(createCalls);
  });

  test("datachannel onmessage dispatches chat and emote", () => {
    const { createRtcChannel } = require("../rtcChannel");
    const onChat = jest.fn();
    const onEmote = jest.fn();
    createRtcChannel({ roomId: "room1", playerId: "me", onChat, onEmote });
    const pc = FakeRTCPeerConnection.instances[FakeRTCPeerConnection.instances.length - 1];
    const remoteChannel = new FakeDataChannel("chat");
    pc.ondatachannel({ channel: remoteChannel });
    remoteChannel.onmessage({ data: JSON.stringify({ type: "chat", message: "hi" }) });
    remoteChannel.onmessage({ data: JSON.stringify({ type: "emote", emote: "😀" }) });
    expect(onChat).toHaveBeenCalled();
    expect(onEmote).toHaveBeenCalledWith("😀");
  });

  test("handles offer by replying with answer via api.post", async () => {
    const { createRtcChannel } = require("../rtcChannel");
    createRtcChannel({ roomId: "r1", playerId: "me" });
    const es = FakeEventSource.instances[FakeEventSource.instances.length - 1];
    // Simulate incoming offer from other peer
    es.dispatch("signal", { type: "offer", senderId: "other", payload: { type: "offer", sdp: "sdp" } });
    // allow async handlers to resolve
    await new Promise((r) => setImmediate(r));
    const pc = FakeRTCPeerConnection.instances[FakeRTCPeerConnection.instances.length - 1];
    expect(pc.setRemoteDescription).toHaveBeenCalled();
    expect(pc.createAnswer).toHaveBeenCalled();
    expect(pc.setLocalDescription).toHaveBeenCalled();
    // Answer posted
    expect(api.post).toHaveBeenCalledWith("/api/gomoku/rtc/signal", expect.objectContaining({ type: "answer" }));
  });

  test("handles answer and candidate, emits candidate on ice event", async () => {
    const { createRtcChannel } = require("../rtcChannel");
    createRtcChannel({ roomId: "r1", playerId: "me" });
    const pc = FakeRTCPeerConnection.instances[FakeRTCPeerConnection.instances.length - 1];
    const es = FakeEventSource.instances[FakeEventSource.instances.length - 1];
    // Answer from other peer
    es.dispatch("signal", { type: "answer", senderId: "other", payload: { type: "answer", sdp: "asdp" } });
    await new Promise((r) => setImmediate(r));
    expect(pc.setRemoteDescription).toHaveBeenCalled();
    // Candidate from other peer
    es.dispatch("signal", { type: "candidate", senderId: "other", payload: { candidate: "cand" } });
    await new Promise((r) => setImmediate(r));
    expect(pc.addIceCandidate).toHaveBeenCalled();
    // Local ICE candidate event
    pc.onicecandidate({ candidate: { candidate: "lcand" } });
    await new Promise((r) => setImmediate(r));
    expect(api.post).toHaveBeenCalledWith("/api/gomoku/rtc/signal", expect.objectContaining({ type: "candidate" }));
  });

  test("setMic on/off manages tracks and may create offer if remote known", async () => {
    const { createRtcChannel } = require("../rtcChannel");
    const ch = createRtcChannel({ roomId: "r1", playerId: "me" });
    const pc = FakeRTCPeerConnection.instances.at(-1);
    await ch.setMic(true);
    expect(navigator.mediaDevices.getUserMedia).toHaveBeenCalled();
    expect(pc.addTrack).toHaveBeenCalled();
    // Turn off
    await ch.setMic(false);
    expect(global.__testTrack.stop).toHaveBeenCalled();
  });

  test("sendEmote sends emote payload and ontrack forwards remote audio", async () => {
    const { createRtcChannel } = require("../rtcChannel");
    const onRemoteAudio = jest.fn();
    const ch = createRtcChannel({ roomId: "r1", playerId: "me", onRemoteAudio });
    const pc = FakeRTCPeerConnection.instances[FakeRTCPeerConnection.instances.length - 1];
    // ensure DC created and open
    await ch.sendEmote("😀");
    const payload = JSON.parse(pc._dc.send.mock.calls.pop()[0]);
    expect(payload).toEqual({ type: "emote", emote: "😀" });
    // ontrack
    const stream = { id: "remote", getTracks: () => [] };
    pc.ontrack({ streams: [stream] });
    expect(onRemoteAudio).toHaveBeenCalledWith(stream);
  });

  test("presence event triggers onPresence callback", async () => {
    const { createRtcChannel } = require("../rtcChannel");
    const onPresence = jest.fn();
    createRtcChannel({ roomId: "r1", playerId: "me", onPresence });
    const es = FakeEventSource.instances[FakeEventSource.instances.length - 1];
    es.dispatch("signal", { type: "presence", senderId: "other", payload: { action: "join" } });
    await new Promise((r) => setImmediate(r));
    expect(onPresence).toHaveBeenCalledWith({ action: "join" });
  });

  test("close tears down and sends leave", async () => {
    const { createRtcChannel } = require("../rtcChannel");
    const ch = createRtcChannel({ roomId: "r1", playerId: "me" });
    const pc = FakeRTCPeerConnection.instances.at(-1);
    const es = FakeEventSource.instances.at(-1);
    // Ensure a data channel exists to be closed
    await ch.sendChat("bye");
    ch.close();
    expect(es.close).toHaveBeenCalled();
    expect(pc.close).toHaveBeenCalled();
    expect(api.post).toHaveBeenCalledWith("/api/gomoku/rtc/signal/leave", null, expect.any(Object));
  });
});
