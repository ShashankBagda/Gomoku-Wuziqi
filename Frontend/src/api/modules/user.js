/**
 * User API Module
 *
 * Contains all user-related API requests
 * Reference backend API docs: gomuku-backend/user/
 */

import {api} from "../client";
import {rsaEncryptPkcs1v15} from "../utils/rsa";

// ==================== Public APIs (No authentication required) ====================

/**
 * Get RSA public key for password encryption
 * @returns {Promise<string>} Public key in PEM format
 *
 * Backend endpoint: GET /api/user/public-key
 * Response format: ApiResult<String>
 */
export async function getPublicKey() {
  const res = await api.get("/api/user/public-key");
  return res?.data || res;
}

/**
 * User registration
 * @param {Object} params - Registration parameters
 * @param {string} params.email - Email address (required, max 128 chars)
 * @param {string} params.nickname - Nickname (required, 2-64 chars, unique)
 * @param {string} params.password - Plain text password (required, will be auto-encrypted)
 * @param {string} [params.avatarUrl] - Avatar URL (optional, max 255 chars)
 * @param {string} [params.avatarBase64] - Base64 encoded avatar (optional)
 * @param {string} [params.country] - Country (optional, max 64 chars)
 * @param {number|string} [params.gender] - Gender (optional, 0=unknown, 1=male, 2=female, or string "Male"/"Female"/"Other")
 * @param {string} params.verificationCode - Email verification code (required, 6 digits)
 * @returns {Promise<Object>} Registration response with token (auto-login)
 *
 * Backend endpoint: POST /api/user/register
 * Request body: UserRegisterRequest
 * Response format: ApiResult<UserRegisterResponse>
 *
 * Response fields:
 * - userId: User ID
 * - email: Email address
 * - nickname: Nickname
 * - avatarUrl: Avatar URL
 * - country: Country
 * - gender: Gender (0/1/2)
 * - status: Status (0=inactive, 1=active, 2=disabled, 3=deleted)
 * - createdAt: Creation timestamp
 * - token: JWT Token (for auto-login after registration)
 */
export async function register(params) {
  const { email, nickname, password, verificationCode, avatarUrl, avatarBase64, country, gender } = params;

  // Get RSA public key
  const publicKey = await getPublicKey();

  // Encrypt password
  const encryptedPassword = rsaEncryptPkcs1v15(publicKey, password);

  // Handle gender field: supports string "Male"/"Female"/"Other" or number 0/1/2
  let genderValue = gender;
  if (typeof gender === "string") {
    const genderMap = {
      "Male": 1,
      "Female": 2,
      "Other": 0,
    };
    genderValue = genderMap[gender] !== undefined ? genderMap[gender] : 0;
  }

  const payload = {
    email: (email || "").trim(),
    nickname: (nickname || "").trim(),
    encryptedPassword,
    ...(verificationCode && { verificationCode: verificationCode.trim() }),
    ...(avatarUrl && { avatarUrl }),
    ...(avatarBase64 && { avatarBase64 }),
    ...(country && { country }),
    ...(genderValue !== undefined && { gender: Number(genderValue) }),
  };

  const res = await api.post("/api/user/register", payload);
  return res?.data || res;
}

/**
 * User login
 * @param {Object} params - Login parameters
 * @param {string} params.username - Username (email or nickname, required)
 * @param {string} params.password - Plain text password (required, will be auto-encrypted)
 * @returns {Promise<Object>} Login response
 *
 * Backend endpoint: POST /api/user/login
 * Request body: UserLoginRequest
 * Response format: ApiResult<UserLoginResponse>
 *
 * Response fields:
 * - userId: User ID
 * - email: Email address
 * - nickname: Nickname
 * - avatarUrl: Avatar URL
 * - token: JWT Token
 */
export async function login(params) {
  const { username, password } = params;

  // Get RSA public key
  const publicKey = await getPublicKey();

  // Encrypt password
  const encryptedPassword = rsaEncryptPkcs1v15(publicKey, password);

  const payload = {
    username: (username || "").trim(),
    encryptedPassword,
  };

  const res = await api.post("/api/user/login", payload);
  return res?.data || res;
}

/**
 * Fetch a user's public profile
 *
 * @param {string|number} userId - Target user ID
 * @returns {Promise<Object>} Public profile payload
 *
 * Backend endpoint: GET /api/user/{userId}
 */
export async function fetchPublicProfile(userId) {
  const normalizedId = String(userId ?? "").trim();
  if (!normalizedId) {
    throw new Error("User ID is required");
  }

  const res = await api.get(`/api/user/${normalizedId}`);
  console.log("[fetchPublicProfile] Full response from API:", res);
  console.log("[fetchPublicProfile] res.data:", res?.data);
  console.log("[fetchPublicProfile] Returning:", res?.data || res);
  return res?.data || res;
}

