import { api } from "../client";

/**
 * Ranking API Module
 * Handles all ranking-related API calls
 *
 * Note: Backend context-path is /api/ranking
 */

/**
 * Get player's ranking profile
 * @returns {Promise} Player profile with ranking info
 */
export async function getRankingProfile() {
  const response = await api.get("/api/ranking/profile");
  return response?.data || response;
}

/**
 * Get ranking profile for a specific user
 * Tries multiple common backend routes for compatibility.
 * @param {string|number} userId
 */
export async function getRankingProfileById(userId) {
  const id = String(userId ?? "").trim();
  if (!id) throw new Error("userId is required");
  // Preferred route: /api/ranking/profile/{userId}
  try {
    const res = await api.get(`/api/ranking/profile/${encodeURIComponent(id)}`);
    return res?.data || res;
  } catch (e) {
    // Fallback: /api/ranking/profile?userId=...
    try {
      const res2 = await api.get(`/api/ranking/profile`, { params: { userId: id } });
      return res2?.data || res2;
    } catch (e2) {
      // Final fallback: return null so caller can use leaderboard
      return null;
    }
  }
}

/**
 * Get top leaderboards (Daily, Weekly, Monthly with top 50 each)
 * @returns {Promise} Object containing daily, weekly, monthly leaderboards
 */
// Known backend context path variants for different deployments/gateways
const RANKING_BASES = [
  "/api/ranking", // via API gateway (preferred)
  "/ranking", // direct service without /api prefix
  "/api/user/ranking", // legacy gateway mount
  "/api/user/api/ranking", // double-mounted legacy
];

async function getWithBaseFallback(pathSuffix) {
  let lastError = null;
  for (const base of RANKING_BASES) {
    try {
      const res = await api.get(`${base}${pathSuffix}`);
      return res?.data || res;
    } catch (e) {
      lastError = e;
      // Only continue fallback on 404/405 (not found/method not allowed)
      if (e?.status !== 404 && e?.status !== 405) {
        throw e;
      }
    }
  }
  if (lastError) throw lastError;
}

export async function getTopLeaderboards() {
  // 1) Try aggregated endpoint across known bases
  try {
    const data = await getWithBaseFallback("/leaderboard");
    if (data && (data.daily || data.weekly || data.monthly)) {
      return data;
    }
    if (Array.isArray(data)) {
      return { daily: { topList: [] }, weekly: { topList: [] }, monthly: { topList: data } };
    }
    // fall through to per-type fetches
  } catch (_) {
    // continue with per-type fallback
  }

  // 2) Fallback to per-type queries across bases
  const types = ["DAILY", "WEEKLY", "MONTHLY"];
  const result = { daily: { topList: [] }, weekly: { topList: [] }, monthly: { topList: [] } };

  for (const t of types) {
    try {
      const data = await getWithBaseFallback(`/leaderboard?type=${encodeURIComponent(t)}&limit=50`);
      const list = Array.isArray(data)
        ? data
        : Array.isArray(data?.topList)
        ? data.topList
        : Array.isArray(data?.data)
        ? data.data
        : [];
      if (t === "DAILY") result.daily = { topList: list };
      if (t === "WEEKLY") result.weekly = { topList: list };
      if (t === "MONTHLY") result.monthly = { topList: list };
    } catch (_) {
      // keep empty for that category
    }
  }

  return result;
}

/**
 * Get ranking rules configuration
 * @returns {Promise} Ranking rules configuration
 */
export async function getRankingRules() {
  const response = await api.get("/api/ranking/rules");
  return response?.data || response;
}

/**
 * Settle a match
 * @param {Object} settlementData - Match settlement data
 * @param {string|number} settlementData.matchId - Match ID
 * @param {string|number} [settlementData.winnerId] - Winner's user ID (null if draw)
 * @param {string|number} [settlementData.loserId] - Loser's user ID (null if draw)
 * @param {string} settlementData.modeType - Game mode: RANKED, CASUAL, or PRIVATE
 * @returns {Promise} Settlement response with player rewards
 */
export async function settleMatch(settlementData) {
  const response = await api.post("/api/ranking/settle", settlementData);
  return response?.data || response;
}

export const rankingApi = {
  getRankingProfile,
  getRankingProfileById,
  getTopLeaderboards,
  getRankingRules,
  settleMatch,
};

export default rankingApi;
