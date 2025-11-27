import { api } from "./client";

const RANKING_ENDPOINTS = [
  "/api/ranking",
  "/ranking",
  "/api/user/ranking",
  "/api/user/api/ranking",
];

const categoryMap = {
  Daily: "DAILY",
  Monthly: "MONTHLY",
  Season: "SEASONAL",
  Seasonal: "SEASONAL",
  TOTAL: "TOTAL",
};

function normalizeType(type) {
  if (!type) return "SEASONAL";
  if (categoryMap[type]) return categoryMap[type];
  const upper = String(type).toUpperCase();
  return categoryMap[upper] || upper;
}

async function getWithFallback(pathSuffix) {
  let lastError;
  for (const base of RANKING_ENDPOINTS) {
    try {
      return await api.get(`${base}${pathSuffix}`);
    } catch (error) {
      lastError = error;
      if (error?.status !== 404 && error?.status !== 405) {
        throw error;
      }
    }
  }
  throw lastError;
}

export async function fetchLeaderboard(type, limit = 50) {
  const backendType = normalizeType(type);
  const params = new URLSearchParams({ type: backendType, limit: String(limit) });
  const res = await getWithFallback(`/leaderboard?${params.toString()}`);
  return res?.data || res;
}

export async function fetchUserRank(type) {
  const backendType = normalizeType(type);
  const params = new URLSearchParams({ type: backendType });
  const res = await getWithFallback(`/my-rank?${params.toString()}`);
  return res?.data || res;
}

export const leaderboardApi = {
  fetchLeaderboard,
  fetchUserRank,
};

export default leaderboardApi;