// ==================== Protected APIs (Requires JWT Token) ====================

/**
 * Get current user's profile (requires authentication)
 * @returns {Promise<Object>} Current user's profile
 *
 * Backend endpoint: GET /api/user/{userId}
 * Request headers: Authorization: Bearer <JWT_TOKEN>
 * Response format: ApiResult<UserProfileResponse>
 *
 * Response fields:
 * - userId: User ID
 * - email: Email address
 * - nickname: Nickname
 * - avatarUrl: Avatar URL
 * - country: Country
 * - gender: Gender
 * - status: Status
 */
export async function getCurrentUserProfile() {
  // Get userId from localStorage
  const userId = typeof window !== "undefined" && window?.localStorage
      ? localStorage.getItem("userId")
      : null;

  if (!userId) {
    throw new Error("No user ID found");
  }

  const res = await api.get(`/api/user/${userId}`);
  return res?.data || res;
}

/**
 * Send verification code to email
 * @param {Object} params - Parameters
 * @param {string} params.email - Email address (required)
 * @returns {Promise<string>} Success message
 *
 * Backend endpoint: POST /api/user/verify/send-email/send
 * Request body: { email: string }
 * Response format: ApiResult<String>
 */
export async function sendVerificationCode(params) {
  const { email } = params;

  const payload = {
    email: (email || "").trim(),
  };

  const res = await api.post("/api/user/verify/send-email/send", payload);
  return res?.data || res;
}

/**
 * Reset password (PUBLIC API - no authentication required)
 * Supports two scenarios:
 * 1. Forgot password (user not logged in): manually input email
 * 2. Change password (user logged in): email auto-filled from current session
 *
 * @param {Object} params - Reset password parameters
 * @param {string} params.email - Email address (required)
 * @param {string} params.newPassword - New password (plain text, required, will be auto-encrypted)
 * @param {string} params.verificationCode - Email verification code (required, 6 digits)
 * @returns {Promise<void>}
 *
 * Backend endpoint: POST /api/user/reset-password
 * Request body: UserResetPasswordRequest
 * Response format: ApiResult<Void>
 */
export async function resetPassword(params = {}) {
  const { email, verificationCode, newPassword, userId } = params;

  if (!newPassword) {
    throw new Error("New password is required");
  }

  // Get RSA public key
  const publicKey = await getPublicKey();

  // Encrypt new password
  const encryptedNewPassword = rsaEncryptPkcs1v15(publicKey, newPassword);

  const trimmedEmail =
    typeof email === "string" ? email.trim() : "";
  const trimmedCode =
    typeof verificationCode === "string" ? verificationCode.trim() : "";

  const payload = {
    encryptedNewPassword,
    ...(trimmedEmail ? { email: trimmedEmail } : {}),
    ...(trimmedCode ? { verificationCode: trimmedCode } : {}),
  };

  const headers = {};
  let resolvedUserId = userId;
  if (
    (resolvedUserId === undefined || resolvedUserId === null) &&
    typeof window !== "undefined" &&
    window?.localStorage
  ) {
    resolvedUserId = localStorage.getItem("userId");
  }

  if (resolvedUserId !== undefined && resolvedUserId !== null) {
    const normalizedUserId = String(resolvedUserId).trim();
    if (normalizedUserId) {
      headers["X-USER-ID"] = normalizedUserId;
    }
  }

  const res = await api.post("/api/user/reset-password", payload, { headers });
  return res?.data || res;
}

/**
 * User logout
 * @returns {Promise<void>}
 *
 * Backend endpoint: POST /api/user/logout
 * Request headers: Authorization: Bearer <JWT_TOKEN>, X-USER-ID: <USER_ID>
 * Response format: ApiResult<Void>
 *
 * Note: Requires authenticated state, userId will be extracted from token
 */
export async function logout() {
  const headers = {};
  if (typeof window !== "undefined" && window?.localStorage) {
    const storedUserId = localStorage.getItem("userId");
    if (storedUserId) {
      const normalizedUserId = String(storedUserId).trim();
      if (normalizedUserId) {
        headers["X-USER-ID"] = normalizedUserId;
      }
    }
  }

  const res = await api.post("/api/user/logout", null, { headers });
  return res?.data || res;
}

// ==================== Default Export ====================

const userApiExports = {
  // Public APIs
  getPublicKey,
  register,
  login,
  fetchPublicProfile,
  sendVerificationCode,

  // Protected APIs
  getCurrentUserProfile,
  resetPassword,
  logout,
};

export default userApiExports;
