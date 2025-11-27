import React, {useEffect, useState} from "react";
import {Link} from "react-router-dom";
import {Medal, Star, Trophy} from "lucide-react";
import SectionCard from "../components/SectionCard";
import BackgroundAnimation from "../components/BackgroundAnimation";
import {rankingApi, userApi} from "../api";
import {addAuthChangeListener} from "../utils/authState";

const readAuthStatus = () => {
  if (typeof window === "undefined") {
    return false;
  }
  return (
    localStorage.getItem("loggedIn") === "1" &&
    !!localStorage.getItem("token") &&
    !!localStorage.getItem("userId")
  );
};

export default function Leaderboard() {
  const [category, setCategory] = useState("Daily");
  const [animatePodium, setAnimatePodium] = useState(false);
  const [showConfetti, setShowConfetti] = useState(false);
  const [podiumPlayers, setPodiumPlayers] = useState([]);
  const [leaderboardRows, setLeaderboardRows] = useState([]);
  const [userRank, setUserRank] = useState(null);
  const [currentUserId, setCurrentUserId] = useState(null);
  const [userProfiles, setUserProfiles] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const categories = ["Daily", "Weekly", "Monthly"];
  const [hasAuth, setHasAuth] = useState(() => readAuthStatus());
  const podiumFirst = podiumPlayers[0] || null;
  const podiumSecond = podiumPlayers[1] || null;
  const podiumThird = podiumPlayers[2] || null;
  const leaderboardEmpty = !loading && !error && leaderboardRows.length === 0;

  useEffect(() => {
    // Trigger animations when page loads
    setTimeout(() => {
      setAnimatePodium(true);
      setShowConfetti(true);
      setTimeout(() => setShowConfetti(false), 4000); // stop confetti after 4s
    }, 300);
  }, []);

  useEffect(() => {
    const handleAuthChange = () => {
      setHasAuth(readAuthStatus());
    };
    const unsubscribe = addAuthChangeListener(handleAuthChange);
    window.addEventListener("storage", handleAuthChange);
    return () => {
      unsubscribe();
      window.removeEventListener("storage", handleAuthChange);
    };
  }, []);

  useEffect(() => {
    // Get current user ID
    const userId = typeof window !== "undefined" && window?.localStorage
      ? localStorage.getItem("userId")
      : null;
    setCurrentUserId(userId);
  }, []);

  const loadUserProfiles = async (userIds) => {
    try {
      const profiles = await Promise.all(
        userIds.map(async (userId) => {
          try {
            const profile = await userApi.fetchPublicProfile(userId);
            return { userId, profile };
          } catch (error) {
            console.warn(`Failed to load profile for user ${userId}:`, error);
            return { userId, profile: null };
          }
        })
      );

      const profilesMap = {};
      profiles.forEach(({ userId, profile }) => {
        if (profile) {
          profilesMap[userId] = {
            nickname: profile.nickname || `Player ${userId}`,
            avatarUrl: profile.avatarUrl || null,
          };
        }
      });

      setUserProfiles(profilesMap);
    } catch (error) {
      console.error("Failed to load user profiles:", error);
    }
  };

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        // Leaderboard is a public API - no authentication required
        const leaderboardsData = await rankingApi.getTopLeaderboards().catch((err) => {
          throw err;
        });

        if (cancelled) {
          return;
        }

        // Determine best category to display if current is empty
        const catDataMap = {
          Daily: leaderboardsData?.daily,
          Weekly: leaderboardsData?.weekly,
          Monthly: leaderboardsData?.monthly,
        };

        const resolveTopList = (lb) => Array.isArray(lb?.topList) ? lb.topList : [];

        let selectedCategory = category;
        let currentLeaderboard = catDataMap[selectedCategory];
        let currentTopList = resolveTopList(currentLeaderboard);

        if (!currentLeaderboard || currentTopList.length === 0) {
          const preferredOrder = ["Daily", "Weekly", "Monthly"];
          const firstNonEmpty = preferredOrder.find((c) => resolveTopList(catDataMap[c]).length > 0);
          if (firstNonEmpty && firstNonEmpty !== selectedCategory) {
            selectedCategory = firstNonEmpty;
            currentLeaderboard = catDataMap[selectedCategory];
            currentTopList = resolveTopList(currentLeaderboard);
            // Update tab selection (triggers re-run but we already set data below for immediate UX)
            setCategory(firstNonEmpty);
          }
        }

        if (!currentLeaderboard) {
          throw new Error("No leaderboard data available");
        }

        // Extract top list
        const normalizedEntries = normalizeNewLeaderboardEntries(currentTopList || []);
        setLeaderboardRows(normalizedEntries);
        setPodiumPlayers(normalizedEntries.slice(0, 3));

        // Extract user rank (only available if logged in)
        if (hasAuth) {
          const meData = currentLeaderboard.me;
          const normalizedUser = normalizeNewUserRank(meData, currentLeaderboard.totalPlayers);
          setUserRank(normalizedUser);
        } else {
          setUserRank(null);
        }

        // Load user profiles for leaderboard entries
        const userIds = normalizedEntries
          .map(entry => entry.userId)
          .filter(id => id != null && id !== "");

        if (userIds.length > 0) {
          loadUserProfiles(userIds);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err?.message || "Failed to load leaderboard.");
          setLeaderboardRows([]);
          setPodiumPlayers([]);
          setUserRank(null);
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
  }, [category, hasAuth]);

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-[#F1F1F1] dark:bg-[#0D1B2A] text-[#2F2F2F] dark:text-gray-100">
      <BackgroundAnimation stoneCount={28} intensity={12} />
      {showConfetti && <ConfettiEffect />}

      <div className="relative z-10 flex flex-col items-center px-4 py-10">

        {/* Header */}
        <h1 className="text-4xl sm:text-5xl font-extrabold text-center mb-6 bg-gradient-to-r from-[#003D7C] to-[#EF7C00] bg-clip-text text-transparent">
          Leaderboard
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mb-8 text-center text-sm sm:text-base">
          Track your rank and compete for the top spot!
        </p>

        {error && (
          <div className="w-full max-w-4xl mb-6 text-sm sm:text-base text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Category Tabs */}
        <div className="flex flex-wrap justify-center gap-3 mb-8">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setCategory(cat)}
              className={`px-5 py-2 rounded-full border-2 font-semibold text-sm sm:text-base transition-all duration-300 ${
                category === cat
                  ? "border-[#EF7C00] text-[#EF7C00] bg-white dark:bg-[#0F2538]"
                  : "border-gray-400 text-gray-500 hover:border-[#EF7C00] hover:text-[#EF7C00]"
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

      {/* 🏆 Top 3 Podium */}
      <SectionCard
        title={`Top 3 Players (${category})`}
        className="w-full max-w-4xl mb-10 bg-white dark:bg-[#0F2538] shadow-lg rounded-xl p-6 sm:p-8 transition-all"
      >
        {loading ? (
          <LoadingState message="Loading leaderboard..." />
        ) : error ? (
          <EmptyState message={error} />
        ) : leaderboardEmpty ? (
          <EmptyState message="No leaderboard data available yet. Play some matches to get ranked!" />
        ) : (
          <div
            className={`flex flex-row justify-center items-end gap-3 sm:gap-6 text-center flex-wrap transition-transform duration-700 ${
              animatePodium ? "translate-y-0 opacity-100" : "translate-y-6 opacity-0"
            }`}
          >
            {/* 2nd */}
            <PodiumPlayer
              position={2}
              player={podiumSecond}
              userProfile={podiumSecond ? userProfiles[podiumSecond.userId] : null}
              height="h-28 sm:h-40 md:h-44"
              color="from-[#003D7C] to-[#1E6091]"
            />

            {/* 1st */}
            <PodiumPlayer
              position={1}
              player={podiumFirst}
              userProfile={podiumFirst ? userProfiles[podiumFirst.userId] : null}
              height="h-32 sm:h-48 md:h-56"
              color="from-[#EF7C00] to-[#FFD166]"
              glow
            />

            {/* 3rd */}
            <PodiumPlayer
              position={3}
              player={podiumThird}
              userProfile={podiumThird ? userProfiles[podiumThird.userId] : null}
              height="h-24 sm:h-36 md:h-40"
              color="from-[#6A040F] to-[#E85D04]"
            />
          </div>
        )}
      </SectionCard>

        {/* 🧍 Player Rank Bar (between podium and table) - Only show if user is ranked */}
        {hasAuth && userRank && userRank.rank != null && userRank.rank > 0 && (
            <footer
                className="w-full max-w-5xl bg-gradient-to-r from-[#003D7C] to-[#002B5C] dark:from-[#0F2538] dark:to-[#0C1B2C] text-white px-6 py-3 flex flex-col sm:flex-row items-center justify-between shadow-lg rounded-xl my-6">
              <div className="flex items-center gap-2 text-sm sm:text-base">
                <span className="font-semibold text-[#FFD166]">Your Rank:</span>
                <span className="font-bold text-white">
              #{userRank.rank}
            </span>
                {userRank.totalPlayers ? (
                    <span className="text-gray-200 text-sm">
                / {userRank.totalPlayers} Players
              </span>
                ) : null}
              </div>

              <div className="flex items-center gap-3 mt-2 sm:mt-0">
                <span className="font-semibold text-[#FFD166]">Score:</span>
                <span className="font-bold text-white">
              {userRank.score != null ? userRank.score : "—"}
            </span>
              </div>
            </footer>
        )}

      {/* 🪶 Top 50 Table */}
      <SectionCard
        title={`Top 50 - ${category}`}
        className="w-full max-w-5xl bg-white dark:bg-[#0F2538] shadow-lg rounded-xl p-4 sm:p-6"
      >
        {loading ? (
          <LoadingState message="Fetching top players..." />
        ) : error ? (
          <EmptyState message={error} />
        ) : leaderboardEmpty ? (
          <EmptyState message="No ranked players yet. Be the first to climb the leaderboard!" />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm border-collapse">
              <thead>
                <tr className="text-gray-500 dark:text-gray-300 border-b border-gray-200 dark:border-gray-700">
                  <th className="py-3 px-4">Rank</th>
                  <th className="py-3 px-4">Player</th>
                  <th className="py-3 px-4">Score</th>
                </tr>
              </thead>
              <tbody>
                {leaderboardRows.map((row, index) => {
                  const rank = row.rank ?? index + 1;
                  const profile = userProfiles[row.userId];
                  const name = profile?.nickname || `Player ${row.userId || rank}`;
                  const avatarUrl = profile?.avatarUrl;
                  const isCurrentUser = currentUserId && String(row.userId) === String(currentUserId);

                  return (
                    <tr
                      key={row.userId ?? row.id ?? rank}
                      className={`transition-colors ${
                        isCurrentUser
                          ? "bg-[#FFD166]/20 dark:bg-[#EF7C00]/20 border-l-4 border-[#EF7C00]"
                          : rank % 2 === 0
                          ? "bg-gray-50 dark:bg-[#102238]"
                          : "bg-white dark:bg-[#0C1B2C]"
                      } hover:bg-[#EF7C00]/10`}
                    >
                      <td className="py-3 px-4 font-semibold flex items-center gap-2">
                        {rank <= 3 ? <RankBadge rank={rank} /> : <span>{rank}</span>}
                        {isCurrentUser && <span className="text-xs text-[#EF7C00] font-bold">(You)</span>}
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2">
                          {avatarUrl && (
                            <img
                              src={avatarUrl}
                              alt={name}
                              className="w-8 h-8 rounded-full border border-gray-300 dark:border-gray-600"
                              onError={(e) => { e.target.style.display = 'none'; }}
                            />
                          )}
                          <Link
                              to={isCurrentUser ? "/profile" : `/profile/${row.userId}`}
                            className={`${
                              isCurrentUser
                                ? "text-[#EF7C00] dark:text-[#FFD166] font-bold"
                                : "text-[#003D7C] dark:text-[#FFD166]"
                            } hover:underline font-medium`}
                          >
                            {name}
                          </Link>
                        </div>
                      </td>
                      <td className={`py-3 px-4 font-semibold ${
                        isCurrentUser ? "text-[#EF7C00] font-extrabold" : "text-[#EF7C00]"
                      }`}>
                        {row.score != null ? row.score : "—"}
                      </td>
                    </tr>
                  );
                })}

                {/* Show user rank outside top 50 */}
                {userRank && userRank.rank > 50 && (
                  <>
                    <tr className="bg-gray-200 dark:bg-gray-700">
                      <td colSpan="3" className="py-2 text-center text-xs text-gray-500">
                        ...
                      </td>
                    </tr>
                    <tr className="bg-[#FFD166]/30 dark:bg-[#EF7C00]/30 border-l-4 border-[#EF7C00]">
                      <td className="py-3 px-4 font-semibold flex items-center gap-2">
                        <span>{userRank.rank}</span>
                        <span className="text-xs text-[#EF7C00] font-bold">(You)</span>
                      </td>
                      <td className="py-3 px-4">
                        <Link
                          to="/profile"
                          className="text-[#EF7C00] dark:text-[#FFD166] hover:underline font-bold"
                        >
                          Player {currentUserId}
                        </Link>
                      </td>
                      <td className="py-3 px-4 text-[#EF7C00] font-extrabold">
                        {userRank.score != null ? userRank.score : "—"}
                      </td>
                    </tr>
                  </>
                )}
              </tbody>
            </table>
          </div>
        )}
      </SectionCard>

      </div>
    </div>
  );
}

