import React, {useEffect, useMemo, useState} from "react";
import SectionCard from "../components/SectionCard";
import {CheckCircle, Edit2, Lock, Mail, User} from "lucide-react";
import Select from "react-select";
import countryList from "react-select-country-list";
import BackgroundAnimation from "../components/BackgroundAnimation";
import {rankingApi, userApi} from "../api";
import {addAuthChangeListener} from "../utils/authState";
import {useNavigate, useParams} from "react-router-dom";
import {toast} from "react-toastify";
import {createEmptyStats, formatNumber, formatPercentage, sanitizePercentage,} from "../utils/profileStats";

function findCountryOption(options, value) {
  if (!value || !Array.isArray(options)) return null;
  const trimmed = String(value).trim();
  if (!trimmed) return null;
  const lower = trimmed.toLowerCase();
  return (
    options.find((opt) => opt?.value?.toLowerCase() === lower) ||
    options.find((opt) => opt?.label?.toLowerCase() === lower) ||
    null
  );
}

function mapRankingProfile(rankingProfile) {
  if (!rankingProfile) {
    return createEmptyStats();
  }

  // Extract data from ranking profile response
  const level = rankingProfile?.level ?? null;
  const exp = rankingProfile?.exp ?? 0;
  const totalExp = rankingProfile?.exp ?? 0;
  const nextLevelExpRequired = rankingProfile?.nextLevelExpRequired ?? null;
  const expToNext = rankingProfile?.expToNext ?? 0;
  const progressPercent = rankingProfile?.progressPercent ?? 0;

  // Extract scores from different leaderboards
  const scores = rankingProfile?.scores || {};
  const totalScore = scores.total ?? 0;

  // Extract ranks from different leaderboards
  const ranks = rankingProfile?.ranks || {};
  const totalRank = ranks.TOTAL?.rank ?? null;

  // Game stats
  const totalGames = rankingProfile?.totalGames ?? 0;
  const winRateStr = rankingProfile?.winRate ?? "0%";
  const winRateNum = parseFloat(winRateStr.replace('%', '')) || 0;
  const wins = Math.round((winRateNum / 100) * totalGames);

  // Calculate XP percentage for progress bar
  const xpPercent = nextLevelExpRequired > 0
    ? ((exp / nextLevelExpRequired) * 100)
    : progressPercent;

  // Map all leaderboard ranks for display
  const mappedRanks = {};
  if (ranks.DAILY) {
    mappedRanks.daily = {
      rank: ranks.DAILY.rank,
      score: ranks.DAILY.score ?? scores.daily ?? 0,
      totalPlayers: ranks.DAILY.totalPlayers ?? 0,
    };
  }
  if (ranks.WEEKLY) {
    mappedRanks.weekly = {
      rank: ranks.WEEKLY.rank,
      score: ranks.WEEKLY.score ?? scores.weekly ?? 0,
      totalPlayers: ranks.WEEKLY.totalPlayers ?? 0,
    };
  }
  if (ranks.MONTHLY) {
    mappedRanks.monthly = {
      rank: ranks.MONTHLY.rank,
      score: ranks.MONTHLY.score ?? scores.monthly ?? 0,
      totalPlayers: ranks.MONTHLY.totalPlayers ?? 0,
    };
  }
  if (ranks.SEASONAL) {
    mappedRanks.seasonal = {
      rank: ranks.SEASONAL.rank,
      score: ranks.SEASONAL.score ?? scores.seasonal ?? 0,
      totalPlayers: ranks.SEASONAL.totalPlayers ?? 0,
    };
  }
  if (ranks.TOTAL) {
    mappedRanks.total = {
      rank: ranks.TOTAL.rank,
      score: ranks.TOTAL.score ?? scores.total ?? 0,
      totalPlayers: ranks.TOTAL.totalPlayers ?? 0,
    };
  }

  return {
    level,
    totalExp,
    xpPercent,
    score: totalScore,
    gamesPlayed: totalGames,
    wins,
    winRate: winRateNum,
    rank: totalRank,
    ranks: mappedRanks,
  };
}

