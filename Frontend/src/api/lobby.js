import { api } from "./client";

const LOBBY_ENDPOINTS = [
  "/api/gomoku/lobby",
  "/api/lobby",
  "/api/user/lobby",
  "/lobby",
];

async function postWithFallback(pathSuffix, payload) {
  let lastError;
  for (const base of LOBBY_ENDPOINTS) {
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

/**
 * Normalize room code
 * @param {string} code - Room code
 * @returns {string} Normalized room code
 */
function normalizeCode(code) {
  const trimmed = String(code || "").trim();
  if (!trimmed) {
    throw new Error("Room code is required");
  }
  return trimmed;
}

/**
 * Normalize player ID - Use string to avoid JavaScript large number precision loss
 * @param {string|number} playerId - Player ID
 * @returns {string} Normalized player ID string
 */
function normalizePlayerId(playerId) {
  const value = playerId ?? localStorage.getItem("userId");
  if (!value) {
    throw new Error("Player ID is required");
  }
  return String(value);
}

/**
 * Create a new room
 * @param {string|number} playerId - Player ID
 * @returns {Promise<Object>} Room information containing roomCode and roomId
 */
export async function createRoom(playerId) {
  const payload = { playerId: normalizePlayerId(playerId) };
  const res = await postWithFallback("/create-room", payload);
  return res?.data || res;
}

/**
 * Join a room
 * @param {string} roomCode - Room code
 * @param {string|number} playerId - Player ID
 * @returns {Promise<Object>} Room information and player list
 */
export async function joinRoom(roomCode, playerId) {
  const payload = {
    roomCode: normalizeCode(roomCode),
    playerId: normalizePlayerId(playerId),
  };
  const res = await postWithFallback("/join-room", payload);
  return res?.data || res;
}

/**
 * Leave a room
 * @param {string} roomCode - Room code
 * @param {string|number} playerId - Player ID
 * @returns {Promise<Object>} Operation result
 */
export async function leaveRoom(roomCode, playerId) {
  const payload = {
    roomCode: normalizeCode(roomCode),
    playerId: normalizePlayerId(playerId),
  };
  const res = await postWithFallback("/leave", payload);
  return res?.data || res;
}

export const lobbyApi = {
  createRoom,
  joinRoom,
  leaveRoom,
};

export default lobbyApi;