function normalizeNewLeaderboardEntries(entries) {
  if (!Array.isArray(entries)) {
    return [];
  }

  return entries.map((item, idx) => {
    const rank = item?.rank ?? idx + 1;
    const score = item?.score ?? 0;
    const userId = item?.userId ?? null;
    const name = `Player ${userId || rank}`;

    return {
      id: userId,
      userId,
      rank,
      score,
      name,
      raw: item,
    };
  });
}

function normalizeNewUserRank(meData, totalPlayers) {
  if (!meData) {
    return null;
  }

  return {
    rank: meData?.rank ?? 0,
    score: meData?.score ?? 0,
    nickname: null,
    totalPlayers: totalPlayers ?? null,
  };
}

function LoadingState({ message }) {
  return (
    <div className="py-6 text-center text-sm sm:text-base text-gray-500 dark:text-gray-400">
      {message}
    </div>
  );
}

function EmptyState({ message }) {
  return (
    <div className="py-6 text-center text-sm sm:text-base text-gray-500 dark:text-gray-400">
      {message}
    </div>
  );
}

/* 🥇 Podium Component */
function PodiumPlayer({ position, player, userProfile, height, color, glow = false }) {
  const medal =
    position === 1 ? (
      <Trophy className="text-[#FFD700]" size={26} />
    ) : position === 2 ? (
      <Medal className="text-[#C0C0C0]" size={22} />
    ) : (
      <Star className="text-[#CD7F32]" size={20} />
    );

  const displayName = userProfile?.nickname || player?.name || `Player ${player?.userId || position}`;
  const displayScore = player?.score == null ? "—" : player.score;
  const avatarUrl = userProfile?.avatarUrl;
  const shouldGlow = glow && !!player;
  const profileLink = player?.userId ? `/profile/${player.userId}` : null;

  const podiumContent = (
      <>
      {/* Glow behind #1 */}
      {shouldGlow && (
        <div className="absolute -top-6 sm:-top-8 w-16 sm:w-24 h-16 sm:h-24 bg-[#FFD700] opacity-20 rounded-full blur-2xl animate-pulse"></div>
      )}

      {/* Avatar above podium */}
      {avatarUrl && (
        <div className="mb-2">
          <img
            src={avatarUrl}
            alt={displayName}
            className="w-12 h-12 sm:w-16 sm:h-16 rounded-full border-4 border-white dark:border-gray-700 shadow-lg"
            onError={(e) => { e.target.style.display = 'none'; }}
          />
        </div>
      )}

      {/* Podium block */}
      <div
        className={`w-full ${height} rounded-t-xl bg-gradient-to-t ${color} flex flex-col justify-end items-center p-2 sm:p-3 shadow-lg`}
      >
        <div className="text-sm sm:text-base md:text-lg font-bold text-white drop-shadow truncate w-full text-center">
          {displayName}
        </div>
        <div className="text-xs text-white/90">Score: {displayScore}</div>
      </div>

      {/* Medal below block */}
      <div className="mt-1 sm:mt-2 flex items-center justify-center gap-1">
        {medal}
        <span className="text-xs sm:text-sm font-semibold">{position}</span>
      </div>
      </>
  );

  if (profileLink) {
    return (
        <Link
            to={profileLink}
            className={`flex flex-col items-center justify-end relative w-24 sm:w-28 md:w-36 ${
                shouldGlow ? "animate-glow" : ""
            } hover:scale-105 transition-transform cursor-pointer`}
        >
          {podiumContent}
        </Link>
    );
  }

  return (
      <div
          className={`flex flex-col items-center justify-end relative w-24 sm:w-28 md:w-36 ${
              shouldGlow ? "animate-glow" : ""
          }`}
      >
        {podiumContent}
    </div>
  );
}


