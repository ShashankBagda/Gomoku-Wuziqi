import {api} from "./client";

const GAME_ENDPOINTS = [
  "/api/gomoku/game",
  "/api/game",
  "/api/user/api/game",
  "/api/user/game",
  "/game",
];

async function requestGame(method, suffix, payload) {
  let lastError;
  for (const base of GAME_ENDPOINTS) {
    try {
      if (method === "get") {
        return await api.get(`${base}${suffix}`);
      }
      return await api.post(`${base}${suffix}`, payload);
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
 * Normalize room ID - Use string to avoid JavaScript large number precision loss
 * @param {string|number} roomId - Room ID
 * @returns {string} Normalized room ID string
 */
function normalizeRoomId(roomId) {
  if (roomId == null) {
    throw new Error("Missing roomId");
  }
  // Convert to string to preserve precision for large numbers
  const strId = String(roomId).trim();
  // Validate if it's a valid positive integer string
  if (!/^\d+$/.test(strId) || strId === "0") {
    throw new Error("Invalid roomId");
  }
  return strId;
}

/**
 * Normalize player ID - Use string to avoid JavaScript large number precision loss
 * @param {string|number} playerId - Player ID
 * @returns {string} Normalized player ID string
 */
function normalizePlayerId(playerId) {
  if (playerId == null) {
    throw new Error("Missing playerId");
  }
  // Convert to string to preserve precision for large numbers
  const strId = String(playerId).trim();
  // Validate if it's a valid positive integer string
  if (!/^\d+$/.test(strId) || strId === "0") {
    throw new Error("Invalid playerId");
  }
  return strId;
}

function buildQuery(params) {
  const search = new URLSearchParams(params);
  return `?${search.toString()}`;
}

/**
 * Fetch game state
 * @param {string|number} roomId - Room ID
 * @returns {Promise<Object>} Game state data
 */
export async function fetchGameState(roomId) {
  const normalizedRoomId = normalizeRoomId(roomId);
  const res = await requestGame("get", `/${normalizedRoomId}/state`);
  return res?.data || res;
}

/**
 * Send game action
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @param {Object} action - Action object {type, position}
 * @returns {Promise<Object>} Action result
 */
export async function sendGameAction(roomId, playerId, action) {
  const normalizedRoomId = normalizeRoomId(roomId);
  const normalizedPlayerId = normalizePlayerId(playerId);

  // Build payload with numeric coordinates
  const payload = {
    type: action?.type,
    ...(action?.position
      ? {
          position: {
            x: Number(action.position.x),
            y: Number(action.position.y),
          },
        }
      : {}),
  };

  if (!payload.type) {
    throw new Error("Missing action type");
  }

  // Pass playerId as query parameter (string format)
  const query = buildQuery({ playerId: normalizedPlayerId });
  const res = await requestGame(
    "post",
    `/${normalizedRoomId}/action${query}`,
    payload
  );
  return res?.data || res;
}

/**
 * Player ready up
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @returns {Promise<Object>} Game state
 */
export async function readyUp(roomId, playerId) {
  return sendGameAction(roomId, playerId, { type: "READY" });
}

/**
 * Make a move (place a stone)
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @param {number} x - X coordinate (backend's first dimension)
 * @param {number} y - Y coordinate (backend's second dimension)
 * @returns {Promise<Object>} Game state
 *
 * Note: Backend uses board[x][y] format
 */
export async function makeMove(roomId, playerId, x, y) {
  return sendGameAction(roomId, playerId, {
    type: "MOVE",
    position: { x, y },
  });
}

/**
 * Surrender the game
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @returns {Promise<Object>} Game state
 */
export async function surrender(roomId, playerId) {
  return sendGameAction(roomId, playerId, { type: "SURRENDER" });
}

/**
 * Propose a draw
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @param {boolean} agree - Whether to agree (default true means initiating draw request)
 * @returns {Promise<Object>} Game state
 */
export async function proposeDraw(roomId, playerId, agree = true) {
  return sendGameAction(roomId, playerId, { type: agree ? "DRAW" : "DRAW_DISAGREE" });
}

/**
 * Respond to draw request
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @param {boolean} accept - Whether to accept the draw
 * @returns {Promise<Object>} Game state
 */
export async function respondDraw(roomId, playerId, accept) {
  return sendGameAction(roomId, playerId, { type: accept ? "DRAW_AGREE" : "DRAW_DISAGREE" });
}

/**
 * Request undo
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @returns {Promise<Object>} Game state
 */
export async function requestUndo(roomId, playerId) {
  return sendGameAction(roomId, playerId, { type: "UNDO" });
}

/**
 * Respond to undo request
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @param {boolean} accept - Whether to accept the undo
 * @returns {Promise<Object>} Game state
 */
export async function respondUndo(roomId, playerId, accept) {
  return sendGameAction(roomId, playerId, { type: accept ? "UNDO_AGREE" : "UNDO_DISAGREE" });
}

/**
 * Request to restart the game after it finishes
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @returns {Promise<Object>} Game state
 */
export async function requestRestart(roomId, playerId) {
  return sendGameAction(roomId, playerId, {type: "RESTART"});
}

/**
 * Respond to restart request
 * @param {string|number} roomId - Room ID
 * @param {string|number} playerId - Player ID
 * @param {boolean} accept - Whether to accept the restart
 * @returns {Promise<Object>} Game state
 */
export async function respondRestart(roomId, playerId, accept) {
  return sendGameAction(roomId, playerId, {type: accept ? "RESTART_AGREE" : "RESTART_DISAGREE"});
}

export const gameApi = {
  fetchGameState,
  sendGameAction,
  readyUp,
  makeMove,
  surrender,
  proposeDraw,
  respondDraw,
  requestUndo,
  respondUndo,
  requestRestart,
  respondRestart,
};

export default gameApi;
