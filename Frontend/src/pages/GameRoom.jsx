import React, {useCallback, useEffect, useMemo, useRef, useState,} from "react";
import {useLocation, useNavigate, useParams} from "react-router-dom";
import {toast} from "react-toastify";
import SectionCard from "../components/SectionCard";
import {useSettings} from "../context/SettingsContext";
import BackgroundAnimation from "../components/BackgroundAnimation";
import {
  Handshake,
  LogOut,
  Mic,
  MicOff,
  PlayCircle,
  RefreshCcw,
  RotateCcw,
  ShieldOff,
  Volume2,
  VolumeX,
  MessageCircle,
  Smile,
} from "lucide-react";
import {gameApi, lobbyApi, rankingApi, userApi} from "../api";
import {getAuthData, isAuthenticated} from "../utils/auth";
import {BOARD_DEFAULT_SIZE, findWinningSequence, mapBoardSnapshot,} from "../utils/board";
import createRtcChannel from "../utils/rtcChannel";
import createLivekitVoice from "../utils/voice/livekitClient";

const POLL_INTERVAL_MS = 500;

const statusLabel = (status) => {
  switch (status) {
    case "WAITING":
      return "Waiting for players";
    case "PLAYING":
      return "Game in progress";
    case "FINISHED":
      return "Game finished";
    default:
      return status || "Unknown";
  }
};

const colorLabel = (color) => {
  if (color === "black") return "Black";
  if (color === "white") return "White";
  return "TBD";
};

function hexToRgb(hex) {
  const normalized = hex?.replace("#", "");
  if (!normalized || normalized.length < 6) return { r: 255, g: 255, b: 255 };
  const bigint = parseInt(normalized.substring(0, 6), 16);
  return {
    r: (bigint >> 16) & 255,
    g: (bigint >> 8) & 255,
    b: bigint & 255,
  };
}

function isColorDark(hex) {
  const { r, g, b } = hexToRgb(hex);
  // Perceived brightness
  const brightness = (r * 299 + g * 587 + b * 114) / 1000;
  return brightness < 140;
}

function stoneSurface(style, baseColor) {
  if (style === "pattern") {
    return `radial-gradient(circle at 30% 30%, ${baseColor} 0%, ${baseColor} 55%, rgba(0,0,0,0.45) 100%)`;
  }
  return baseColor;
}

function buildFallbackProfile(id) {
  const normalized = String(id ?? "").trim();
  return {
    userId: normalized,
    nickname: normalized ? `Player ${normalized}` : "Unknown Player",
    avatarUrl: null,
    country: null,
  };
}