/* 🎖 Rank Badge */
function RankBadge({ rank }) {
  const colors = {
    1: "bg-[#FFD700] text-black",
    2: "bg-[#C0C0C0] text-black",
    3: "bg-[#CD7F32] text-white",
  };
  return (
    <span
      className={`inline-block px-2 py-1 rounded-md text-xs font-bold ${colors[rank]} shadow-sm`}
    >
      #{rank}
    </span>
  );
}

/* 🎉 Confetti Animation */
function ConfettiEffect() {
  const confettiPieces = Array.from({ length: 60 });
  return (
    <div className="absolute inset-0 overflow-hidden pointer-events-none z-50">
      {confettiPieces.map((_, i) => (
        <div
          key={i}
          className="absolute w-2 h-2 bg-[#EF7C00] animate-fall"
          style={{
            left: `${Math.random() * 100}%`,
            animationDuration: `${2 + Math.random() * 3}s`,
            animationDelay: `${Math.random() * 1.5}s`,
            backgroundColor:
              i % 3 === 0
                ? "#FFD700"
                : i % 3 === 1
                ? "#EF7C00"
                : "#003D7C",
          }}
        ></div>
      ))}
    </div>
  );
}

/* 💫 Confetti Keyframes */
const style = document.createElement("style");
style.innerHTML = `
@keyframes fall {
  0% { transform: translateY(-10vh) rotate(0deg); opacity: 1; }
  100% { transform: translateY(110vh) rotate(720deg); opacity: 0; }
}
.animate-fall { animation-name: fall; animation-timing-function: linear; }
@keyframes glow {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}
.animate-glow { animation: glow 2s infinite ease-in-out; }
`;
document.head.appendChild(style);
