import { api } from "./client";

export async function getGptMove(board, next, opts = {}) {
  const payload = { board, next };
  if (Number.isInteger(opts.k) && opts.k > 0) payload.k = opts.k;
  if (opts.style) payload.style = String(opts.style);
  const res = await api.post("/api/gomoku/ai/gpt-move", payload);
  return res?.data || res;
}

export const aiApi = { getGptMove };
export default aiApi;
