import {
  emitAuthChange,
  addAuthChangeListener,
  AUTH_EVENT_NAME,
} from "../authState";

describe("authState helpers", () => {
  beforeEach(() => {
    jest.restoreAllMocks();
  });

  it("emits auth change event", () => {
    const handler = jest.fn();
    window.addEventListener(AUTH_EVENT_NAME, handler);

    emitAuthChange();
    expect(handler).toHaveBeenCalledTimes(1);

    window.removeEventListener(AUTH_EVENT_NAME, handler);
  });

  it("registers and cleans up listener", () => {
    const listener = jest.fn();
    const cleanup = addAuthChangeListener(listener);

    emitAuthChange();
    expect(listener).toHaveBeenCalledTimes(1);

    cleanup();
    emitAuthChange();
    expect(listener).toHaveBeenCalledTimes(1);
  });

  it("returns noop cleanup for invalid listener", () => {
    const cleanup = addAuthChangeListener(null);
    expect(typeof cleanup).toBe("function");
    expect(() => cleanup()).not.toThrow();
  });

  it("handles environments without window", () => {
    const originalWindow = global.window;
    // Some environments may forbid deleting window; fallback to assignment.
    try {
      // eslint-disable-next-line no-undef
      delete global.window;
    } catch (err) {
      global.window = undefined;
    }

    expect(() => emitAuthChange()).not.toThrow();
    const cleanup = addAuthChangeListener(() => {});
    expect(typeof cleanup).toBe("function");
    cleanup();

    global.window = originalWindow;
  });
});
