/**
 * API Client with automatic authentication handling
 *
 * Features:
 * - Automatic JWT token injection
 * - Global 401 response interceptor (auto-clears auth state)
 * - Unified error handling
 */

// API base URL based on environment
const getApiBase = () => {
  const env = process.env.REACT_APP_ENV;

  switch (env) {
    case 'development':
      return 'http://127.0.0.1:8083';
    case 'test':
      return 'https://test-api-gomoku.goodyhao.me';
    case 'production':
      return 'https://api-gomoku.goodyhao.me';
    default:
      // Default to local development
      return 'http://127.0.0.1:8083';
  }
};

const API_BASE = getApiBase();

export const API_BASE_URL = API_BASE;

// Log current API base for debugging
console.log(`[API Client] Environment: ${process.env.REACT_APP_ENV || 'default (development)'}`);
console.log(`[API Client] Using API Base: ${API_BASE}`);

/**
 * Clear authentication state
 * Separated to avoid circular dependency with auth utils
 */
function clearAuthState() {
  if (typeof window !== "undefined" && window?.localStorage) {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("nickname");
    localStorage.removeItem("email");
    localStorage.removeItem("loggedIn");
  }
}

/**
 * Send HTTP request with automatic 401 handling
 * @param {string} path - API path
 * @param {Object} options - Request options
 * @returns {Promise<any>} Response data
 *
 * Global behaviors:
 * - Automatically injects JWT token from localStorage
 * - On 401 response: clears auth state and throws error
 * - X-User-Id header is added by API Gateway (not frontend)
 */
async function request(path, { method = "GET", headers = {}, body } = {}) {
  const token =
    typeof window !== "undefined" && window?.localStorage
      ? localStorage.getItem("token")
      : null;

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body ? JSON.stringify(body) : undefined,
    credentials: "include",
  });

  const contentType = res.headers.get("content-type") || "";
  const data = contentType.includes("application/json") ? await res.json().catch(() => ({})) : await res.text();

  // Global 401 interceptor: clear auth state on unauthorized
  if (res.status === 401) {
    clearAuthState();
    const message = data?.errorMsg || data?.message || "Session expired. Please login again.";
    const err = new Error(message);
    err.status = 401;
    err.data = data;
    err.errorCode = data?.errorCode;
    throw err;
  }

  // Handle other HTTP errors
  if (!res.ok) {
    const message = data?.errorMsg || data?.message || data?.error || res.statusText || "Request failed";
    const err = new Error(message);
    err.status = res.status;
    err.data = data;
    err.errorCode = data?.errorCode;
    throw err;
  }

  // Handle backend business logic errors (ApiResult with success: false)
  if (data && typeof data === "object" && data.success === false) {
    const message = data.errorMsg || data.message || data.error || `Error ${data.errorCode || "occurred"}`;
    const err = new Error(message);
    err.status = res.status;
    err.data = data;
    err.errorCode = data.errorCode;
    throw err;
  }

  return data;
}

export const api = {
  post: (path, body, opts = {}) => {
    const { headers = {}, ...rest } = opts;
    return request(path, { method: "POST", body, ...rest, headers });
  },
  get: (path, opts = {}) => {
    const { headers = {}, ...rest } = opts;
    return request(path, { method: "GET", ...rest, headers });
  },
};

export default api;
