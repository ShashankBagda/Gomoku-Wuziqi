import React, {useEffect, useRef, useState} from "react";
import SectionCard from "../components/SectionCard";
import Navbar from "../components/Navbar";
import BackgroundAnimation from "../components/BackgroundAnimation";
import {Link, useNavigate} from "react-router-dom";
import {toast} from "react-toastify";
import {lobbyApi, matchApi, playerApi, communicationApi} from "../api";
import {getAuthData, isAuthenticated} from "../utils/auth";

export default function Home() {
  const navigate = useNavigate();
  const [roomCode, setRoomCode] = useState("");
  const [creating, setCreating] = useState(false);
  const [joining, setJoining] = useState(false);
  const [matching, setMatching] = useState(false);
  const [matchingMode, setMatchingMode] = useState(null); // "casual" or "ranked"
  const matchPollIntervalRef = useRef(null);
  const joinPollIntervalRef = useRef(null);
  const [onlineCount, setOnlineCount] = useState(null);
  const onlinePollRef = useRef(null);

  // Cleanup polling intervals on unmount
  useEffect(() => {
    return () => {
      if (matchPollIntervalRef.current) {
        clearInterval(matchPollIntervalRef.current);
      }
      if (joinPollIntervalRef.current) {
        clearInterval(joinPollIntervalRef.current);
      }
      if (onlinePollRef.current) {
        clearInterval(onlinePollRef.current);
      }
    };
  }, []);

  // Check player status on mount
  useEffect(() => {
    const checkPlayerStatus = async () => {
      if (!isAuthenticated()) {
        return; // Not logged in, skip status check
      }

      try {
        const status = await playerApi.getPlayerStatus();

        // Check if player is in a matching queue
        if (status?.matchingStatus?.inQueue) {
          const mode = status.matchingStatus.mode;
          toast.info(`Resuming ${mode} match search...`);
          setMatching(true);
          setMatchingMode(mode);

          const playerId = resolvePlayerId();
          if (playerId) {
            // Start polling for match status
            matchPollIntervalRef.current = setInterval(() => {
              pollMatchStatus(mode, playerId);
            }, 2000);
          }
          return;
        }

        // Check if player is in a room
        if (status?.roomStatus?.inRoom) {
          const { roomCode: code, roomId, status: roomStatus } = status.roomStatus;

          if (roomStatus === "matched" && roomId) {
            // Room is already matched, navigate directly to game
            toast.info("Rejoining your game room...");
            navigate(`/room/${roomId}`, {
              state: { roomCode: code, roomId },
            });
          } else if (roomStatus === "waiting") {
            // Room is waiting for another player, resume polling
            toast.info("Resuming room join...");
            setJoining(true);
            setRoomCode(code);

            const playerId = resolvePlayerId();
            if (playerId) {
              // Start polling for join status
              joinPollIntervalRef.current = setInterval(() => {
                pollJoinRoomStatus(code, playerId);
              }, 2000);
            }
          }
        }
      } catch (err) {
        console.error("Failed to check player status:", err);
        // Don't show error toast on status check failure to avoid annoying the user
      }
    };

    checkPlayerStatus();
  }, []);

  // Live online users counter (polls backend every 10s)
  useEffect(() => {
    let cancelled = false;
    const fetchCount = async () => {
      try {
        const result = await communicationApi.fetchOnlineCount();
        // result can be either number or {count}; normalize to number
        const value = typeof result === "number" ? result : (result?.count ?? null);
        if (!cancelled) setOnlineCount(value);
      } catch (e) {
        if (!cancelled) setOnlineCount(null);
        // Silent fail; not critical to UX
      }
    };
    // initial fetch quickly, then poll
    fetchCount();
    onlinePollRef.current = setInterval(fetchCount, 10000);
    return () => {
      cancelled = true;
      if (onlinePollRef.current) {
        clearInterval(onlinePollRef.current);
        onlinePollRef.current = null;
      }
    };
  }, []);

  const resolvePlayerId = () => {
    if (!isAuthenticated()) {
      toast.error("Please sign in to host or join a room.");
      navigate("/login");
      return null;
    }
    const { userId } = getAuthData();
    if (!userId) {
      toast.error("Missing user session. Please sign in again.");
      navigate("/login");
      return null;
    }
    return userId;
  };

  const stopMatchPolling = () => {
    if (matchPollIntervalRef.current) {
      clearInterval(matchPollIntervalRef.current);
      matchPollIntervalRef.current = null;
    }
  };

  const stopJoinPolling = () => {
    if (joinPollIntervalRef.current) {
      clearInterval(joinPollIntervalRef.current);
      joinPollIntervalRef.current = null;
    }
  };

  const handleMatchSuccess = (roomId, roomCode, players) => {
    stopMatchPolling();
    setMatching(false);
    setMatchingMode(null);
    toast.success("Matched with an opponent! Redirecting to game...");
    navigate(`/room/${roomId}`, {
      state: {
        roomCode,
        roomId,
        players,
        fromMatching: true,
      },
    });
  };

  const pollMatchStatus = async (mode, playerId) => {
    try {
      const result = await matchApi.startMatch(mode, playerId);
      const status = String(result?.status || "").toLowerCase();

      if (status === "matched" && result?.roomId) {
        handleMatchSuccess(result.roomId, result.roomCode, result.players);
      }
      // If status is "waiting", continue polling
    } catch (err) {
      console.error("Match polling error:", err);
      stopMatchPolling();
      setMatching(false);
      setMatchingMode(null);
      toast.error(err?.message || "Matching failed. Please try again.");
    }
  };

  const handleStartMatch = async (mode) => {
    const playerId = resolvePlayerId();
    if (!playerId) return;

    setMatching(true);
    setMatchingMode(mode);
    toast.info(`Searching for ${mode} match...`);

    try {
      // Initial match request
      const result = await matchApi.startMatch(mode, playerId);
      const status = String(result?.status || "").toLowerCase();

      if (status === "matched" && result?.roomId) {
        handleMatchSuccess(result.roomId, result.roomCode, result.players);
        return;
      }

      // Start polling
      matchPollIntervalRef.current = setInterval(() => {
        pollMatchStatus(mode, playerId);
      }, 2000); // Poll every 2 seconds
    } catch (err) {
      setMatching(false);
      setMatchingMode(null);
      toast.error(err?.message || "Failed to start matching.");
    }
  };

  const handleCancelMatch = async () => {
    stopMatchPolling();
    setMatching(false);
    setMatchingMode(null);

    // Call cancel match API
    try {
      await matchApi.cancelMatch(resolvePlayerId());
      toast.info("Match search cancelled.");
    } catch (err) {
      console.error("Failed to cancel match:", err);
      toast.info("Match search cancelled locally.");
    }
  };

  const pollJoinRoomStatus = async (code, playerId) => {
    try {
      const result = await lobbyApi.joinRoom(code, playerId);
      const status = String(result?.status || "").toLowerCase();

      if (status.includes("not")) {
        stopJoinPolling();
        setJoining(false);
        toast.error("Room not found or expired.");
        return;
      }

      if (status === "matched" && result?.roomId) {
        stopJoinPolling();
        setJoining(false);
        toast.success("Matched with an opponent! Redirecting to game...");
        navigate(`/room/${result.roomId}`, {
          state: {
            roomCode: code,
            roomId: result.roomId,
            players: result.players,
            fromLobby: true,
          },
        });
      }
      // If status is "waiting", continue polling
    } catch (err) {
      console.error("Join room polling error:", err);
      stopJoinPolling();
      setJoining(false);
      toast.error(err?.message || "Failed to join room.");
    }
  };

  const handleGenerateCode = async () => {
    const playerId = resolvePlayerId();
    if (!playerId) return;

    setCreating(true);
    try {
      const creation = await lobbyApi.createRoom(playerId);
      const generatedCode = creation?.roomCode;
      if (!generatedCode) {
        throw new Error("Failed to generate room code. Please try again.");
      }
      setRoomCode(generatedCode);
      const joinResult = await lobbyApi.joinRoom(generatedCode, playerId);
      const status = String(joinResult?.status || "waiting").toLowerCase();

      if (status === "matched" && joinResult?.roomId) {
        toast.success(`Room ${generatedCode} created and matched!`);
        navigate(`/room/${joinResult.roomId}`, {
          state: {
            roomCode: generatedCode,
            roomId: joinResult.roomId,
            players: joinResult.players,
            fromLobby: true,
          },
        });
        return;
      }

      toast.success(`Room ${generatedCode} created. Share the code and wait for an opponent.`);
      setJoining(true);

      // Start polling for join status
      joinPollIntervalRef.current = setInterval(() => {
        pollJoinRoomStatus(generatedCode, playerId);
      }, 2000);
    } catch (err) {
      toast.error(err?.message || "Failed to create a room.");
      setCreating(false);
    } finally {
      setCreating(false);
    }
  };

  const handleJoinRoom = async () => {
    const code = roomCode.trim();
    if (code.length !== 6) {
      toast.error("Please enter a valid 6-digit room code.");
      return;
    }
    const playerId = resolvePlayerId();
    if (!playerId) return;

    setJoining(true);
    try {
      const result = await lobbyApi.joinRoom(code, playerId);
      const status = String(result?.status || "").toLowerCase();

      if (status.includes("not")) {
        toast.error("Room not found or expired.");
        setJoining(false);
        return;
      }

      if (status === "full") {
        toast.error("This room is already full.");
        setJoining(false);
        return;
      }

      if (status === "matched" && result?.roomId) {
        toast.success("Matched with an opponent! Redirecting to game...");
        navigate(`/room/${result.roomId}`, {
          state: {
            roomCode: code,
            roomId: result.roomId,
            players: result.players,
            fromLobby: true,
          },
        });
        setJoining(false);
        return;
      }

      // Status is "waiting", start polling
      toast.info("Waiting for another player to join...");
      joinPollIntervalRef.current = setInterval(() => {
        pollJoinRoomStatus(code, playerId);
      }, 2000);
    } catch (err) {
      toast.error(err?.message || "Failed to join the room.");
      setJoining(false);
    }
  };

  return (
    <div className="relative flex flex-col min-h-screen w-full overflow-hidden 
      bg-[#F1F1F1] dark:bg-gradient-to-b dark:from-[#0D1B2A] dark:via-[#0A1520] dark:to-[#0C1C2C]
      text-[#2F2F2F] dark:text-gray-100 transition-colors duration-500">

      {/* Animated background */}
      <BackgroundAnimation stoneCount={28} intensity={12} radialOpacity={0.14} />

      {/* Hero Header */}
      <section className="relative z-10 w-full 
        bg-gradient-to-r from-[#003D7C] to-[#00254C] 
        dark:from-[#0C2A45] dark:to-[#001B33] 
        text-white text-center py-6 sm:py-8 md:py-8 lg:py-10 px-4 sm:px-6 shadow-md overflow-hidden">
        
        <div className="absolute inset-0 opacity-20 bg-[radial-gradient(circle_at_center,rgba(239,124,0,0.4)_0%,transparent_60%)] animate-pulse" />

        {/* Navbar (responsive) */}
        <div className="relative z-20 mb-2 sm:mb-3">
          <Navbar />
        </div>

        <h1 className="text-4xl sm:text-5xl md:text-6xl font-extrabold mb-1 sm:mb-2 relative z-10 tracking-wide leading-snug px-2">
          Welcome to{" "}
          <span className="text-[#EF7C00] drop-shadow-[0_2px_8px_rgba(239,124,0,0.5)]">
            GOMOKU
          </span>
        </h1>
        <p className="text-base sm:text-lg md:text-xl font-light relative z-10 mb-4 sm:mb-5 px-3">
          Challenge opponents and rise to the top!
        </p>
        {/* Live online users indicator */}
        <div className="relative z-10 flex justify-center">
          <div className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1 text-sm sm:text-base">
            <span className="inline-block h-2.5 w-2.5 rounded-full bg-emerald-400 animate-pulse" />
            <span className="opacity-90">Players online:</span>
            <span className="font-semibold text-white">
              {onlineCount === null || onlineCount === undefined ? "—" : onlineCount}
            </span>
          </div>
        </div>
      </section>

      {/* Main Content */}
      <section className="relative z-10 flex flex-col flex-grow items-center justify-center w-full max-w-5xl mx-auto px-4 py-12 space-y-10">
        
        {/* 🟠 Public Room */}
        <SectionCard
          title="Public Room"
          className="text-center bg-white/90 dark:bg-[#0F2538]/70 shadow-lg hover:shadow-[#EF7C00]/20 dark:hover:shadow-[#EF7C00]/30 transition-all duration-300 rounded-xl backdrop-blur-sm">

          <p className="mb-6 text-gray-600 dark:text-gray-300">
            Join global matchmaking and play ranked or casual matches.
          </p>
        <div className="flex flex-col sm:flex-row justify-around items-center gap-4">
          {matching ? (
            <div className="flex flex-col items-center gap-4">
              <p className="text-lg font-semibold text-[#EF7C00] animate-pulse">
                Searching for {matchingMode} match...
              </p>
              <button
                onClick={handleCancelMatch}
                className="w-44 sm:w-48 font-semibold border-2 border-red-500 text-red-500 rounded-md px-6 py-3
                  hover:bg-red-500 hover:text-white active:bg-red-600 transition-all duration-300 shadow-sm hover:shadow-red-500/40"
              >
                ❌ Cancel Search
              </button>
            </div>
          ) : (
            <>
              <button
                onClick={() => handleStartMatch("ranked")}
                disabled={joining || creating}
                className="w-44 sm:w-48 font-semibold border-2 border-[#EF7C00] text-[#EF7C00] rounded-md px-6 py-3
                  hover:bg-[#EF7C00] hover:text-white active:bg-[#EF7C00]/90 transition-all duration-300 shadow-sm hover:shadow-[#EF7C00]/40 disabled:opacity-60 disabled:cursor-not-allowed"
              >
                🥇 Join Ranked
              </button>
              <button
                onClick={() => handleStartMatch("casual")}
                disabled={joining || creating}
                className="w-44 sm:w-48 font-semibold border-2 border-[#003D7C] text-[#003D7C] dark:border-[#4B8CD9] dark:text-[#4B8CD9] rounded-md px-6 py-3
                  hover:bg-[#003D7C] hover:text-white dark:hover:bg-[#4B8CD9] dark:hover:text-black active:scale-95 transition-all duration-300 shadow-sm hover:shadow-[#003D7C]/40 disabled:opacity-60 disabled:cursor-not-allowed"
              >
                🎯 Join Casual
              </button>
            </>
          )}
        </div>
        </SectionCard>

        {/* Practice vs AI */}
        <SectionCard
          title="Practice vs AI"
          className="text-center bg-white/90 dark:bg-[#0F2538]/70 shadow-lg hover:shadow-[#EF7C00]/20 dark:hover:shadow-[#EF7C00]/30 transition-all duration-300 rounded-xl backdrop-blur-sm">

          <p className="mb-6 text-gray-600 dark:text-gray-300">
            Play a quick local game against a GPT-powered AI.
          </p>
          <div className="flex justify-center">
            <button
              onClick={() => { if (!(joining || matching || creating)) navigate('/practice'); }}
              disabled={joining || matching || creating}
              className="inline-flex items-center gap-2 font-semibold border-2 border-emerald-600 text-emerald-600 rounded-md px-6 py-3
                hover:bg-emerald-600 hover:text-white active:bg-emerald-700 transition-all duration-300 shadow-sm hover:shadow-emerald-600/40 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              🤖 Start Practice
            </button>
          </div>
        </SectionCard>

        {/* 🔵 Private Room */}
        <SectionCard
          title="Private Room"
          className="text-center bg-white/90 dark:bg-[#0F2538]/70 shadow-lg hover:shadow-[#EF7C00]/20 dark:hover:shadow-[#EF7C00]/30 transition-all duration-300 rounded-xl backdrop-blur-sm">

          <p className="mb-6 text-gray-600 dark:text-gray-300">
            Create or join a private game using a 6-digit code.
          </p>

          {joining ? (
            <div className="flex flex-col items-center gap-4">
              <p className="text-lg font-semibold text-[#EF7C00] animate-pulse">
                Waiting for opponent to join room...
              </p>
              <div className="flex items-center gap-3 bg-gray-100 dark:bg-[#15293F] px-4 py-3 rounded-lg">
                <span className="font-mono text-2xl font-bold tracking-wider text-[#003D7C] dark:text-[#4B8CD9]">
                  {roomCode}
                </span>
                <button
                  onClick={() => {
                    navigator.clipboard.writeText(roomCode);
                    toast.success("Room code copied!");
                  }}
                  className="px-3 py-1.5 bg-[#EF7C00] text-white rounded-md hover:bg-[#D66D00] transition-all duration-200 text-sm font-semibold"
                  title="Copy room code"
                >
                  📋 Copy
                </button>
              </div>
              <button
                onClick={() => {
                  stopJoinPolling();
                  setJoining(false);
                  toast.info("Cancelled waiting for opponent.");
                }}
                className="w-44 sm:w-48 font-semibold border-2 border-red-500 text-red-500 rounded-md px-6 py-3
                  hover:bg-red-500 hover:text-white active:bg-red-600 transition-all duration-300 shadow-sm hover:shadow-red-500/40"
              >
                ❌ Cancel
              </button>
            </div>
          ) : (
            <>
              <div className="flex flex-col sm:flex-row justify-center items-center gap-4 w-full">
                <button
                  onClick={handleGenerateCode}
                  disabled={creating || matching}
                  className="w-44 sm:w-48 font-semibold border-2 border-[#003D7C] text-[#003D7C] dark:border-[#4B8CD9] dark:text-[#4B8CD9] rounded-md px-6 py-3
                    hover:bg-[#003D7C] hover:text-white dark:hover:bg-[#4B8CD9] dark:hover:text-black active:scale-95 transition-all duration-300 shadow-sm hover:shadow-[#003D7C]/40 disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {creating ? "Generating..." : "⚙️ Generate Code"}
                </button>

                <input
                  type="text"
                  maxLength={6}
                  value={roomCode}
                  onChange={(e) => setRoomCode(e.target.value.replace(/\D/g, ""))}
                  placeholder="Enter Code"
                  disabled={matching}
                  className="w-44 sm:w-48 text-center font-mono text-lg tracking-widest border-2 border-gray-300 dark:border-gray-600 rounded-md px-4 py-3 focus:outline-none focus:ring-2 focus:ring-[#EF7C00] bg-gray-50 dark:bg-[#15293F] dark:text-gray-100 transition-all disabled:opacity-60 disabled:cursor-not-allowed"
                />

                <button
                  onClick={handleJoinRoom}
                  disabled={matching}
                  className="w-44 sm:w-48 font-semibold border-2 border-[#EF7C00] text-[#EF7C00] rounded-md px-6 py-3
                    hover:bg-[#EF7C00] hover:text-white active:bg-[#EF7C00]/90 transition-all duration-300 shadow-sm hover:shadow-[#EF7C00]/40 disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  🚀 Join Room
                </button>
              </div>

              {roomCode && roomCode.length === 6 && !joining && (
                <p className="mt-4 text-sm text-gray-500 dark:text-gray-400">
                  Share this code with your friend:{" "}
                  <span className="font-bold text-[#EF7C00]">{roomCode}</span>
                </p>
              )}
            </>
          )}
        </SectionCard>
      </section>

      {/* Footer */}
      <footer className="relative z-10 mt-auto w-full py-4 text-center text-sm 
        text-gray-600 dark:text-gray-400 
        border-t border-gray-300 dark:border-[#1E324F] 
        bg-gray-100 dark:bg-[#0A1520]/80 backdrop-blur-sm">
          Made with ❤️ at NUS ISS <p></p>
        © {new Date().getFullYear()} Gomoku Multiplayer | <Link to="/credits" className="text-[#EF7C00] hover:underline">Game Development Credits</Link>
    </footer>
    </div>
  );
}
