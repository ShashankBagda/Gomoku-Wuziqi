import { api } from "./client";

const COMM_ENDPOINTS = [
  "/api/rtc",            // new RTC endpoints (preferred)
  "/api/gomoku/rtc",     // direct to gomoku service (fallback)
  "/api/communication",  // legacy path (fallback)
];

async function get(pathSuffix) {
  let lastError;
  for (const base of COMM_ENDPOINTS) {
    try {
      const response = await api.get(`${base}${pathSuffix}`);
      return response?.data ?? response;
    } catch (error) {
      lastError = error;
      if (error?.status !== 404 && error?.status !== 405) {
        throw error;
      }
    }
  }
  throw lastError;
}

export async function fetchOnlineCount() {
  return get("/online-count");
}

export const communicationApi = {
  fetchOnlineCount,
};

export default communicationApi;
