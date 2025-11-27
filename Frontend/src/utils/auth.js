/**
 * Authentication Utilities
 *
 * Centralized authentication management for high cohesion and low coupling.
 * All auth-related operations should go through these utility functions.
 */

import {userApi} from "../api";
import {emitAuthChange} from "./authState";

// ==================== Constants ====================

const AUTH_KEYS = {
  TOKEN: "token",
  USER_ID: "userId",
  LOGGED_IN: "loggedIn",
  NICKNAME: "nickname",
  EMAIL: "email",
};

// ==================== Storage Operations ====================

/**
 * Save authentication data to localStorage
 * @param {Object} authData - Authentication data from login response
 * @param {string} authData.token - JWT token
 * @param {number} authData.userId - User ID
 * @param {string} [authData.nickname] - User nickname
 * @param {string} [authData.email] - User email
 */
export function saveAuthData(authData) {
  if (authData.token) {
    localStorage.setItem(AUTH_KEYS.TOKEN, authData.token);
  }
  if (authData.userId) {
    localStorage.setItem(AUTH_KEYS.USER_ID, String(authData.userId));
  }
  if (authData.nickname) {
    localStorage.setItem(AUTH_KEYS.NICKNAME, authData.nickname);
  }
  if (authData.email) {
    localStorage.setItem(AUTH_KEYS.EMAIL, authData.email);
  }
  localStorage.setItem(AUTH_KEYS.LOGGED_IN, "1");
  localStorage.setItem("lastLoginAt", String(Date.now()));
  emitAuthChange();
}

/**
 * Get authentication data from localStorage
 * @returns {Object} Authentication data
 */
export function getAuthData() {
  return {
    token: localStorage.getItem(AUTH_KEYS.TOKEN),
    userId: localStorage.getItem(AUTH_KEYS.USER_ID),
    nickname: localStorage.getItem(AUTH_KEYS.NICKNAME),
    email: localStorage.getItem(AUTH_KEYS.EMAIL),
    loggedIn: localStorage.getItem(AUTH_KEYS.LOGGED_IN) === "1",
  };
}

/**
 * Clear all authentication data from localStorage
 */
export function clearAuthData() {
  Object.values(AUTH_KEYS).forEach((key) => {
    localStorage.removeItem(key);
  });
  emitAuthChange();
}

/**
 * Check if user is authenticated
 * @returns {boolean} True if user is logged in
 */
export function isAuthenticated() {
  const token = localStorage.getItem(AUTH_KEYS.TOKEN);
  const loggedIn = localStorage.getItem(AUTH_KEYS.LOGGED_IN) === "1";
  if (!(token && loggedIn)) return false;
  // Expire session after 7 days by default
  const last = parseInt(localStorage.getItem("lastLoginAt") || "0", 10);
  const maxAgeMs = 7 * 24 * 60 * 60 * 1000;
  if (!Number.isFinite(last) || Date.now() - last > maxAgeMs) {
    try { clearAuthData(); } catch (_) {}
    return false;
  }
  return true;
}

// ==================== API Operations ====================

/**
 * Perform user login
 * @param {Object} credentials - Login credentials
 * @param {string} credentials.username - Email address
 * @param {string} credentials.password - Plain text password
 * @returns {Promise<Object>} Login response data
 * @throws {Error} If login fails
 */
export async function login(credentials) {
  clearAuthData(); // Clear old session before login

  const result = await userApi.login(credentials);
  const data = result?.data || result;

  saveAuthData(data);
  return data;
}

/**
 * Perform user logout
 * @returns {Promise<void>}
 */
export async function logout() {
  try {
    await userApi.logout();
  } catch (error) {
    console.error("Logout API call failed:", error);
  } finally {
    clearAuthData();
  }
}

// ==================== Event Helpers ====================

/**
 * Create a storage change listener for auth state
 * Useful for syncing auth state across components
 * @param {Function} callback - Callback function when auth state changes
 * @returns {Function} Cleanup function to remove listener
 */
export function onAuthStateChange(callback) {
  const handleStorageChange = (event) => {
    // Only trigger if auth-related keys changed
    if (Object.values(AUTH_KEYS).includes(event.key) || event.key === null) {
      callback(isAuthenticated());
    }
  };

  window.addEventListener("storage", handleStorageChange);

  // Return cleanup function
  return () => {
    window.removeEventListener("storage", handleStorageChange);
  };
}

// ==================== Default Export ====================

const authUtils = {
  // Storage operations
  saveAuthData,
  getAuthData,
  clearAuthData,
  isAuthenticated,

  // API operations
  login,
  logout,

  // Event helpers
  onAuthStateChange,

  // Constants
  AUTH_KEYS,
};

export default authUtils;