export default function Profile() {
  const navigate = useNavigate();
  const {userId: urlUserId} = useParams(); // Get userId from URL
  const [avatarSeed, setAvatarSeed] = useState("gomoku");
  const [nickname, setNickname] = useState("");
  const [email, setEmail] = useState("");
  const [emailOtp, setEmailOtp] = useState("");
  const [emailOtpSent, setEmailOtpSent] = useState(false);
  const [country, setCountry] = useState({ label: "Singapore", value: "SG" });
  const [userId, setUserId] = useState("");
  const [showPopup, setShowPopup] = useState(false);
  const [stats, setStats] = useState(() => createEmptyStats());
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [profileError, setProfileError] = useState("");
  const [authVersion, setAuthVersion] = useState(0);

  // Determine if viewing own profile or another user's profile
  const isOwnProfile = !urlUserId;
  const targetUserId = urlUserId || (typeof window !== "undefined" && window?.localStorage ? localStorage.getItem("userId") : null);

  const options = useMemo(() => countryList().getData(), []);

  useEffect(() => {
    const handleAuthChange = () => {
      setAuthVersion((prev) => prev + 1);
    };

    const unsubscribe = addAuthChangeListener(handleAuthChange);
    window.addEventListener("storage", handleAuthChange);

    return () => {
      unsubscribe();
      window.removeEventListener("storage", handleAuthChange);
    };
  }, []);

  useEffect(() => {
    let cancelled = false;

    if (!targetUserId) {
      setProfileError("Please sign in to view your profile.");
      setStats(createEmptyStats());
      setUserId("");
      setNickname("");
      setEmail("");
      setAvatarSeed("gomoku");
      setLoadingProfile(false);
      return;
    }

    const loadProfile = async () => {
      setLoadingProfile(true);
      setProfileError("");
      try {
        // Only fetch ranking profile if viewing own profile
        const [userData, rankingProfile] = await Promise.all([
          userApi.fetchPublicProfile(targetUserId).catch((err) => {
            throw err;
          }),
          isOwnProfile
              ? rankingApi.getRankingProfile().catch((err) => {
                if (err?.status === 404) {
                  return null;
                }
                console.warn("Failed to load ranking profile:", err);
                return null;
              })
              : Promise.resolve(null), // Don't fetch ranking for other users
        ]);

        if (cancelled) {
          return;
        }

        console.log("[Profile] Received userData from API:", userData);

        if (userData) {
          if (userData.valid === false) {
            throw new Error("Session expired. Please sign in again.");
          }
          // Backend returns 'id' field, but we use 'userId' internally
          const userIdValue = userData.userId || userData.id;
          const profileNickname = userData.nickname || "";

          console.log("[Profile] Extracted values:");
          console.log("  - userIdValue:", userIdValue);
          console.log("  - profileNickname:", profileNickname);
          console.log("  - email:", userData.email);
          console.log("  - country:", userData.country);

          setNickname(profileNickname);
          setEmail(userData.email || "");
          setUserId(userIdValue ? String(userIdValue) : "");
          const seedSource = userData.nickname || userData.email;
          if (seedSource) {
            setAvatarSeed(seedSource);
          }
          if (userData.country) {
            const countryOption = findCountryOption(options, userData.country);
            if (countryOption) {
              setCountry(countryOption);
            }
          }

          console.log("[Profile] State set. nickname:", profileNickname, "email:", userData.email, "userId:", userIdValue);
        } else {
          console.log("[Profile] userData is null/undefined");
        }

        // Map ranking profile data to stats
        const mappedStats = mapRankingProfile(rankingProfile);
        setStats(mappedStats);
      } catch (err) {
        if (!cancelled) {
          setProfileError(err?.message || "Failed to load profile data.");
          setStats(createEmptyStats());
          setUserId("");
          setNickname("");
          setEmail("");
          setAvatarSeed("gomoku");
        }
      } finally {
        if (!cancelled) {
          setLoadingProfile(false);
        }
      }
    };

    loadProfile();
    return () => {
      cancelled = true;
    };
  }, [options, authVersion, targetUserId, isOwnProfile]);

  const regenerateAvatar = () => {
    const newSeed = Math.random().toString(36).substring(7);
    setAvatarSeed(newSeed);
  };

  // OTP handlers
  const sendOtp = () => {
    setEmailOtpSent(true);
    toast.success("OTP sent successfully!");
  };

  const verifyOtp = () => {
    if (emailOtp === "123456") {
      toast.success("EMAIL verified successfully!");
      setEmailOtp("");
      setEmailOtpSent(false);
    } else {
      toast.error("Invalid OTP");
    }
  };

  const handleSaveChanges = () => {
    if (loadingProfile || profileError) return;
    setShowPopup(true);
    setTimeout(() => setShowPopup(false), 2000);
  };

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
      <BackgroundAnimation stoneCount={26} intensity={11} />

      <div className="relative z-10 flex flex-col items-center px-4 py-10">
        <h1 className="text-4xl sm:text-5xl font-extrabold text-center mb-8 bg-gradient-to-r from-[#003D7C] to-[#EF7C00] bg-clip-text text-transparent">
          Profile
        </h1>

        {profileError && (
          <div className="w-full max-w-3xl mb-6 text-sm sm:text-base text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-700 px-4 py-3 rounded-lg text-center">
            {profileError}
          </div>
        )}

        {loadingProfile && !profileError && (
          <div className="w-full max-w-3xl mb-6 text-sm sm:text-base text-gray-600 dark:text-gray-400 bg-white/70 dark:bg-gray-900/30 border border-gray-200 dark:border-gray-700 px-4 py-3 rounded-lg text-center">
            Loading your latest stats...
          </div>
        )}

      {/* Avatar, Level & Stats Section */}
      <SectionCard
        title="Avatar, Level & Stats"
        className="w-full max-w-3xl mb-8 bg-white dark:bg-[#0F2538] shadow-lg rounded-xl p-6"
      >
        <div className="flex flex-col sm:flex-row justify-around items-center gap-6 mb-6">
          <div className="flex flex-col items-center">
            <div className="relative w-32 h-32 rounded-full overflow-hidden border-4 border-[#EF7C00] shadow-md">
              <img
                src={`https://api.dicebear.com/7.x/bottts/svg?seed=${avatarSeed}`}
                alt="avatar"
                className="w-full h-full"
              />
            </div>
            {isOwnProfile && (
                <button
                    onClick={regenerateAvatar}
                    className="mt-3 flex items-center gap-2 text-sm px-3 py-1 rounded-md bg-gradient-to-r from-[#003D7C] to-[#004C99] hover:from-[#EF7C00] hover:to-[#FF9B1A] text-white"
                >
                  <Edit2 size={14}/> Regenerate
                </button>
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
                : "Play ranked games to start earning XP."}
            </p>
            <p className="text-xs text-gray-600 dark:text-gray-400 text-center sm:text-left">
              Rank: <span className="font-semibold text-[#EF7C00]">{rankLabel}</span>
            </p>
            {stats.totalExp != null && (
              <p className="text-xs text-gray-600 dark:text-gray-400 text-center sm:text-left">
                Total EXP: <span className="font-semibold">{formatNumber(stats.totalExp)}</span>
              </p>
            )}
          </div>
        </div>

        {/* Stats inline */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-center">
          <Stat label="Score" value={scoreDisplay} color="from-[#EF7C00] to-[#FFD166]" />
          <Stat label="Games Played" value={gamesDisplay} color="from-[#003D7C] to-[#3A86FF]" />
          <Stat label="Wins" value={winsDisplay} color="from-[#1B998B] to-[#38B000]" />
          <Stat label="Win Rate" value={winRateDisplay} color="from-[#EF476F] to-[#FF9B1A]" />
        </div>
      </SectionCard>

      {/* Leaderboard Rankings Section */}
      {!loadingProfile && !profileError && stats.ranks && (
        <SectionCard
          title="Leaderboard Rankings"
          className="w-full max-w-3xl mb-8 bg-white dark:bg-[#0F2538] shadow-lg rounded-xl p-6"
        >
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {stats.ranks.daily && (
              <RankCard
                title="Daily"
                rank={stats.ranks.daily.rank}
                score={stats.ranks.daily.score}
                totalPlayers={stats.ranks.daily.totalPlayers}
                color="from-[#EF7C00] to-[#FFD166]"
              />
            )}
            {stats.ranks.weekly && (
              <RankCard
                title="Weekly"
                rank={stats.ranks.weekly.rank}
                score={stats.ranks.weekly.score}
                totalPlayers={stats.ranks.weekly.totalPlayers}
                color="from-[#003D7C] to-[#3A86FF]"
              />
            )}
            {stats.ranks.monthly && (
              <RankCard
                title="Monthly"
                rank={stats.ranks.monthly.rank}
                score={stats.ranks.monthly.score}
                totalPlayers={stats.ranks.monthly.totalPlayers}
                color="from-[#1B998B] to-[#38B000]"
              />
            )}
          </div>
        </SectionCard>
      )}

        {/* Account Section - Only show for own profile */}
        {isOwnProfile && (
            <>
              <SectionCard
                  title="Account Information"
                  className="w-full max-w-3xl mb-8 bg-white dark:bg-[#0F2538] shadow-lg rounded-xl p-6"
              >
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <InputField icon={<User size={16}/>} label="User ID" value={userId} readOnly/>
                  <InputField icon={<User size={16}/>} label="Nickname" value={nickname}
                              onChange={(e) => setNickname(e.target.value)}/>
                  <div className="flex flex-col">
                    <label className="text-xs mb-1 text-gray-500 dark:text-gray-400">Country</label>
                    <Select
                        options={options}
                        value={country}
                        onChange={setCountry}
                        formatOptionLabel={(opt) => (
                            <div className="flex items-center gap-2">
                              <span>{getFlag(opt.value)}</span>
                              <span>{opt.label}</span>
                            </div>
                        )}
                        styles={selectStyle}
                    />
                  </div>
                </div>
              </SectionCard>

              {/* Email */}
              <SectionCard
                  title="Email & Verification"
                  className="w-full max-w-3xl mb-8 bg-white dark:bg-[#0F2538] shadow-lg rounded-xl p-6"
              >
                <InputField
                    icon={<Mail size={16}/>}
                    label="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
                <OtpBlock
                    otp={emailOtp}
                    setOtp={setEmailOtp}
                    otpSent={emailOtpSent}
                    onSend={sendOtp}
                    onVerify={verifyOtp}
                />
              </SectionCard>

              {/* Password */}
              <SectionCard
                  title="Change Password"
                  className="w-full max-w-3xl mb-8 bg-white dark:bg-[#0F2538] shadow-lg rounded-xl p-6"
              >
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                  To change your password, you'll need to verify your email with a verification code for security.
                </p>
                <button
                    onClick={() => navigate("/reset-password", {state: {email}})}
                    className="flex items-center gap-2 px-4 py-2 rounded-md bg-gradient-to-r from-[#003D7C] to-[#004C99] hover:from-[#EF7C00] hover:to-[#FF9B1A] text-white font-semibold shadow-md transition-all"
                >
                  <Lock size={16}/>
                  Change Password
                </button>
              </SectionCard>

              {/* Save Changes Button */}
              <button
                  onClick={handleSaveChanges}
                  disabled={loadingProfile || !!profileError}
                  className="px-6 py-2 mb-8 rounded-md bg-gradient-to-r from-[#003D7C] to-[#004C99] hover:from-[#EF7C00] hover:to-[#FF9B1A] text-white font-semibold shadow-md disabled:opacity-60 disabled:cursor-not-allowed"
              >
                {loadingProfile ? "Loading..." : "Save Changes"}
              </button>
            </>
        )}

      {/* Confirmation Popup */}
      {showPopup && (
        <div className="fixed bottom-8 right-8 bg-green-600 text-white px-4 py-2 rounded-md shadow-lg flex items-center gap-2 animate-bounce">
          <CheckCircle size={18} /> Changes saved successfully!
        </div>
      )}
      </div>
    </div>
  );
}

/* Reusable OTP block */
function OtpBlock({ otp, setOtp, otpSent, onSend, onVerify }) {
  return (
    <div className="flex justify-end mt-3">
      {!otpSent ? (
        <button
          onClick={onSend}
          className="text-xs px-3 py-1 rounded-md bg-[#003D7C] hover:bg-[#EF7C00] text-white transition-all"
        >
          Send OTP
        </button>
      ) : (
        <div className="flex items-center gap-2">
          <input
            type="text"
            placeholder="Enter OTP"
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
            className="w-24 px-2 py-1 text-sm border rounded bg-white dark:bg-gray-800 dark:border-gray-700"
          />
          <button
            onClick={onVerify}
            className="text-xs px-3 py-1 rounded-md bg-[#EF7C00] text-white transition-all"
          >
            Verify
          </button>
        </div>
      )}
    </div>
  );
}

/* Input Field */
function InputField({ icon, label, value, onChange, type = "text", readOnly = false }) {
  return (
    <div className="flex flex-col mt-3">
      <label className="text-xs text-gray-500 dark:text-gray-400 mb-1">{label}</label>
      <div className="flex items-center border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 rounded-md px-3 py-2 focus-within:ring-2 focus-within:ring-[#EF7C00] transition-all">
        <span className="text-gray-500 dark:text-gray-400 mr-2">{icon}</span>
        <input
          type={type}
          value={value}
          readOnly={readOnly}
          onChange={onChange}
          className={`bg-transparent w-full outline-none text-sm ${readOnly ? "cursor-not-allowed opacity-70" : ""}`}
        />
      </div>
    </div>
  );
}

/* Flag helper */
function getFlag(code) {
  return String.fromCodePoint(...[...code.toUpperCase()].map((c) => 127397 + c.charCodeAt()));
}

/* react-select styles */
const selectStyle = {
  control: (base) => ({
    ...base,
    backgroundColor: "transparent",
    borderColor: "rgba(156,163,175,1)",
    color: "inherit",
    minHeight: "38px",
  }),
  menu: (base) => ({
    ...base,
    backgroundColor: "#fff",
    color: "#111",
    zIndex: 50,
  }),
  singleValue: (base) => ({
    ...base,
    color: "inherit",
  }),
};

/* Stat */
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

/* RankCard - Display leaderboard ranking */
function RankCard({ title, rank, score, totalPlayers, color }) {
  const rankDisplay = rank != null && rank > 0 ? `#${rank}` : "Unranked";
  const scoreDisplay = score != null ? score : 0;
  const playersDisplay = totalPlayers != null && totalPlayers > 0 ? `/ ${totalPlayers}` : "";

  return (
    <div
      className={`p-4 rounded-lg shadow-md border-2 border-gray-200 dark:border-gray-700 bg-gradient-to-br ${color} bg-opacity-20 hover:shadow-lg transition-all`}
    >
      <div className="text-sm font-bold text-gray-700 dark:text-gray-300 mb-2">{title}</div>
      <div className="flex items-baseline gap-2">
        <div className="text-2xl font-extrabold text-[#EF7C00] dark:text-[#FFD166]">
          {rankDisplay}
        </div>
        {playersDisplay && (
          <div className="text-xs text-gray-600 dark:text-gray-400">
            {playersDisplay}
          </div>
        )}
      </div>
      <div className="mt-2 text-xs text-gray-600 dark:text-gray-400">
        Score: <span className="font-semibold text-gray-800 dark:text-gray-200">{scoreDisplay}</span>
      </div>
    </div>
  );
}
