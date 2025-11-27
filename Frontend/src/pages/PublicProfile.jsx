import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import SectionCard from "../components/SectionCard";
import BackgroundAnimation from "../components/BackgroundAnimation";
import { fetchLeaderboard } from "../api/leaderboard";
import { userApi, rankingApi } from "../api";
import {
  createEmptyStats,
  mapUserStats,
  formatNumber,
  formatPercentage,
  sanitizePercentage,
} from "../utils/profileStats";

function normalizeProfileData(raw, fallbackId, fallbackNickname) {
  if (!raw || typeof raw !== "object") {
    return {
      userId: fallbackId,
      nickname: fallbackNickname,
    };
  }

  const payload =
    raw && typeof raw === "object" && !Array.isArray(raw)
      ? raw.data && typeof raw.data === "object" && !Array.isArray(raw.data)
        ? raw.data
        : raw
      : raw;

  const userId =
    payload?.userId ??
    payload?.playerId ??
    payload?.id ??
    fallbackId;

  const nickname =
    payload?.nickname ??
    payload?.displayName ??
    payload?.username ??
    payload?.playerName ??
    payload?.email?.split("@")[0] ??
    fallbackNickname;

  return {
    userId: String(userId ?? fallbackId),
    nickname: nickname || fallbackNickname,
    avatarUrl:
      payload?.avatarUrl ??
      payload?.avatar ??
      payload?.avatar_url ??
      null,
    country: payload?.country ?? payload?.location ?? payload?.nationality ?? null,
    bio: payload?.bio ?? payload?.signature ?? "",
    joinedAt:
      payload?.createdAt ??
      payload?.joinedAt ??
      payload?.registeredAt ??
      payload?.created_at ??
      null,
  };
}

function extractLeaderboardEntries(raw) {
  if (!raw) {
    return [];
  }
  if (Array.isArray(raw)) {
    return raw;
  }
  const data = raw.data ?? raw.records ?? raw.players ?? raw.items;
  if (Array.isArray(data)) {
    return data;
  }
  return [];
}

function matchesPlayer(entry, targetId) {
  if (!entry || !targetId) {
    return false;
  }

  const candidates = [
    entry.userId,
    entry.userID,
    entry.playerId,
    entry.playerID,
    entry.id,
    entry.uid,
  ];

  if (entry.player && typeof entry.player === "object") {
    candidates.push(
      entry.player.userId,
      entry.player.id,
      entry.player.playerId
    );
  }

  return candidates.some(
    (value) => value !== undefined && value !== null && String(value) === targetId
  );
}

function mergeProfileWithLeaderboard(profile, leaderboardEntry) {
  if (!leaderboardEntry) {
    return profile;
  }

  const merged = { ...profile };
  const nickname =
    merged.nickname && !merged.nickname.startsWith("Player ")
      ? merged.nickname
      : leaderboardEntry.nickname ??
        leaderboardEntry.playerName ??
        merged.nickname;

  merged.nickname = nickname || merged.nickname;

  if (!merged.avatarUrl) {
    merged.avatarUrl =
      leaderboardEntry.avatarUrl ??
      leaderboardEntry.avatar ??
      (leaderboardEntry.player && leaderboardEntry.player.avatarUrl) ??
      null;
  }

  if (!merged.country) {
    merged.country =
      leaderboardEntry.country ??
      leaderboardEntry.region ??
      (leaderboardEntry.player && leaderboardEntry.player.country) ??
      null;
  }

  return merged;
}

function formatDate(value) {
  if (!value) return "—";
  const date =
    typeof value === "number"
      ? new Date(value)
      : new Date(String(value));
  if (Number.isNaN(date.getTime())) {
    return "—";
  }
  return date.toLocaleDateString();
}

function getFlag(code) {
  if (!code) return "";
  const trimmed = String(code).trim();
  // Only render a flag when a 2-letter country code is provided
  if (!/^[A-Za-z]{2}$/.test(trimmed)) return "";
  const upper = trimmed.toUpperCase();
  try {
    return String.fromCodePoint(...[...upper].map((c) => 127397 + c.charCodeAt()));
  } catch (error) {
    return "";
  }
}

function Stat({ label, value, color }) {
  return (
    <div
      className={`p-4 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 bg-gradient-to-r ${color} bg-opacity-10 hover:shadow-md transition-all`}
    >
      <div className="text-2xl font-extrabold drop-shadow-sm">{value}</div>
      <div className="text-xs mt-1 font-medium">{label}</div>
    </div>
  );
}

