/**
 * Shared helpers for mapping and formatting player statistics.
 */

export function createEmptyStats() {
  return {
    score: null,
    rank: null,
    wins: null,
    gamesPlayed: null,
    winRate: null,
    totalExp: null,
    level: null,
    xpPercent: null,
  };
}

export function coerceNumber(value) {
  if (value === null || value === undefined) return null;
  const num = Number(value);
  if (Number.isNaN(num) || !Number.isFinite(num)) return null;
  return num;
}

export function sanitizePercentage(value) {
  const num = coerceNumber(value);
  if (num === null) return null;
  if (num <= 1 && num >= 0) return Math.max(0, Math.min(100, num * 100));
  return Math.max(0, Math.min(100, num));
}

export function mapUserStats(raw) {
  if (!raw) {
    return createEmptyStats();
  }

  const payload =
    raw && typeof raw === "object" && !Array.isArray(raw)
      ? raw.data && typeof raw.data === "object" && !Array.isArray(raw.data)
        ? raw.data
        : raw
      : null;

  if (!payload || typeof payload !== "object") {
    return createEmptyStats();
  }

  const score =
    coerceNumber(
      payload.currentTotalScore ??
        payload.totalScore ??
        payload.score ??
        payload.rating ??
        payload.points
    ) ?? null;

  const rank =
    coerceNumber(
      payload.rank ??
        payload.rankPosition ??
        payload.position ??
        payload.ranking ??
        payload.order
    ) ?? null;

  const wins =
    coerceNumber(
      payload.wins ??
        payload.totalWins ??
        payload.winCount ??
        payload.victories ??
        payload.win
    ) ?? null;

  const losses =
    coerceNumber(
      payload.losses ??
        payload.totalLosses ??
        payload.lossCount
    ) ?? null;

  let games =
    coerceNumber(
      payload.gamesPlayed ??
        payload.totalMatches ??
        payload.matches ??
        payload.games ??
        payload.matchCount
    ) ?? null;

  if (games === null && wins !== null && losses !== null) {
    games = wins + losses;
  }

  let winRate = payload.winRate ?? payload.winrate ?? payload.win_ratio;
  winRate = winRate != null ? coerceNumber(winRate) : null;
  if (winRate != null && winRate <= 1 && games && games > 0 && winRate <= 1.01) {
    winRate = winRate * 100;
  } else if (winRate == null && wins != null && games) {
    winRate = (wins / games) * 100;
  }

  const totalExp =
    coerceNumber(
      payload.totalExp ??
        payload.exp ??
        payload.experience ??
        payload.totalExperience
    ) ?? null;

  const level =
    coerceNumber(
      payload.levelId ?? payload.level ?? payload.levelNumber
    ) ?? null;

  let xpPercent =
    payload.levelProgress ??
    payload.expProgress ??
    payload.progress ??
    payload.currentProgress ??
    null;
  xpPercent = sanitizePercentage(xpPercent);

  return {
    score,
    rank,
    wins,
    gamesPlayed: games,
    winRate: winRate != null ? Math.max(0, Math.min(100, winRate)) : null,
    totalExp,
    level,
    xpPercent,
  };
}

export function formatNumber(value) {
  const num = coerceNumber(value);
  if (num === null) return "—";
  return num.toLocaleString();
}

export function formatPercentage(value) {
  const num = coerceNumber(value);
  if (num === null) return "—";
  return `${num.toFixed(1)}%`;
}
