import authUtils, {
  clearAuthData,
  getAuthData,
  isAuthenticated,
  login,
  logout,
  onAuthStateChange,
  saveAuthData,
} from "../auth";

jest.mock("../authState", () => ({
  emitAuthChange: jest.fn(),
}));

jest.mock("../../api", () => ({
  userApi: {
    login: jest.fn(),
    logout: jest.fn(),
  },
}));

const { emitAuthChange } = jest.requireMock("../authState");
const { userApi } = jest.requireMock("../../api");

describe("auth utilities", () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  it("saves auth data to localStorage", () => {
    saveAuthData({
      token: "abc",
      userId: 42,
      nickname: "gomoku",
      email: "player@example.com",
    });

    expect(localStorage.getItem("token")).toBe("abc");
    expect(localStorage.getItem("userId")).toBe("42");
    expect(localStorage.getItem("nickname")).toBe("gomoku");
    expect(localStorage.getItem("email")).toBe("player@example.com");
    expect(localStorage.getItem("loggedIn")).toBe("1");
    expect(emitAuthChange).toHaveBeenCalledTimes(1);
  });

  it("reads auth data from localStorage", () => {
    localStorage.setItem("token", "token-123");
    localStorage.setItem("userId", "99");
    localStorage.setItem("nickname", "tester");
    localStorage.setItem("email", "tester@example.com");
    localStorage.setItem("loggedIn", "1");

    expect(getAuthData()).toEqual({
      token: "token-123",
      userId: "99",
      nickname: "tester",
      email: "tester@example.com",
      loggedIn: true,
    });
  });

  it("handles saveAuthData with partial payloads", () => {
    saveAuthData({});
    expect(localStorage.getItem("token")).toBeNull();
    expect(localStorage.getItem("userId")).toBeNull();
    expect(localStorage.getItem("nickname")).toBeNull();
    expect(localStorage.getItem("email")).toBeNull();
    expect(localStorage.getItem("loggedIn")).toBe("1");
  });

  it("clears auth data", () => {
    saveAuthData({ token: "abc", userId: 1 });
    clearAuthData();

    expect(localStorage.getItem("token")).toBeNull();
    expect(localStorage.getItem("userId")).toBeNull();
    expect(localStorage.getItem("loggedIn")).toBeNull();
    expect(emitAuthChange).toHaveBeenCalledTimes(2); // save + clear
  });

  it("checks authentication status", () => {
    expect(isAuthenticated()).toBe(false);
    localStorage.setItem("token", "abc");
    localStorage.setItem("loggedIn", "1");
    expect(isAuthenticated()).toBe(true);
  });

  it("logs in successfully and stores data", async () => {
    userApi.login.mockResolvedValueOnce({
      data: { token: "new-token", userId: 7, nickname: "hero" },
    });

    const result = await login({ username: "user", password: "pass" });

    expect(userApi.login).toHaveBeenCalledWith({ username: "user", password: "pass" });
    expect(result).toEqual({ token: "new-token", userId: 7, nickname: "hero" });
    expect(localStorage.getItem("token")).toBe("new-token");
    expect(localStorage.getItem("userId")).toBe("7");
    expect(emitAuthChange).toHaveBeenCalledTimes(2); // clear + save
  });

  it("supports login responses without data wrapper", async () => {
    userApi.login.mockResolvedValueOnce({ token: "raw-token", userId: 55 });

    const result = await login({ username: "user", password: "pass" });

    expect(result).toEqual({ token: "raw-token", userId: 55 });
    expect(localStorage.getItem("token")).toBe("raw-token");
  });

  it("logs out even if API fails", async () => {
    userApi.logout.mockRejectedValueOnce(new Error("Network"));
    localStorage.setItem("token", "token");
    localStorage.setItem("loggedIn", "1");
    const consoleErrorSpy = jest.spyOn(console, "error").mockImplementation(() => {});

    await logout();

    expect(userApi.logout).toHaveBeenCalledTimes(1);
    expect(localStorage.getItem("token")).toBeNull();
    expect(localStorage.getItem("loggedIn")).toBeNull();
    expect(emitAuthChange).toHaveBeenCalledTimes(1);
    consoleErrorSpy.mockRestore();
  });

  it("subscribes to auth state changes and cleans up", () => {
    const callback = jest.fn();
    const cleanup = onAuthStateChange(callback);

    const authEvent = new Event("storage");
    Object.defineProperty(authEvent, "key", { value: "token" });
    window.dispatchEvent(authEvent);
    expect(callback).toHaveBeenCalledTimes(1);

    const ignoredEvent = new Event("storage");
    Object.defineProperty(ignoredEvent, "key", { value: "some-other-key" });
    window.dispatchEvent(ignoredEvent);
    expect(callback).toHaveBeenCalledTimes(1);

    cleanup();

    const postCleanupEvent = new Event("storage");
    Object.defineProperty(postCleanupEvent, "key", { value: "userId" });
    window.dispatchEvent(postCleanupEvent);
    expect(callback).toHaveBeenCalledTimes(1);
  });

  it("exposes auth keys via default export", () => {
    expect(authUtils.AUTH_KEYS).toMatchObject({
      TOKEN: "token",
      USER_ID: "userId",
      LOGGED_IN: "loggedIn",
    });
  });
});