export default function PublicProfile() {
  const { playerId } = useParams();
  const navigate = useNavigate();

  const normalizedId = useMemo(() => {
    if (!playerId) return "";
    return String(playerId).trim();
  }, [playerId]);

  const fallbackNickname = useMemo(
    () => (normalizedId ? `Player ${normalizedId}` : "Unknown Player"),
    [normalizedId]
  );

  const [profileInfo, setProfileInfo] = useState(null);
  const [stats, setStats] = useState(() => createEmptyStats());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [seasonPlacement, setSeasonPlacement] = useState(null);

  useEffect(() => {
    if (!normalizedId) {
      setError("Invalid player ID.");
      setLoading(false);
      return;
    }

    let cancelled = false;

    const load = async () => {
      setLoading(true);
      setError("");
      try {
        const [profileData, rankingData, leaderboardData] = await Promise.all([
          // Make all three fetches fault-tolerant: return null on any error
          userApi.fetchPublicProfile(normalizedId).catch(() => null),
          rankingApi.getRankingProfileById(normalizedId).catch(() => null),
          fetchLeaderboard("Season").catch(() => null),
        ]);

        if (cancelled) return;

        const baseProfile = normalizeProfileData(
          profileData,
          normalizedId,
          fallbackNickname
        );

        // Prefer direct ranking profile for stats; fallback to leaderboard
        let leaderboardEntry = null;
        if (rankingData && typeof rankingData === "object") {
          // Normalize into a leaderboard-like entry for mapUserStats
          leaderboardEntry = rankingData.data || rankingData;
        }
        if (!leaderboardEntry) {
          const entries = extractLeaderboardEntries(leaderboardData);
          leaderboardEntry = entries.find((entry) => matchesPlayer(entry, baseProfile.userId));
        }

        const mergedProfile = mergeProfileWithLeaderboard(
          baseProfile,
          leaderboardEntry
        );

        setProfileInfo(mergedProfile);
        setStats(leaderboardEntry ? mapUserStats(leaderboardEntry) : createEmptyStats());

        if (leaderboardEntry) {
          const rank =
            leaderboardEntry.rank ??
            leaderboardEntry.rankPosition ??
            leaderboardEntry.position ??
            leaderboardEntry.ranking ??
            leaderboardEntry.order ??
            null;
          const numericRank = rank != null ? Number(rank) : null;
          setSeasonPlacement(
            numericRank !== null && Number.isFinite(numericRank) ? numericRank : null
          );
        } else {
          setSeasonPlacement(null);
        }
      } catch (err) {
        if (!cancelled) {
          // Do not hard-fail the page; show soft error and fallback data
          setError("Some details could not be loaded at the moment.");
          setProfileInfo({ userId: normalizedId, nickname: fallbackNickname });
          setStats(createEmptyStats());
          setSeasonPlacement(null);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [normalizedId, fallbackNickname]);

  const avatarSeed = profileInfo?.avatarUrl
    ? null
    : profileInfo?.nickname || profileInfo?.userId || "gomoku";

  const avatarUrl = profileInfo?.avatarUrl
    ? profileInfo.avatarUrl
    : `https://api.dicebear.com/7.x/bottts/svg?seed=${encodeURIComponent(avatarSeed || "gomoku")}`;

  const scoreDisplay = formatNumber(stats.score);
  const gamesDisplay = formatNumber(stats.gamesPlayed);
  const winsDisplay = formatNumber(stats.wins);
  const computedWinRate =
    stats.winRate != null
      ? stats.winRate
      : stats.wins != null && stats.gamesPlayed
      ? (stats.wins / stats.gamesPlayed) * 100
      : null;
  const winRateDisplay = formatPercentage(computedWinRate);
  const rankLabel =
    stats.rank != null ? `#${formatNumber(stats.rank)}` : "Unranked";
  const xpValue = stats.xpPercent != null ? stats.xpPercent : 0;
  const xpBarPercent = sanitizePercentage(xpValue) ?? 0;
  const xpLabel = stats.xpPercent != null ? `${Math.round(xpBarPercent)}%` : "—";
  const displayLevel = stats.level != null ? stats.level : "—";

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-[#F1F1F1] dark:bg-[#0D1B2A] text-[#2F2F2F] dark:text-gray-100 transition-colors duration-500">
      <BackgroundAnimation stoneCount={24} intensity={10} />

      <div className="relative z-10 flex flex-col items-center px-4 py-12">
        <h1 className="text-4xl sm:text-5xl font-extrabold text-center mb-6 bg-gradient-to-r from-[#003D7C] to-[#EF7C00] bg-clip-text text-transparent">
          Player Profile
        </h1>
        <p className="text-gray-600 dark:text-gray-400 text-center text-sm sm:text-base mb-6">
          {loading
            ? "Loading player information..."
            : profileInfo
            ? `Insights for ${profileInfo.nickname}`
            : "Player details not available."}
        </p>

        {error && (
          <div className="w-full max-w-3xl mb-6 text-sm sm:text-base text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 px-4 py-3 rounded-lg text-center">
            {error}
          </div>
        )}

        <SectionCard className="w-full max-w-3xl mb-8 bg-white dark:bg-[#0F2538] shadow-lg rounded-xl p-6">
          <div className="flex flex-col sm:flex-row justify-around items-center gap-6 mb-6">
            <div className="flex flex-col items-center">
              <div className="relative w-32 h-32 rounded-full overflow-hidden border-4 border-[#EF7C00] shadow-md bg-white">
                {avatarUrl ? (
                  <img src={avatarUrl} alt="avatar" className="w-full h-full object-cover" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-gray-200 dark:bg-gray-700 text-4xl">
                    🎯
                  </div>
                )}
              </div>
              <span className="mt-3 text-lg font-semibold text-center">
                {profileInfo?.nickname || fallbackNickname}
              </span>
              <span className="text-xs text-gray-500 dark:text-gray-400">
                ID: {profileInfo?.userId || normalizedId}
              </span>
              {profileInfo?.country && (
                <span className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                  {/* Render flag only for ISO codes; otherwise just show the country name */}
                  {getFlag(profileInfo.country) && (
                    <span className="mr-1">{getFlag(profileInfo.country)}</span>
                  )}
                  {profileInfo.country}
                </span>
              )}
            </div>

            <div className="flex flex-col w-full sm:w-1/2">
              <h2 className="text-lg font-semibold mb-2 text-center sm:text-left">
                Level {displayLevel}
              </h2>
              <div className="w-full bg-gray-300 dark:bg-gray-700 rounded-full h-4 overflow-hidden">
                <div
                  className="bg-gradient-to-r from-[#EF7C00] to-[#FFD166] h-4 transition-all duration-700"
                  style={{ width: `${xpBarPercent}%` }}
                ></div>
              </div>
              <p className="text-xs mt-2 text-gray-600 dark:text-gray-400 text-center sm:text-left">
                {stats.xpPercent != null
                  ? `${xpLabel} to next level`
                  : "Level progress not tracked yet."}
              </p>
              <p className="text-xs text-gray-600 dark:text-gray-400 text-center sm:text-left">
                Rank: <span className="font-semibold text-[#EF7C00]">{rankLabel}</span>
              </p>
              {seasonPlacement != null && (
                <p className="text-xs text-gray-600 dark:text-gray-400 text-center sm:text-left">
                  Seasonal placement: <span className="font-semibold">#{formatNumber(seasonPlacement)}</span>
                </p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-center">
            <Stat label="Score" value={scoreDisplay} color="from-[#EF7C00] to-[#FFD166]" />
            <Stat label="Games Played" value={gamesDisplay} color="from-[#003D7C] to-[#3A86FF]" />
            <Stat label="Wins" value={winsDisplay} color="from-[#1B998B] to-[#38B000]" />
            <Stat label="Win Rate" value={winRateDisplay} color="from-[#EF476F] to-[#FF9B1A]" />
          </div>
        </SectionCard>

        {/* <SectionCard className="w-full max-w-3xl mb-8 bg-white dark:bg-[#0F2538] shadow-lg rounded-xl p-6 text-sm text-gray-600 dark:text-gray-300">
          <h2 className="text-lg font-semibold mb-3 text-[#003D7C] dark:text-[#FFD166]">About this player</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <p className="font-semibold text-gray-700 dark:text-gray-200">Country</p>
              <p>{profileInfo?.country ? `${getFlag(profileInfo.country)} ${profileInfo.country}` : "Not specified"}</p>
            </div>
            <div>
              <p className="font-semibold text-gray-700 dark:text-gray-200">Joined</p>
              <p>{formatDate(profileInfo?.joinedAt)}</p>
            </div>
            <div className="sm:col-span-2">
              <p className="font-semibold text-gray-700 dark:text-gray-200">Bio</p>
              <p>{profileInfo?.bio ? profileInfo.bio : "No bio shared yet."}</p>
            </div>
          </div>
        </SectionCard> */}

        <button
          onClick={() => navigate(-1)}
          className="px-5 py-2 rounded-md bg-gradient-to-r from-[#003D7C] to-[#004C99] hover:from-[#EF7C00] hover:to-[#FF9B1A] text-white font-semibold shadow-md transition-all"
        >
          Back
        </button>
      </div>
    </div>
  );
}
