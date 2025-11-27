import {api} from "./client";

const MATCH_ENDPOINTS = [
  "/api/gomoku/match",
  "/api/match",
  "/api/user/match",
  "/match",
];

async function postWithFallback(pathSuffix, payload) {
  let lastError;
  for (const base of MATCH_ENDPOINTS) {
    try {
      return await api.post(`${base}${pathSuffix}`, payload);
    } catch (error) {
      lastError = error;
      if (error?.status !== 404 && error?.status !== 405) {
        throw error;
      }
    }
  }
  throw lastError;
}

function normalizePlayerId(playerId) {
  const value = playerId ?? localStorage.getItem("userId");
  if (!value) {
    throw new Error("Player ID is required");
  }
  return String(value);
}

/**
 * Start matching for casual or ranked game
 * @param {string} mode - "casual" or "ranked"
 * @param {string} playerId - Player ID
 * @returns {Promise<{status: string, roomCode?: string, roomId?: number, players?: string[], message: string}>}
 */
export async function startMatch(mode, playerId) {
  const payload = {
    mode: mode || "casual",
    playerId: normalizePlayerId(playerId),
  };
  const res = await postWithFallback("", payload);
  return res?.data || res;
}

/**
 * Cancel ongoing match request
 * @param {string} playerId - Player ID
 * @returns {Promise<Object>} Cancel response
 */
export async function cancelMatch(playerId) {
  const payload = {
    playerId: normalizePlayerId(playerId),
  };
  const res = await postWithFallback("/cancel", payload);
  return res?.data || res;
}

export const matchApi = {
  startMatch,
  cancelMatch,
};

export default matchApi;
