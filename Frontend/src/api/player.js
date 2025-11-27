import { api } from "./client";

const PLAYER_ENDPOINTS = [
  "/api/gomoku/player",
  "/api/player",
  "/api/user/player",
  "/player",
];

async function requestPlayer(method, suffix) {
  let lastError;
  for (const base of PLAYER_ENDPOINTS) {
    try {
      return await api.get(`${base}${suffix}`);
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
 * Get player's current status (matching queue or room)
 * @returns {Promise<{matchingStatus: {inQueue: boolean, mode: string}, roomStatus: {inRoom: boolean, roomCode: string, roomId: number, status: string}}>}
 */
export async function getPlayerStatus() {
  const res = await requestPlayer("get", "/status");
  return res?.data || res;
}

export const playerApi = {
  getPlayerStatus,
};

export default playerApi;