function normalizePlayerProfile(raw, fallbackId) {
  if (!raw || typeof raw !== "object") {
    return buildFallbackProfile(fallbackId);
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
    buildFallbackProfile(userId).nickname;

  return {
    userId: String(userId ?? fallbackId),
    nickname: nickname || buildFallbackProfile(userId).nickname,
    avatarUrl:
      payload?.avatarUrl ??
      payload?.avatar ??
      payload?.avatar_url ??
      null,
    country: payload?.country ?? payload?.location ?? null,
  };
}


export default function GameRoom() {
  const {
    boardColor,
    blackStone,
    whiteStone,
    stoneStyle,
    blackEmoji,
    whiteEmoji,
    sound,
    sfxVolume,
  } = useSettings();
  const location = useLocation();
  const navigate = useNavigate();
  const params = useParams();
  const locationState = useMemo(() => location.state || {}, [location.state]);
  const useLiveKitFlag = useMemo(() => String(process.env.REACT_APP_USE_LIVEKIT || "").toLowerCase() === "true", []);

  // Get roomId from URL params (/room/:roomId) or from location state
  // Use string format to avoid precision loss for large numbers
  const roomId = useMemo(() => {
    if (params.roomId) {
      const id = String(params.roomId).trim();
      return /^\d+$/.test(id) ? id : null;
    }
    if (locationState.roomId) {
      const id = String(locationState.roomId).trim();
      return /^\d+$/.test(id) ? id : null;
    }
    return null;
  }, [params.roomId, locationState.roomId]);

  // Get roomCode from location state or URL query params
  const roomCode = useMemo(() => {
    if (locationState.roomCode) {
      return String(locationState.roomCode);
    }
    const searchParams = new URLSearchParams(location.search);
    return searchParams.get("room") || "";
  }, [locationState.roomCode, location.search]);

  // Get playerId from auth data or location state, use string to avoid precision loss
  const authData = getAuthData();
  const derivedPlayerId = locationState.playerId || authData.userId || null;
  const playerId = derivedPlayerId ? String(derivedPlayerId).trim() : null;

  const [gameState, setGameState] = useState(null);
  const [lobbyStatus, setLobbyStatus] = useState("waiting");
  const [knownPlayers, setKnownPlayers] = useState([]);
  const [playerProfiles, setPlayerProfiles] = useState({});
  const [loadingGame, setLoadingGame] = useState(true);
  const [gameError, setGameError] = useState("");

  const [hoverCell, setHoverCell] = useState(null);
  const [isMuted, setIsMuted] = useState(!sound);
  const [chatOpen, setChatOpen] = useState(false);
  const [emoteOpen, setEmoteOpen] = useState(false);
  const [hasUnread, setHasUnread] = useState(false);
  const [micOn, setMicOn] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [emote, setEmote] = useState(null);
  const [voiceConnected, setVoiceConnected] = useState(false);
  const [micEnabled, setMicEnabled] = useState(false);
  const [matchReward, setMatchReward] = useState(null);

  const [readying, setReadying] = useState(false);
  const [surrendering, setSurrendering] = useState(false);
  const [requestingUndo, setRequestingUndo] = useState(false);
  const [proposingDraw, setProposingDraw] = useState(false);
  const [restarting, setRestarting] = useState(false);
  const [pendingDrawRequest, setPendingDrawRequest] = useState(false);
  const [pendingUndoRequest, setPendingUndoRequest] = useState(false);
  const [pendingRestartRequest, setPendingRestartRequest] = useState(false);

  const pollTimerRef = useRef(null);
  const pollCallbackRef = useRef(() => {});
  const lastOutcomeRef = useRef(null);
  const communicationRef = useRef(null);
  const rtcRef = useRef(null);
  const processedMessageIdsRef = useRef(new Set());
  const emoteTimeoutRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const voiceStreamRef = useRef(null);
  const lastDrawProposerRef = useRef(null);
  const lastUndoRequesterRef = useRef(null);

  // Redirect if missing critical info
  useEffect(() => {
    if (!roomId && !roomCode) {
      toast.error("Missing room information. Returning home.");
      navigate("/");
    }
  }, [roomId, roomCode, navigate]);

  useEffect(() => {
    if (!playerId) {
      if (!isAuthenticated()) {
        toast.error("Please sign in to play Gomoku.");
        navigate("/login");
      } else {
        toast.error("Unable to determine your player ID. Returning home.");
        navigate("/");
      }
    }
  }, [playerId, navigate]);

  useEffect(() => {
    setLobbyStatus(locationState.lobbyStatus || "waiting");
    setKnownPlayers(
      Array.isArray(locationState.players) ? locationState.players : []
    );
    setPlayerProfiles({});
    setGameState(null);
    setMessages([]);
    setEmote(null);
    setHoverCell(null);
  }, [locationState, roomCode, roomId]);

  useEffect(() => {
    setIsMuted(!sound);
  }, [sound]);

  // Manual refresh function - triggers immediate poll without waiting for timer
  const refreshGameState = useCallback(() => {
    if (pollCallbackRef.current) {
      // Clear current timer
      if (pollTimerRef.current) {
        clearTimeout(pollTimerRef.current);
      }
      // Trigger immediate poll (which will reschedule itself)
      pollCallbackRef.current();
    }
  }, []);

  const resolveProfile = useCallback(
      (id) => {
        const key = String(id ?? "");
        if (!key) {
          return buildFallbackProfile("");
        }
        return playerProfiles[key] || buildFallbackProfile(key);
      },
      [playerProfiles]
  );

  // Determine current player's color, use string comparison to avoid precision issues
  const playerColor = useMemo(() => {
    if (!gameState || !playerId) return null;
    if (gameState.blackPlayerId && String(gameState.blackPlayerId) === String(playerId)) {
      return "black";
    }
    if (gameState.whitePlayerId && String(gameState.whitePlayerId) === String(playerId)) {
      return "white";
    }
    return null;
  }, [gameState, playerId]);

  // Detect pending draw or undo requests from opponent
  useEffect(() => {
    if (!gameState || !playerId || !playerColor) return;

    // Check for draw proposal
    const drawProposer = gameState?.drawProposerColor;
    if (drawProposer && drawProposer !== playerColor.toUpperCase()) {
      // Opponent proposed draw
      const drawKey = `${roomId}-${gameState.version}-${drawProposer}`;
      if (lastDrawProposerRef.current !== drawKey) {
        lastDrawProposerRef.current = drawKey;
        setPendingDrawRequest(true);
      }
    } else {
      if (pendingDrawRequest) {
        setPendingDrawRequest(false);
      }
    }

    // Check for undo request from lastAction
    const lastAction = gameState?.lastAction;
    if (lastAction?.type === "UNDO") {
      const requester = lastAction.playerId;
      if (requester && String(requester) !== String(playerId)) {
        // Opponent requested undo
        const undoKey = `${roomId}-${gameState.version}-${requester}-${lastAction.timestamp}`;
        if (lastUndoRequesterRef.current !== undoKey) {
          lastUndoRequesterRef.current = undoKey;
          setPendingUndoRequest(true);
        }
      }
    } else {
      if (pendingUndoRequest) {
        setPendingUndoRequest(false);
      }
    }

    // Check for restart request from lastAction
    if (lastAction?.type === "RESTART") {
      const requester = lastAction.playerId;
      if (requester && String(requester) !== String(playerId)) {
        // Opponent requested restart
        const restartKey = `${roomId}-${gameState.version}-${requester}-${lastAction.timestamp}`;
        if (lastUndoRequesterRef.current !== restartKey) {
          lastUndoRequesterRef.current = restartKey;
          setPendingRestartRequest(true);
        }
      }
    } else {
      if (pendingRestartRequest) {
        setPendingRestartRequest(false);
      }
    }
  }, [gameState, playerId, playerColor, roomId, pendingDrawRequest, pendingUndoRequest, pendingRestartRequest, resolveProfile, refreshGameState]);

  useEffect(() => {
    const ids = Array.from(
      new Set(
        knownPlayers
          .filter((id) => id !== null && id !== undefined)
          .map((id) => String(id))
      )
    );
    const missing = ids.filter((id) => !playerProfiles[id]);
    if (!missing.length) {
      return;
    }

    let cancelled = false;
    const loadProfiles = async () => {
      try {
        const results = await Promise.all(
          missing.map(async (id) => {
            try {
              const data = await userApi.fetchPublicProfile(id);
              return [id, normalizePlayerProfile(data, id)];
            } catch (error) {
              return [id, buildFallbackProfile(id)];
            }
          })
        );
        if (cancelled) return;
        setPlayerProfiles((prev) => {
          const next = { ...prev };
          results.forEach(([id, profile]) => {
            next[id] = profile;
          });
          return next;
        });
      } catch (error) {
        if (!cancelled) {
          console.warn("Failed to load player profile(s)", error);
        }
      }
    };

    loadProfiles();
    return () => {
      cancelled = true;
    };
  }, [knownPlayers, playerProfiles]);

  // Poll backend for game state every 2 seconds
  useEffect(() => {
    if (!roomId || !playerId) {
      setLoadingGame(false);
      return;
    }

    let cancelled = false;

    const poll = async () => {
      try {
        const state = await gameApi.fetchGameState(roomId);
        if (cancelled) {
          return;
        }
        setGameState(state);
        setLobbyStatus((prev) => prev || "matched");
        const players = [];
        if (state?.blackPlayerId) {
          players.push(String(state.blackPlayerId));
        }
        if (state?.whitePlayerId) {
          players.push(String(state.whitePlayerId));
        }
        if (players.length > 0) {
          const next = Array.from(new Set(players));
          setKnownPlayers((prev) => {
            if (
              prev.length === next.length &&
              prev.every((value, index) => value === next[index])
            ) {
              return prev;
            }
            return next;
          });
        } else {
          setKnownPlayers((prev) => (prev.length ? [] : prev));
        }
        setGameError("");
      } catch (err) {
        if (cancelled) {
          return;
        }
        if (err?.status === 404) {
          // No game document yet; waiting for both players to ready
          setGameState(null);
          setGameError("");
        } else if (err?.status === 401 || err?.status === 403) {
          setGameError("Access denied. Please rejoin the room from the lobby.");
        } else {
          setGameError(err?.message || "Failed to load game state.");
        }
      } finally {
        if (!cancelled) {
          setLoadingGame(false);
          // Always schedule next poll to ensure continuous polling
          pollTimerRef.current = setTimeout(() => poll(), POLL_INTERVAL_MS);
        }
      }
    };

    // Store poll function in ref for manual refresh
    pollCallbackRef.current = poll;

    // Start polling immediately
    poll();

    return () => {
      cancelled = true;
      if (pollTimerRef.current) {
        clearTimeout(pollTimerRef.current);
      }
    };
  }, [roomId, playerId]);

  const boardSize = gameState?.currentState?.boardSize || BOARD_DEFAULT_SIZE;
  const boardMatrix = useMemo(
    () => mapBoardSnapshot(gameState?.currentState),
    [gameState]
  );

  const boardLineColor = useMemo(
    () =>
      isColorDark(boardColor)
        ? "rgba(255,255,255,0.3)"
        : "rgba(0,0,0,0.25)",
    [boardColor]
  );

  const currentTurnColor =
    gameState?.currentState?.currentTurn === "BLACK"
      ? "black"
      : gameState?.currentState?.currentTurn === "WHITE"
      ? "white"
      : null;

  const handleViewProfile = useCallback(
    (id) => {
      if (!id) return;
      navigate(`/players/${id}`, {
        state: {
          fromRoomId: roomId,
        },
      });
    },
    [navigate, roomId]
  );

  const uniqueKnownPlayers = useMemo(
    () =>
      Array.from(
        new Set(
          knownPlayers
            .filter((id) => id !== null && id !== undefined)
            .map((id) => String(id))
        )
      ),
    [knownPlayers]
  );

  const renderPlayerLabel = (id, accentClass) => {
    if (!id) {
      return "Waiting";
    }
    const profile = resolveProfile(id);
    return (
      <button
        type="button"
        onClick={() => handleViewProfile(profile.userId)}
        className={`underline-offset-4 hover:underline focus:underline ${accentClass}`}
        title={`View ${profile.nickname}'s profile`}
      >
        {profile.nickname}
      </button>
    );
  };

  const opponentColor =
    playerColor === "black" ? "white" : playerColor === "white" ? "black" : null;

  const playerReady =
    playerColor === "black"
      ? !!gameState?.blackReady
      : playerColor === "white"
      ? !!gameState?.whiteReady
      : false;

  const opponentReady =
    opponentColor === "black"
      ? !!gameState?.blackReady
      : opponentColor === "white"
      ? !!gameState?.whiteReady
      : false;

  const gameStatus = gameState?.status || "WAITING";

  const winnerCode = gameState?.currentState?.winner;
  const winnerLabel =
    winnerCode === 1
      ? "Black"
      : winnerCode === 2
      ? "White"
      : winnerCode === 0
      ? "Draw"
      : null;

  const isMyTurn =
    playerColor != null &&
    gameStatus === "PLAYING" &&
    currentTurnColor === playerColor;

  const canInteract = !!playerColor && gameStatus === "PLAYING";

  const winningSequence = useMemo(() => {
    if (winnerCode !== 1 && winnerCode !== 2) {
      return [];
    }
    const targetColor = winnerCode === 1 ? "black" : "white";
    return findWinningSequence(boardMatrix, targetColor);
  }, [boardMatrix, winnerCode]);

  const winningCells = useMemo(() => {
    if (!winningSequence.length) {
      return new Set();
    }
    return new Set(winningSequence.map((pos) => `${pos.row}-${pos.col}`));
  }, [winningSequence]);

  const boardCursorClass =
    canInteract && !loadingGame ? "cursor-none" : "cursor-not-allowed";

  // Track last opponent move to animate hint
  const prevMatrixRef = useRef(null);
  const [lastOppMove, setLastOppMove] = useState(null); // {r,c}
  useEffect(() => {
    const prev = prevMatrixRef.current;
    prevMatrixRef.current = boardMatrix;
    if (!prev || !boardMatrix) return;
    const n = boardMatrix.length;
    for (let r = 0; r < n; r++) {
      for (let c = 0; c < n; c++) {
        const before = prev?.[r]?.[c];
        const after = boardMatrix?.[r]?.[c];
        if (!before && after) {
          const placedByOpponent =
            (after === "black" && playerColor === "white") ||
            (after === "white" && playerColor === "black");
          if (placedByOpponent) {
            setLastOppMove({ r, c });
            setTimeout(() => setLastOppMove(null), 1400);
            return;
          }
        }
      }
    }
  }, [boardMatrix, playerColor]);

  const showEmote = useCallback((emoji) => {
    if (!emoji) return;
    setEmote(emoji);
    if (emoteTimeoutRef.current) {
      clearTimeout(emoteTimeoutRef.current);
    }
    emoteTimeoutRef.current = setTimeout(() => {
      setEmote(null);
    }, 1800);
  }, []);

  useEffect(() => {
    return () => {
      if (emoteTimeoutRef.current) {
        clearTimeout(emoteTimeoutRef.current);
      }
    };
  }, []);

  const appendMessage = useCallback((entry) => {
    const stamp = entry?.sentAt != null ? entry.sentAt : `${Date.now()}:${Math.random().toString(36).slice(2,8)}`;
    const id = `${entry.type}:${entry.senderId}:${stamp}`;
    if (processedMessageIdsRef.current.has(id)) {
      return;
    }
    processedMessageIdsRef.current.add(id);
    setMessages((prev) => {
      const next = [...prev, { ...entry, id }];
      if (next.length > 150) {
        next.shift();
      }
      return next;
    });
    if (!entry.isSelf && entry.type === "chat" && !chatOpen) {
      setHasUnread(true);
    }
  }, [chatOpen]);

  // Auto-scroll chat panes to the latest message
  const desktopChatRef = useRef(null);
  const modalChatRef = useRef(null);
  useEffect(() => {
    try {
      if (desktopChatRef.current) {
        desktopChatRef.current.scrollTop = desktopChatRef.current.scrollHeight;
      }
      if (chatOpen && modalChatRef.current) {
        modalChatRef.current.scrollTop = modalChatRef.current.scrollHeight;
      }
    } catch (_) {}
  }, [messages, chatOpen]);

  const remoteAudioRef = useRef(null);
  const playRemoteStream = useCallback((stream) => {
    try {
      const audio = new Audio();
      audio.srcObject = stream;
      audio.autoplay = true;
      audio.muted = isMuted || !sound;
      remoteAudioRef.current = audio;
      audio.play().catch(() => {});
    } catch (_e) {}
  }, [isMuted, sound]);

  useEffect(() => {
    if (remoteAudioRef.current) {
      remoteAudioRef.current.muted = isMuted || !sound;
    }
  }, [isMuted, sound]);

  useEffect(() => {
    if (!roomId || !playerId) {
      return undefined;
    }
    const { token } = getAuthData() || {};
    const useLiveKit = String(process.env.REACT_APP_USE_LIVEKIT || "").toLowerCase() === "true";

    let controller = null;
    if (useLiveKit) {
      const lk = createLivekitVoice({
        roomId,
        playerId,
        onConnected: () => setVoiceConnected(true),
        onDisconnected: () => { setVoiceConnected(false); setMicEnabled(false); },
        onMicChanged: (on) => setMicEnabled(!!on),
        onChat: ({ senderId, message, sentAt }) => {
          const isSelf = String(senderId) === String(playerId);
          const profile = resolveProfile(String(senderId));
          appendMessage({
            id: `chat:${senderId}:${sentAt}`,
            senderId,
            senderLabel: isSelf ? "You" : (profile?.nickname || `Player ${senderId}`),
            text: message || "",
            type: "chat",
            sentAt,
            isSelf,
          });
        },
        onEmote: (em) => showEmote(em),
      });
      lk.connect().catch(() => {});
      controller = {
        async setMic(on) { try { await lk.setMic(on); } catch (_) {} },
        async sendChat(message) { try { await lk.sendChat(message); } catch (_) {} },
        async sendEmote(em) { try { await lk.sendEmote(em); } catch (_) {} },
        close() { try { lk.disconnect(); } catch (_) {} },
      };
    } else {
      const rtc = createRtcChannel({
        roomId,
        playerId,
        token,
        onChat: ({ senderId, message, sentAt }) => {
          const isSelf = String(senderId) === String(playerId);
          const profile = resolveProfile(String(senderId));
          appendMessage({
            id: `chat:${senderId}:${sentAt}`,
            senderId,
            senderLabel: isSelf ? "You" : (profile?.nickname || `Player ${senderId}`),
            text: message || "",
            type: "chat",
            sentAt,
            isSelf,
          });
        },
        onEmote: (emote) => showEmote(emote),
        onPresence: () => {},
        onRemoteAudio: (stream) => playRemoteStream(stream),
      });
      controller = rtc;
    }
    rtcRef.current = controller;
    return () => {
      try { controller?.close(); } catch (_) {}
      rtcRef.current = null;
      setVoiceConnected(false);
      setMicEnabled(false);
    };
  }, [appendMessage, playerId, roomId, showEmote, playRemoteStream, resolveProfile]);

  useEffect(() => {
    if (winnerCode == null || winnerCode < 0) {
      lastOutcomeRef.current = null;
      return;
    }

    if (lastOutcomeRef.current === winnerCode) {
      return;
    }

    lastOutcomeRef.current = winnerCode;

    if (isMuted || !sound) {
      return;
    }

    const playerWon =
      (winnerCode === 1 && playerColor === "black") ||
      (winnerCode === 2 && playerColor === "white");

    const audioSrc =
      winnerCode === 0
        ? "/sounds/victory.mp3"
        : playerWon
        ? "/sounds/victory.mp3"
        : "/sounds/lose.mp3";

    const outcomeAudio = new Audio(audioSrc);
    const base = Math.min(1, Math.max(0, sfxVolume ?? 0.3));
    outcomeAudio.volume = playerWon ? Math.min(1, base + 0.4) : Math.min(1, base + 0.25);
    outcomeAudio.play().catch(() => {});
  }, [winnerCode, playerColor, sound, isMuted]);

  // Settle match when game finishes - only winner calls the API
  const settledMatchRef = useRef(null);
  const settlingRef = useRef(false);

  useEffect(() => {
    if (winnerCode == null || winnerCode < 0 || gameStatus !== "FINISHED") {
      return;
    }

    // Avoid settling the same match twice
    if (settledMatchRef.current === roomId) {
      return;
    }

    // Prevent concurrent settlement calls
    if (settlingRef.current) {
      return;
    }

    const blackPlayerId = gameState?.blackPlayerId;
    const whitePlayerId = gameState?.whitePlayerId;

    if (!roomId || !blackPlayerId || !whitePlayerId || !playerId) {
      return;
    }

    // Determine if current player should settle the match
    // Only the winner calls settle API (or black player in case of draw)
    const shouldSettle =
      winnerCode === 0
        ? String(playerId) === String(blackPlayerId) // Draw: black player settles
        : (winnerCode === 1 && String(playerId) === String(blackPlayerId)) || // Black wins
          (winnerCode === 2 && String(playerId) === String(whitePlayerId));  // White wins

    if (!shouldSettle) {
      console.log("Not the winner, skipping settlement call");
      return;
    }

    const settleMatch = async () => {
      // Mark as settling BEFORE making the API call
      settlingRef.current = true;
      settledMatchRef.current = roomId;

      try {
        // Determine winner and loser based on winnerCode
        // winnerCode: 0 = draw, 1 = black wins, 2 = white wins
        const settlementData = {
          matchId: roomId,
          winnerId: winnerCode === 0 ? null : winnerCode === 1 ? blackPlayerId : whitePlayerId,
          loserId: winnerCode === 0 ? null : winnerCode === 1 ? whitePlayerId : blackPlayerId,
          modeType: gameState.modeType || "CASUAL", // Get mode from game state, default to CASUAL
        };

        const response = await rankingApi.settleMatch(settlementData);
        console.log("Match settled successfully:", response);

        // Store reward data for display
        if (response) {
          // Determine which reward to show (for current player)
          const myReward = winnerCode === 0
            ? (String(playerId) === String(blackPlayerId) ? response.player1Reward : response.player2Reward)
            : (String(playerId) === String(response.winnerId) ? response.winnerReward : response.loserReward);

          if (myReward) {
            setMatchReward({
              ...myReward,
              modeType: response.modeType,
              isWinner: winnerCode !== 0 && String(playerId) === String(response.winnerId),
              isDraw: winnerCode === 0,
            });
          }
        }
      } catch (error) {
        console.error("Failed to settle match:", error);
        // Reset on error to allow retry
        settledMatchRef.current = null;
        // Don't show error to user as this is a background operation
      } finally {
        settlingRef.current = false;
      }
    };

    settleMatch();
  }, [winnerCode, gameStatus, roomId, gameState, gameState?.modeType, playerId, playerColor]);

  const handleToggleReady = async () => {
    if (!roomId || !playerId) return;
    setReadying(true);
    try {
      const state = await gameApi.readyUp(roomId, playerId);
      setGameState(state);
      // Use string comparison to check if player is ready
      const readyNow =
        (String(state.blackPlayerId) === String(playerId) && state.blackReady) ||
        (String(state.whitePlayerId) === String(playerId) && state.whiteReady);
      toast.success(readyNow ? "You are ready!" : "You are no longer ready.");
      refreshGameState();
    } catch (err) {
      toast.error(err?.message || "Failed to update ready status.");
    } finally {
      setReadying(false);
    }
  };

  const handleMove = async (row, col) => {
    if (!roomId || !playerId) return;
    if (!isMyTurn) {
      toast.info("Wait for your turn.");
      return;
    }
    if (boardMatrix[row]?.[col]) {
      return;
    }
    try {
      // Send coordinates: x=row, y=col (backend expects board[x][y] format)
      const state = await gameApi.makeMove(roomId, playerId, row, col);
      setGameState(state);
      refreshGameState();
      if (!isMuted && sound) {
        const a = new Audio("/sounds/place.wav");
        a.volume = Math.min(1, Math.max(0, sfxVolume ?? 0.3));
        a.play().catch(() => {});
      }
    } catch (err) {
      toast.error(err?.message || "Move failed.");
      refreshGameState();
    }
  };

  const handleSurrender = async () => {
    if (!roomId || !playerId) return;
    if (!window.confirm("Are you sure you want to surrender?")) {
      return;
    }
    setSurrendering(true);
    try {
      const state = await gameApi.surrender(roomId, playerId);
      setGameState(state);
      toast.info("You surrendered. Better luck next time!");
      refreshGameState();
    } catch (err) {
      toast.error(err?.message || "Failed to surrender.");
    } finally {
      setSurrendering(false);
    }
  };

  const handleProposeDraw = async () => {
    if (!roomId || !playerId) return;
    setProposingDraw(true);
    try {
      const state = await gameApi.proposeDraw(roomId, playerId);
      setGameState(state);
      toast.info("Draw proposal sent.");
      refreshGameState();
    } catch (err) {
      toast.error(err?.message || "Failed to propose a draw.");
    } finally {
      setProposingDraw(false);
    }
  };

  const handleRespondDraw = async (accept) => {
    if (!roomId || !playerId) return;
    try {
      const state = await gameApi.respondDraw(roomId, playerId, accept);
      setGameState(state);
      toast.success(accept ? "Draw accepted." : "Draw declined.");
      setPendingDrawRequest(false);
      lastDrawProposerRef.current = null;
      refreshGameState();
    } catch (err) {
      toast.error(err?.message || "Failed to respond to draw.");
    }
  };

  const handleRespondUndo = async (accept) => {
    if (!roomId || !playerId) return;
    try {
      const state = await gameApi.respondUndo(roomId, playerId, accept);
      setGameState(state);
      toast.success(accept ? "Undo approved." : "Undo declined.");
      setPendingUndoRequest(false);
      lastUndoRequesterRef.current = null;
      refreshGameState();
    } catch (err) {
      toast.error(err?.message || "Failed to respond to undo.");
    }
  };

  const handleRespondRestart = async (accept) => {
    if (!roomId || !playerId) return;
    try {
      const state = await gameApi.respondRestart(roomId, playerId, accept);
      setGameState(state);
      toast.success(accept ? "Restart accepted." : "Restart declined.");
      setPendingRestartRequest(false);
      lastUndoRequesterRef.current = null;
      refreshGameState();
    } catch (err) {
      toast.error(err?.message || "Failed to respond to restart.");
    }
  };

  const handleRequestUndo = async () => {
    if (!roomId || !playerId) return;
    setRequestingUndo(true);
    try {
      const state = await gameApi.requestUndo(roomId, playerId);
      setGameState(state);
      toast.info("Undo request sent.");
      refreshGameState();
    } catch (err) {
      toast.error(err?.message || "Failed to request undo.");
    } finally {
      setRequestingUndo(false);
    }
  };

  const stopVoiceTransmission = useCallback(() => {
    if (rtcRef.current) rtcRef.current.setMic(false);
    if (micOn) {
      setMicOn(false);
      toast.info("Voice channel disabled.");
    }
  }, [micOn]);

  const handleRestartGame = async () => {
    if (!roomId || !playerId || gameStatus !== "FINISHED") {
      return;
    }
    setRestarting(true);
    try {
      const state = await gameApi.requestRestart(roomId, playerId);
      setGameState(state);
      toast.info("Restart requested. Waiting for opponent to accept.");
      refreshGameState();
    } catch (err) {
      toast.error(err?.message || "Failed to restart the game.");
    } finally {
      setRestarting(false);
    }
  };

  const handleLeaveRoom = async () => {
    if (!roomCode || !playerId) {
      stopVoiceTransmission();
      navigate("/");
      return;
    }
    try {
      await lobbyApi.leaveRoom(roomCode, playerId);
    } catch (err) {
      console.warn("Failed to leave room gracefully:", err);
    } finally {
      if (rtcRef.current) rtcRef.current.setMic(false);
      navigate("/");
    }
  };

  const handleSendMessage = (e) => {
    e.preventDefault();
    const trimmed = input.trim();
    if (!trimmed) {
      return;
    }
    // Echo locally so you always see your own message instantly
    appendMessage({
      id: `local:${playerId}:${Date.now()}`,
      senderId: playerId,
      senderLabel: "You",
      text: trimmed,
      type: "chat",
      sentAt: Date.now(),
      isSelf: true,
    });
    if (rtcRef.current) {
      rtcRef.current.sendChat(trimmed);
    }
    setInput("");
  };

  const sendEmote = (emoji) => {
    if (!emoji) return;
    showEmote(emoji);
    rtcRef.current?.sendEmote(emoji);
  };

  const INTERSECTIONS = boardSize;
  const STEPS = INTERSECTIONS - 1;
  const STEP_PCT = 100 / STEPS;
  const EDGE_PADDING_PCT = STEP_PCT / 2;
  const HITBOX_PCT = STEP_PCT;

  useEffect(() => {
    return () => {
      if (rtcRef.current) rtcRef.current.setMic(false);
    };
  }, []);

  return (
    <div className="relative min-h-screen w-full bg-[#F1F1F1] dark:bg-[#0D1B2A] text-gray-900 dark:text-gray-100 overflow-hidden">
      <BackgroundAnimation stoneCount={30} intensity={14} />

      <div className="relative z-10 flex flex-col items-center pb-28 lg:pb-6">
        <h1 className="text-3xl sm:text-4xl font-extrabold my-4 bg-gradient-to-r from-[#003D7C] to-[#EF7C00] bg-clip-text text-transparent">
          Game Room
        </h1>

        <SectionCard className="w-full max-w-5xl mb-4 bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-md p-4 sm:p-6 lg:hidden">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 text-sm sm:text-base">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <span className="font-semibold text-[#003D7C] dark:text-[#FFD166]">
                  Room Code:
                </span>
                {roomCode ? (
                  <>
                    <span className="font-mono font-bold text-lg text-[#003D7C] dark:text-[#4B8CD9]">
                      {roomCode}
                    </span>
                    <button
                      onClick={() => {
                        navigator.clipboard.writeText(roomCode);
                        toast.success("Room code copied!");
                      }}
                      className="px-2 py-1 bg-[#EF7C00] text-white text-xs rounded hover:bg-[#D66D00] transition-all duration-200"
                      title="Copy room code"
                    >
                      📋
                    </button>
                  </>
                ) : (
                  <span>—</span>
                )}
              </div>
              <p>
                <span className="font-semibold text-[#003D7C] dark:text-[#FFD166]">
                  Status:
                </span>{" "}
                {gameError ? "Error" : statusLabel(gameStatus)}
              </p>
            </div>
            <div>
              <p>
                <span className="font-semibold">Your Color:</span>{" "}
                {colorLabel(playerColor)}
              </p>
              <p>
                <span className="font-semibold">Turn:</span>{" "}
                {gameStatus !== "PLAYING"
                  ? "—"
                  : currentTurnColor === "black"
                  ? "Black"
                  : currentTurnColor === "white"
                  ? "White"
                  : "—"}
              </p>
            </div>
            <div>
              <p>
                <span className="font-semibold">You Ready:</span>{" "}
                {playerReady ? "Yes" : "No"}
              </p>
              <p>
                <span className="font-semibold">Opponent Ready:</span>{" "}
                {opponentColor ? (opponentReady ? "Yes" : "No") : "—"}
              </p>
            </div>
            <div className="flex flex-col sm:flex-row gap-2">
              <button
                onClick={handleToggleReady}
                disabled={readying || !roomId || !playerId}
                className={`flex items-center justify-center gap-2 px-4 py-2 rounded-md font-semibold transition ${
                  playerReady
                    ? "bg-green-600 text-white hover:bg-green-700"
                    : "bg-[#003D7C] text-white hover:bg-[#EF7C00]"
                } disabled:opacity-60 disabled:cursor-not-allowed`}
              >
                <PlayCircle size={18} />
                {readying ? "Updating..." : playerReady ? "Ready ✅" : "Ready Up"}
              </button>
              <button
                onClick={handleLeaveRoom}
                className="flex items-center justify-center gap-2 px-4 py-2 rounded-md font-semibold bg-gray-200 dark:bg-gray-800 hover:bg-gray-300 dark:hover:bg-gray-700 transition"
              >
                <LogOut size={18} />
                Leave
              </button>
            </div>
          </div>
          {winnerLabel && (
            <div className="mt-3 text-center text-sm sm:text-base font-semibold text-[#EF7C00]">
              Result: {winnerLabel === "Draw" ? "It's a draw!" : `${winnerLabel} wins!`}
            </div>
          )}
          {gameError && (
            <div className="mt-3 text-center text-sm text-red-500">
              {gameError}
            </div>
          )}
          {/* Players (inline on mobile only) */}
          <div className="mt-3 text-sm text-gray-700 dark:text-gray-300 lg:hidden">
            <h3 className="font-semibold mb-1">Players</h3>
            <ul className="space-y-1">
              {uniqueKnownPlayers.length > 0 ? (
                uniqueKnownPlayers.map((id) => {
                  const profile = resolveProfile(id);
                  const isSelf = String(id) === String(playerId);
                  return (
                    <li key={id} className="flex items-center justify-between">
                      <button
                        type="button"
                        onClick={() => handleViewProfile(profile.userId)}
                        className="text-left underline-offset-4 hover:underline focus:underline text-[#003D7C] dark:text-[#FFD166]"
                        title={`View ${profile.nickname}'s profile`}
                      >
                        {profile.nickname || `Player ${id}`}
                      </button>
                      <span className="text-xs text-gray-500">{isSelf ? "You" : `#${profile.userId}`}</span>
                    </li>
                  );
                })
              ) : (
                <li>No players yet.</li>
              )}
            </ul>
          </div>
        </SectionCard>

        {/* Controls Bar removed to de-clutter; controls moved to panes below */}

        <div className="flex flex-col lg:flex-row gap-6 w-full max-w-6xl mt-6 px-3">
          {/* Left Column (desktop): compact room + players */}
          <div className="hidden lg:flex lg:w-[320px]">
            <SectionCard className="w-full bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-md p-3 text-sm">
              <div className="mb-2">
                <span className="font-semibold text-[#003D7C] dark:text-[#FFD166]">Room:</span>{" "}
                <span className="font-mono">{roomCode || "—"}</span>
              </div>
              <div className="mb-2">
                <span className="font-semibold">Status:</span> {statusLabel(gameStatus)}
              </div>
              <div className="mb-2">
                <span className="font-semibold">Players</span>
                <ul className="mt-1 space-y-1">
                  {uniqueKnownPlayers.map((id) => {
                    const profile = resolveProfile(id);
                    const isSelf = String(id) === String(playerId);
                    return (
                      <li key={id} className="flex items-center justify-between">
                        <button type="button" onClick={() => handleViewProfile(profile.userId)} className="text-left underline-offset-4 hover:underline focus:underline text-[#003D7C] dark:text-[#FFD166]">
                          {profile.nickname || `Player ${id}`}
                        </button>
                        <span className="text-xs text-gray-500">{isSelf ? "You" : `#${profile.userId}`}</span>
                      </li>
                    );
                  })}
                </ul>
              </div>
              <div className="flex gap-2">
                <button onClick={handleToggleReady} disabled={readying || !roomId || !playerId} className={`flex-1 px-3 py-1.5 rounded-md text-sm font-semibold transition ${playerReady ? "bg-green-600 text-white" : "bg-[#003D7C] text-white hover:bg-[#EF7C00]"}`}>
                  {readying ? "Updating..." : playerReady ? "Ready ✅" : "Ready Up"}
                </button>
                <button onClick={handleLeaveRoom} className="px-3 py-1.5 rounded-md text-sm font-semibold bg-gray-200 dark:bg-gray-800 hover:bg-gray-300 dark:hover:bg-gray-700">Leave</button>
              </div>

              {/* Comms + actions */}
              <div className="mt-3 grid grid-cols-2 gap-2">
                <button onClick={() => { if (micOn) { rtcRef.current?.setMic(false); setMicOn(false); toast.info("Voice channel disabled."); } else { rtcRef.current?.setMic(true); setMicOn(true); toast.success("Voice channel enabled."); } }} className={`flex items-center justify-center gap-2 px-3 py-1.5 rounded border ${micOn ? 'bg-[#EF7C00] text-white' : 'bg-white dark:bg-gray-800'}`}>
                  {micOn ? <Mic size={16}/> : <MicOff size={16}/>}<span>Mic</span>
                </button>
                <button onClick={() => setIsMuted(!isMuted)} className={`flex items-center justify-center gap-2 px-3 py-1.5 rounded border ${isMuted ? 'bg-white dark:bg-gray-800' : 'bg-[#EF7C00] text-white'}`}>
                  {isMuted ? <VolumeX size={16}/> : <Volume2 size={16}/>}<span>Speaker</span>
                </button>
                <button onClick={handleRequestUndo} disabled={!canInteract || requestingUndo} className="flex items-center justify-center gap-2 px-3 py-1.5 rounded border disabled:opacity-60">
                  <RotateCcw size={16}/><span>Undo</span>
                </button>
                <button onClick={handleProposeDraw} disabled={!canInteract || proposingDraw} className="flex items-center justify-center gap-2 px-3 py-1.5 rounded border disabled:opacity-60">
                  <Handshake size={16}/><span>Draw</span>
                </button>
                <button onClick={handleSurrender} disabled={surrendering || !canInteract} className="flex items-center justify-center gap-2 px-3 py-1.5 rounded border disabled:opacity-60 col-span-2">
                  <ShieldOff size={16}/><span>Surrender</span>
                </button>
                {gameStatus === 'FINISHED' && (
                  <button onClick={handleRestartGame} disabled={restarting} className="flex items-center justify-center gap-2 px-3 py-1.5 rounded border disabled:opacity-60 col-span-2">
                    <RefreshCcw size={16}/><span>{restarting ? 'Restarting...' : 'Restart'}</span>
                  </button>
                )}
              </div>
            </SectionCard>
          </div>

          {/* Game Board */}
          <div className="flex-1 flex flex-col items-center">
            <SectionCard className="w-full bg-white/90 dark:bg-[#0F2538] p-3 sm:p-4 rounded-xl shadow-md flex flex-col items-center">
              {/* Turn badge (outside board) */}
              {gameStatus === "PLAYING" && (
                <div className="mb-2 text-center text-xs font-semibold">
                  {isMyTurn ? (
                    <span className="px-3 py-0.5 rounded-full bg-white/90 dark:bg-gray-800/90 border border-gray-300 dark:border-gray-700 text-green-600">Your Turn</span>
                  ) : (
                    <span className="px-3 py-0.5 rounded-full bg-white/90 dark:bg-gray-800/90 border border-gray-300 dark:border-gray-700 text-gray-600 dark:text-gray-300">Opponent Turn</span>
                  )}
                </div>
              )}

              <div
                className={`relative aspect-square w-full max-w-[600px] rounded-lg shadow-inner border-2 dark:border-gray-700 ${boardCursorClass}`}
                style={{
                  padding: `${EDGE_PADDING_PCT}%`,
                  backgroundColor: boardColor,
                }}
              >
                <div
                  className="absolute inset-0"
                  style={{
                    backgroundImage: `linear-gradient(to right, ${boardLineColor} 1px, transparent 1px), linear-gradient(to bottom, ${boardLineColor} 1px, transparent 1px)`,
                    backgroundSize: `calc(100% / ${STEPS}) calc(100% / ${STEPS})`,
                  }}
                >
                  {boardMatrix.map((row, rIdx) =>
                    row.map((cell, cIdx) => {
                      const left = (cIdx / STEPS) * 100;
                      const top = (rIdx / STEPS) * 100;
                      const isHovered =
                        hoverCell &&
                        hoverCell[0] === rIdx &&
                        hoverCell[1] === cIdx;
                      const cellKey = `${rIdx}-${cIdx}`;
                      const isWinningStone = winningCells.has(cellKey);
                      return (
                        <div
                          key={`${rIdx}-${cIdx}`}
                          onClick={() => handleMove(rIdx, cIdx)}
                          onMouseEnter={() => setHoverCell([rIdx, cIdx])}
                          onMouseLeave={() => setHoverCell(null)}
                          className={`absolute flex items-center justify-center ${
                            !canInteract || loadingGame
                              ? "cursor-not-allowed opacity-70"
                              : "cursor-none"
                          }`}
                          style={{
                            left: `${left}%`,
                            top: `${top}%`,
                            width: `${HITBOX_PCT}%`,
                            height: `${HITBOX_PCT}%`,
                            transform: "translate(-50%, -50%)",
                          }}
                        >
                          {!cell && isHovered && canInteract && (
                            stoneStyle === "emoji" ? (
                              <div
                                className="flex items-center justify-center text-3xl"
                                style={{ opacity: 0.7 }}
                              >
                                {playerColor === "black" ? blackEmoji : whiteEmoji}
                              </div>
                            ) : (
                              <div
                                className="rounded-full opacity-60"
                                style={{
                                  width: "70%",
                                  height: "70%",
                                  background: stoneSurface(
                                    stoneStyle,
                                    playerColor === "black" ? blackStone : whiteStone
                                  ),
                                  border:
                                    stoneStyle === "classic"
                                      ? "1px solid rgba(0,0,0,0.25)"
                                      : "none",
                                }}
                              />
                            )
                          )}
                          {cell && (
                            <div className="relative flex items-center justify-center w-full h-full">
                              {(() => {
                                if (!lastOppMove) return null;
                                const match = lastOppMove.r === rIdx && lastOppMove.c === cIdx;
                                if (!match) return null;
                                return (
                                  <>
                                    <div
                                      className="absolute rounded-full animate-ping"
                                      style={{ width: "85%", height: "85%", backgroundColor: "rgba(59,130,246,0.25)", zIndex: 0, pointerEvents: "none" }}
                                    />
                                    <div
                                      className="absolute rounded-full"
                                      style={{ width: "75%", height: "75%", border: "2px solid rgba(59,130,246,0.9)", boxShadow: "0 0 14px rgba(59,130,246,0.6)", zIndex: 0, pointerEvents: "none" }}
                                    />
                                  </>
                                );
                              })()}
                              {isWinningStone && (
                                <>
                                  <div
                                    className="absolute rounded-full animate-ping"
                                    style={{
                                      width: "90%",
                                      height: "90%",
                                      backgroundColor: "rgba(250, 204, 21, 0.25)",
                                      zIndex: 0,
                                      pointerEvents: "none",
                                    }}
                                  />
                                  <div
                                    className="absolute rounded-full"
                                    style={{
                                      width: "78%",
                                      height: "78%",
                                      border: "2px solid rgba(250, 204, 21, 0.9)",
                                      boxShadow: "0 0 18px rgba(250, 204, 21, 0.65)",
                                      zIndex: 0,
                                      pointerEvents: "none",
                                    }}
                                  />
                                </>
                              )}
                              {stoneStyle === "emoji" ? (
                                <div
                                  className="flex items-center justify-center text-3xl"
                                  style={{ position: "relative", zIndex: 1 }}
                                >
                                  {cell === "black" ? blackEmoji : whiteEmoji}
                                </div>
                              ) : (
                                <div
                                  className="rounded-full"
                                  style={{
                                    width: "70%",
                                    height: "70%",
                                    background: stoneSurface(
                                      stoneStyle,
                                      cell === "black" ? blackStone : whiteStone
                                    ),
                                    boxShadow: `0 0 10px ${
                                      cell === "black" ? blackStone : whiteStone
                                    }80`,
                                    border:
                                      stoneStyle === "classic"
                                        ? "1px solid rgba(0,0,0,0.25)"
                                        : "none",
                                    position: "relative",
                                    zIndex: 1,
                                  }}
                                />
                              )}
                            </div>
                          )}
                        </div>
                      );
                    })
                  )}
                </div>
              </div>

              <div className="flex justify-between mt-3 w-full max-w-[600px] text-sm sm:text-base font-semibold">
                <span className="text-[#003D7C] dark:text-[#FFD166]">
                  ⚫ Black:{" "}
                  {renderPlayerLabel(
                    gameState?.blackPlayerId,
                    "text-[#003D7C] dark:text-[#FFD166]"
                  )}
                </span>
                <span className="text-[#EF7C00]">
                  ⚪ White:{" "}
                  {renderPlayerLabel(
                    gameState?.whitePlayerId,
                    "text-[#EF7C00]"
                  )}
                </span>
              </div>

              {loadingGame && (
                <div className="mt-3 text-sm text-gray-600 dark:text-gray-400">
                  Loading latest game state...
                </div>
              )}

              {gameStatus === "WAITING" && !loadingGame && (
                <div className="mt-3 text-sm text-gray-600 dark:text-gray-400 text-center">
                  Waiting for both players to ready up.
                </div>
              )}

              {/* Match Reward Display */}
              {matchReward && gameStatus === "FINISHED" && (
                <div className="mt-4 w-full max-w-[600px] bg-gradient-to-br from-[#003D7C]/10 to-[#EF7C00]/10 dark:from-[#003D7C]/20 dark:to-[#EF7C00]/20 rounded-xl p-4 border-2 border-[#EF7C00]/30 animate-fade-in">
                  <div className="text-center mb-3">
                    <h3 className="text-lg font-bold text-[#003D7C] dark:text-[#FFD166]">
                      {matchReward.isDraw ? "🤝 Draw - Match Rewards" : matchReward.isWinner ? "🎉 Victory Rewards!" : "💪 Match Rewards"}
                    </h3>
                    <p className="text-xs text-gray-600 dark:text-gray-400 mt-1">
                      {matchReward.modeType === "RANKED" ? "⭐ Ranked Match" : "🎮 Casual Match"}
                    </p>
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    {/* Experience */}
                    <div className="bg-white/80 dark:bg-gray-800/80 rounded-lg p-3 text-center">
                      <div className="text-xs text-gray-600 dark:text-gray-400 mb-1">Experience</div>
                      <div className="text-2xl font-bold text-[#003D7C] dark:text-[#4B8CD9]">
                        {matchReward.expChange > 0 ? '+' : ''}{matchReward.expChange}
                      </div>
                      <div className="text-xs text-gray-500 mt-1">
                        Total: {matchReward.totalExp} XP
                      </div>
                    </div>

                    {/* Score */}
                    <div className="bg-white/80 dark:bg-gray-800/80 rounded-lg p-3 text-center">
                      <div className="text-xs text-gray-600 dark:text-gray-400 mb-1">Rating</div>
                      <div className={`text-2xl font-bold ${matchReward.scoreChange >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                        {matchReward.scoreChange > 0 ? '+' : ''}{matchReward.scoreChange}
                      </div>
                      <div className="text-xs text-gray-500 mt-1">
                        Total: {matchReward.totalScore}
                      </div>
                    </div>

                    {/* Level */}
                    {matchReward.leveledUp && (
                      <div className="col-span-2 bg-gradient-to-r from-yellow-100 to-orange-100 dark:from-yellow-900/30 dark:to-orange-900/30 rounded-lg p-3 text-center border-2 border-yellow-400/50 animate-level-up">
                        <div className="text-sm font-bold text-yellow-800 dark:text-yellow-300">
                          🌟 Level Up! {matchReward.oldLevel} → {matchReward.newLevel}
                        </div>
                      </div>
                    )}

                    {!matchReward.leveledUp && (
                      <div className="col-span-2 bg-white/60 dark:bg-gray-800/60 rounded-lg p-2 text-center">
                        <div className="text-xs text-gray-600 dark:text-gray-400">
                          Level {matchReward.newLevel}
                        </div>
                      </div>
                    )}
                  </div>

                  {matchReward.modeType === "CASUAL" && matchReward.scoreChange === 0 && (
                    <div className="mt-3 text-xs text-center text-gray-500 dark:text-gray-400">
                      💡 Play Ranked matches to earn rating points!
                    </div>
                  )}
                </div>
              )}

              {emote && (
                <div className="absolute inset-0 z-40 flex justify-center items-center text-6xl animate-bounce">
                  {emote}
                </div>
              )}
            </SectionCard>
          </div>

          {/* Right Column (desktop): chat + emotes always visible */}
          <div className="hidden lg:flex lg:w-[320px]">
            <div className="w-full flex flex-col gap-4">
              <SectionCard className="w-full bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-md p-3">
                <h2 className="text-lg font-semibold mb-2">Chat</h2>
                <div ref={desktopChatRef} className="h-64 overflow-y-auto border border-gray-300 dark:border-gray-700 rounded p-2 mb-2 bg-white dark:bg-gray-800">
                  {messages.map((m, idx) => {
                    const key = m.id || idx;
                    const isBlack = gameState?.blackPlayerId && String(m.senderId) === String(gameState.blackPlayerId);
                    const isWhite = gameState?.whitePlayerId && String(m.senderId) === String(gameState.whitePlayerId);
                    const colorClass = isBlack ? 'text-[#003D7C]' : isWhite ? 'text-[#EF7C00]' : m.isSelf ? 'text-[#2563eb]' : 'text-gray-600 dark:text-gray-300';
                    const resolved = m.isSelf ? { nickname: 'You' } : resolveProfile(String(m.senderId));
                    const label = resolved?.nickname || `Player ${m.senderId}`;
                    return (
                      <p key={key} className={`text-sm my-1 ${colorClass}`}>
                        {label}: {m.text}
                      </p>
                    );
                  })}
                </div>
                <form onSubmit={handleSendMessage} className="flex border rounded-lg overflow-hidden focus-within:ring-1 focus-within:ring-[#EF7C00]">
                  <input type="text" placeholder="Type..." value={input} onChange={(e) => setInput(e.target.value)} className="flex-1 px-2 py-1 text-sm dark:bg-gray-800 focus:outline-none" />
                  <button type="submit" className="bg-[#EF7C00] text-white flex items-center justify-center w-12 hover:bg-[#D66D00]">Send</button>
                </form>
              </SectionCard>

              <SectionCard className="w-full bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-md p-3 text-center">
                <h2 className="text-lg font-semibold mb-2">Emotes</h2>
                <div className="flex flex-wrap justify-center gap-3 text-2xl">
                  {["😀","😎","🔥","💪","😡","🎉","🤝","😢","🤔","👏"].map((emoji) => (
                    <button key={emoji} onClick={() => sendEmote(emoji)} className="transition-transform hover:scale-125">{emoji}</button>
                  ))}
                </div>
              </SectionCard>
            </div>
          </div>
        </div>

        {/* Draw Request Modal */}
        {pendingDrawRequest && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
              <div
                  className="bg-white dark:bg-[#0F2538] rounded-xl shadow-2xl p-6 max-w-md w-full mx-4 border-2 border-[#EF7C00]">
                <h3 className="text-xl font-bold mb-4 text-[#003D7C] dark:text-[#FFD166]">
                  Draw Proposal
                </h3>
                <p className="mb-6 text-gray-700 dark:text-gray-300">
                  Your opponent has proposed a draw. Do you agree to end the game in a draw?
                </p>
                <div className="flex gap-3 justify-end">
                  <button
                      onClick={() => handleRespondDraw(false)}
                      className="px-4 py-2 rounded-md font-semibold bg-gray-300 dark:bg-gray-700 hover:bg-gray-400 dark:hover:bg-gray-600 transition"
                  >
                    Decline
                  </button>
                  <button
                      onClick={() => handleRespondDraw(true)}
                      className="px-4 py-2 rounded-md font-semibold bg-[#EF7C00] text-white hover:bg-[#D66D00] transition"
                  >
                    Accept Draw
                  </button>
                </div>
              </div>
            </div>
        )}

        {/* Undo Request Modal */}
        {pendingUndoRequest && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
              <div
                  className="bg-white dark:bg-[#0F2538] rounded-xl shadow-2xl p-6 max-w-md w-full mx-4 border-2 border-[#EF7C00]">
                <h3 className="text-xl font-bold mb-4 text-[#003D7C] dark:text-[#FFD166]">
                  Undo Request
                </h3>
                <p className="mb-6 text-gray-700 dark:text-gray-300">
                  Your opponent wants to undo their last move. Do you agree?
                </p>
                <div className="flex gap-3 justify-end">
                  <button
                      onClick={() => handleRespondUndo(false)}
                      className="px-4 py-2 rounded-md font-semibold bg-gray-300 dark:bg-gray-700 hover:bg-gray-400 dark:hover:bg-gray-600 transition"
                  >
                    Decline
                  </button>
                  <button
                      onClick={() => handleRespondUndo(true)}
                      className="px-4 py-2 rounded-md font-semibold bg-[#EF7C00] text-white hover:bg-[#D66D00] transition"
                  >
                    Accept Undo
                  </button>
                </div>
              </div>
            </div>
        )}

        {/* Restart Request Modal */}
        {pendingRestartRequest && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
              <div
                  className="bg-white dark:bg-[#0F2538] rounded-xl shadow-2xl p-6 max-w-md w-full mx-4 border-2 border-[#EF7C00]">
                <h3 className="text-xl font-bold mb-4 text-[#003D7C] dark:text-[#FFD166]">
                  Restart Request
                </h3>
                <p className="mb-6 text-gray-700 dark:text-gray-300">
                  Your opponent wants to restart the game. Do you agree?
                </p>
                <div className="flex gap-3 justify-end">
                  <button
                      onClick={() => handleRespondRestart(false)}
                      className="px-4 py-2 rounded-md font-semibold bg-gray-300 dark:bg-gray-700 hover:bg-gray-400 dark:hover:bg-gray-600 transition"
                  >
                    Decline
                  </button>
                  <button
                      onClick={() => handleRespondRestart(true)}
                      className="px-4 py-2 rounded-md font-semibold bg-[#EF7C00] text-white hover:bg-[#D66D00] transition"
                  >
                    Accept Restart
                  </button>
                </div>
          </div>
        </div>
        )}

        {/* Spacer so the fixed mobile action bar doesn't cover content */}
        <div className="h-28 lg:hidden" />

        {/* Mobile Action Bar (mic/speaker/undo/draw/surrender/chat/emotes) */}
        <div className="lg:hidden fixed bottom-3 left-3 right-3 z-40">
          <div className="bg-white/95 dark:bg-[#0F2538]/95 backdrop-blur rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700">
            <div className="grid grid-cols-4 gap-2 p-2 text-[11px]">
              <button
                onClick={() => {
                  if (micOn) {
                    rtcRef.current?.setMic(false);
                    setMicOn(false);
                    toast.info("Voice channel disabled.");
                  } else {
                    rtcRef.current?.setMic(true);
                    setMicOn(true);
                    toast.success("Voice channel enabled.");
                  }
                }}
                className={`flex flex-col items-center justify-center gap-1 px-2 py-2 rounded border ${
                  micOn ? 'bg-[#EF7C00] text-white border-transparent' : 'bg-white dark:bg-gray-800'
                }`}
                title="Toggle microphone"
              >
                {micOn ? <Mic size={16}/> : <MicOff size={16}/>}
                <span>Mic</span>
              </button>

              <button
                onClick={() => setIsMuted(!isMuted)}
                className={`flex flex-col items-center justify-center gap-1 px-2 py-2 rounded border ${
                  isMuted ? 'bg-white dark:bg-gray-800' : 'bg-[#EF7C00] text-white border-transparent'
                }`}
                title="Toggle speaker"
              >
                {isMuted ? <VolumeX size={16}/> : <Volume2 size={16}/>}<span>Speaker</span>
              </button>

              <button
                onClick={handleRequestUndo}
                disabled={!canInteract || requestingUndo}
                className="flex flex-col items-center justify-center gap-1 px-2 py-2 rounded border disabled:opacity-60"
                title="Request undo"
              >
                <RotateCcw size={16}/><span>Undo</span>
              </button>

              <button
                onClick={handleProposeDraw}
                disabled={!canInteract || proposingDraw}
                className="flex flex-col items-center justify-center gap-1 px-2 py-2 rounded border disabled:opacity-60"
                title="Propose draw"
              >
                <Handshake size={16}/><span>Draw</span>
              </button>

              <button
                onClick={handleSurrender}
                disabled={surrendering || !canInteract}
                className="flex flex-col items-center justify-center gap-1 px-2 py-2 rounded border disabled:opacity-60 col-span-2"
                title="Surrender"
              >
                <ShieldOff size={16}/><span>Surrender</span>
              </button>

              <button
                onClick={() => { setChatOpen(true); setHasUnread(false); }}
                className="relative flex flex-col items-center justify-center gap-1 px-2 py-2 rounded border bg-white dark:bg-gray-800"
                title="Open chat"
              >
                <MessageCircle size={16}/><span>Chat</span>
                {hasUnread && (
                  <span className="absolute -top-1 -right-1 h-3 w-3 rounded-full bg-red-500 animate-pulse" />
                )}
              </button>

              <button
                onClick={() => setEmoteOpen(true)}
                className="flex flex-col items-center justify-center gap-1 px-2 py-2 rounded border bg-white dark:bg-gray-800"
                title="Open emotes"
              >
                <Smile size={16}/><span>Emotes</span>
              </button>
            </div>
          </div>
        </div>

        {/* Chat Modal */}
        {chatOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-white dark:bg-[#0F2538] rounded-xl shadow-2xl p-4 w-full max-w-lg mx-4">
              <div className="flex items-center justify-between mb-2">
                <h3 className="text-lg font-semibold">Chat</h3>
                <button className="px-2 py-1 rounded bg-gray-200 dark:bg-gray-700" onClick={() => { setChatOpen(false); setHasUnread(false); }}>Close</button>
              </div>
              <div ref={modalChatRef} className="h-64 overflow-y-auto border border-gray-300 dark:border-gray-700 rounded p-2 mb-2 bg-white dark:bg-gray-800">
                {messages.map((m, idx) => {
                  const key = m.id || idx;
                  const isBlack = gameState?.blackPlayerId && String(m.senderId) === String(gameState.blackPlayerId);
                  const isWhite = gameState?.whitePlayerId && String(m.senderId) === String(gameState.whitePlayerId);
                  const colorClass = isBlack
                    ? "text-[#003D7C]"
                    : isWhite
                    ? "text-[#EF7C00]"
                    : m.isSelf
                    ? "text-[#2563eb]"
                    : "text-gray-600 dark:text-gray-300";
                  const resolved = m.isSelf ? { nickname: 'You' } : resolveProfile(String(m.senderId));
                  const label = resolved?.nickname || `Player ${m.senderId}`;
                  return (
                    <p key={key} className={`text-sm my-1 ${colorClass}`}>
                      {label}: {m.text}
                    </p>
                  );
                })}
              </div>
              <form onSubmit={handleSendMessage} className="flex border rounded-lg overflow-hidden focus-within:ring-1 focus-within:ring-[#EF7C00]">
                <input type="text" placeholder="Type..." value={input} onChange={(e) => setInput(e.target.value)} className="flex-1 px-2 py-1 text-sm dark:bg-gray-800 focus:outline-none" />
                <button type="submit" className="bg-[#EF7C00] text-white flex items-center justify-center w-12 hover:bg-[#D66D00]">Send</button>
              </form>
            </div>
          </div>
        )}

        {/* Emotes Modal */}
        {emoteOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-white dark:bg-[#0F2538] rounded-xl shadow-2xl p-4 w-full max-w-md mx-4 text-center">
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-lg font-semibold">Emotes</h3>
                <button className="px-2 py-1 rounded bg-gray-200 dark:bg-gray-700" onClick={() => setEmoteOpen(false)}>Close</button>
              </div>
              <div className="flex flex-wrap justify-center gap-3 text-3xl">
                {["😀", "😎", "🔥", "💪", "😡", "🎉", "🤝", "😢", "🤔", "👏"].map((emoji) => (
                  <button key={emoji} onClick={() => { sendEmote(emoji); setEmoteOpen(false); }} className="transition-transform hover:scale-125">
                    {emoji}
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

// (legacy communication builder removed)
