const AUTH_EVENT = "gomoku-auth-changed";

export function emitAuthChange() {
  if (typeof window === "undefined") return;
  window.dispatchEvent(new Event(AUTH_EVENT));
}

export function addAuthChangeListener(listener) {
  if (typeof window === "undefined" || typeof listener !== "function") {
    return () => {};
  }
  window.addEventListener(AUTH_EVENT, listener);
  return () => window.removeEventListener(AUTH_EVENT, listener);
}

export const AUTH_EVENT_NAME = AUTH_EVENT;
