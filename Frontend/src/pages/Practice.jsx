import React, { useCallback, useMemo, useRef, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import SectionCard from "../components/SectionCard";
import BackgroundAnimation from "../components/BackgroundAnimation";
// Walkthrough disabled
import { useSettings } from "../context/SettingsContext";
import { aiApi } from "../api/ai";
import { rankingApi } from "../api";
import { getAuthData, isAuthenticated } from "../utils/auth";
import { BOARD_DEFAULT_SIZE, findWinningSequence } from "../utils/board";

const N = BOARD_DEFAULT_SIZE;

function toIntBoard(board) {
  return board.map((row) => row.map((v) => (v === "black" ? 1 : v === "white" ? 2 : 0)));
}

export default function Practice() {
  const { boardColor, blackStone, whiteStone, stoneStyle, blackEmoji, whiteEmoji, sound, sfxVolume } = useSettings();

  const [board, setBoard] = useState(() => Array.from({ length: N }, () => Array(N).fill(null)));
  const [turn, setTurn] = useState("black");
  const [humanColor, setHumanColor] = useState("black");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [winnerCode, setWinnerCode] = useState(-1); // -1 none, 0 draw, 1 black, 2 white
  const [k, setK] = useState(10);
  const [style, setStyle] = useState("BALANCE");
  const historyRef = useRef([]); // {row,col,color}
  const [hoverCell, setHoverCell] = useState(null); // [row, col]
  // const [showTutorial, setShowTutorial] = useState(false);
  const [lastAiMove, setLastAiMove] = useState(null); // {x,y}
  const [controlsOpen, setControlsOpen] = useState(true);
  const [infoOpen, setInfoOpen] = useState(true);
  const navigate = useNavigate();

  const isGameOver = winnerCode >= 0;

  const resetGame = useCallback(() => {
    setBoard(Array.from({ length: N }, () => Array(N).fill(null)));
    setTurn("black");
    setBusy(false);
    setError("");
    setWinnerCode(-1);
    historyRef.current = [];
  }, []);

  const placeSound = useCallback(() => {
    if (!sound) return;
    const a = new Audio("/sounds/pop.mp3");
    a.volume = Math.min(1, Math.max(0, sfxVolume ?? 0.3));
    a.play().catch(() => {});
  }, [sound, sfxVolume]);

  const outcomeSound = useCallback((winner) => {
    if (!sound) return;
    const src = winner === 0 ? "/sounds/victory.mp3" : winner === (humanColor === "black" ? 1 : 2) ? "/sounds/victory.mp3" : "/sounds/lose.mp3";
    const a = new Audio(src);
    a.volume = Math.min(1, Math.max(0, sfxVolume ?? 0.3));
    a.play().catch(() => {});
  }, [sound, sfxVolume, humanColor]);

  function hexToRgb(hex) {
    const normalized = String(hex || "").replace("#", "");
    if (normalized.length < 6) return { r: 255, g: 255, b: 255 };
    const bigint = parseInt(normalized.slice(0, 6), 16);
    return { r: (bigint >> 16) & 255, g: (bigint >> 8) & 255, b: bigint & 255 };
  }
  function isColorDark(hex) {
    const { r, g, b } = hexToRgb(hex);
    const brightness = (r * 299 + g * 587 + b * 114) / 1000;
    return brightness < 140;
  }

  // Collapse side panels on small screens by default
  useEffect(() => {
    const mq = window.matchMedia('(max-width: 1023px)');
    if (mq.matches) {
      setControlsOpen(false);
      setInfoOpen(false);
    }
  }, []);

  // Board visuals like GameRoom
  const boardLineColor = useMemo(
    () => (isColorDark(boardColor) ? "rgba(255,255,255,0.25)" : "rgba(0,0,0,0.2)"),
    [boardColor]
  );
  const INTERSECTIONS = N;
  const STEPS = INTERSECTIONS - 1;
  const STEP_PCT = 100 / STEPS;
  const EDGE_PADDING_PCT = STEP_PCT / 2;
  const HITBOX_PCT = STEP_PCT;
  const canInteract = !busy && !isGameOver && turn === humanColor;
  // Always hide cursor when hovering board to match GameRoom UX
  const boardCursorClass = "cursor-none";

  const checkWinner = useCallback((b) => {
    const seqB = findWinningSequence(b, "black", 5);
    if (seqB.length) return 1;
    const seqW = findWinningSequence(b, "white", 5);
    if (seqW.length) return 2;
    return -1;
  }, []);

  const handleClick = async (r, c) => {
    if (busy || isGameOver) return;
    if (turn !== humanColor) return;
    if (board[r][c]) return;
    setError("");
    const nextBoard = board.map((row, i) => row.map((v, j) => (i === r && j === c ? humanColor : v)));
    historyRef.current.push({ row: r, col: c, color: humanColor });
    setBoard(nextBoard);
    placeSound();

    // Check human win
    const w = checkWinner(nextBoard);
    if (w > 0) {
      setWinnerCode(w);
      outcomeSound(w);
      return;
    }

    setTurn(humanColor === "black" ? "white" : "black");
    setBusy(true);
    try {
      const aiNext = humanColor === "black" ? "WHITE" : "BLACK";
      const ai = await aiApi.getGptMove(toIntBoard(nextBoard), aiNext, { k, style });
      const { x, y } = ai || {};
      if (Number.isInteger(x) && Number.isInteger(y) && nextBoard[x]?.[y] == null) {
        const aiColor = humanColor === "black" ? "white" : "black";
        const after = nextBoard.map((row, i) => row.map((v, j) => (i === x && j === y ? aiColor : v)));
        historyRef.current.push({ row: x, col: y, color: aiColor });
        setBoard(after);
        setLastAiMove({ x, y });
        setTimeout(() => setLastAiMove(null), 1400);
        placeSound();
        const w2 = checkWinner(after);
        if (w2 > 0) {
          setWinnerCode(w2);
          outcomeSound(w2);
          setTurn(aiColor);
          return;
        }
        setTurn(humanColor);
      } else {
        // AI failed; keep turn to human
        setTurn(humanColor);
      }
    } catch (e) {
      setError(e?.message || "AI move failed");
      setTurn(humanColor);
    } finally {
      setBusy(false);
    }
  };

  const handleUndo = () => {
    if (isGameOver || busy) return;
    const hist = historyRef.current;
    if (!hist.length) return;
    const cloned = board.map((row) => row.slice());
    // Remove last move (likely AI)
    const last = hist.pop();
    if (last) cloned[last.row][last.col] = null;
    // Remove previous (player) if present
    const prev = hist.pop();
    if (prev) cloned[prev.row][prev.col] = null;
    setBoard(cloned);
    setWinnerCode(-1);
    setTurn(humanColor);
  };

  const handleSurrender = () => {
    if (isGameOver) return;
    const loser = humanColor === "black" ? 1 : 2;
    const winner = loser === 1 ? 2 : 1;
    setWinnerCode(winner);
    outcomeSound(winner);
  };

  const handleDraw = () => {
    if (isGameOver) return;
    setWinnerCode(0);
    outcomeSound(0);
  };

  // Settle results to ranking after game finishes
  useEffect(() => {
    if (winnerCode == null || winnerCode < 0) return;
    if (!isAuthenticated()) return;
    const { userId } = getAuthData() || {};
    if (!userId) return;

    // Create a unique matchId for AI games
    const matchId = `ai-${userId}-${Date.now()}`;
    const playerWins = (winnerCode === 1 && humanColor === "black") || (winnerCode === 2 && humanColor === "white");

    const settlementData = {
      matchId,
      winnerId: winnerCode === 0 ? null : playerWins ? userId : null,
      loserId: winnerCode === 0 ? null : playerWins ? null : userId,
      modeType: "CASUAL",
    };

    rankingApi
      .settleMatch(settlementData)
      .catch(() => {
        // non-blocking; suppress UI errors for background settlement
      });
  }, [winnerCode, humanColor]);

  const renderStone = (cell) => {
    if (!cell) return null;
    const isEmoji = stoneStyle === "emoji";
    if (isEmoji) {
      return (
        <div className="flex items-center justify-center text-3xl" style={{ position: "relative", zIndex: 1 }}>
          {cell === "black" ? blackEmoji : whiteEmoji}
        </div>
      );
    }
    const color = cell === "black" ? blackStone : whiteStone;
    return (
      <div
        className="rounded-full"
        style={{
          width: "70%",
          height: "70%",
          background: stoneStyle === "pattern"
            ? `radial-gradient(circle at 30% 30%, ${color} 0%, ${color} 55%, rgba(0,0,0,0.45) 100%)`
            : color,
          boxShadow: `0 0 10px ${color}80`,
          border: stoneStyle === "classic" ? "1px solid rgba(0,0,0,0.25)" : "none",
          position: "relative",
          zIndex: 1,
        }}
      />
    );
  };

  return (
    <div className="relative min-h-[calc(100vh-64px)] flex items-center justify-center overflow-hidden bg-[#F8FAFC] dark:bg-[#0D1B2A]">
      <BackgroundAnimation />

      <div className="relative z-10 w-full max-w-6xl p-4">
        <div className="flex flex-col lg:flex-row gap-4">
          <div className="flex-1 flex justify-center">
            <SectionCard className="bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-md p-4 w-full max-w-[720px]">
              <h1 className="text-2xl font-bold text-center mb-2">Practice vs AI</h1>
              <div className="text-center text-sm mb-3">
                Turn: <b className="uppercase">{turn}</b> {busy && <span>• AI thinking...</span>}
              </div>
              {error && <div className="text-red-500 text-sm mb-2 text-center">{error}</div>}

              <div className="flex flex-col items-center">
                <div className="relative" style={{ width: 'min(600px, min(92vw, 70vh))' }}>
                  {/* Turn badge (outside board) */}
                  <div className="mb-2 text-center text-xs font-semibold">
                    {turn === humanColor ? (
                      <span className="px-3 py-0.5 rounded-full bg-white/90 dark:bg-gray-800/90 border border-gray-300 dark:border-gray-700 text-green-600">Your Turn</span>
                    ) : (
                      <span className="px-3 py-0.5 rounded-full bg-white/90 dark:bg-gray-800/90 border border-gray-300 dark:border-gray-700 text-gray-600 dark:text-gray-300">AI Turn</span>
                    )}
                  </div>
                  <div
                    data-tour="practice-board"
                    className={`relative aspect-square w-full rounded-lg shadow-inner border-2 dark:border-gray-700 ${boardCursorClass}`}
                    style={{ padding: `${EDGE_PADDING_PCT}%`, backgroundColor: boardColor }}
                  >
                  <div
                    className="absolute inset-0"
                    style={{
                      backgroundImage: `linear-gradient(to right, ${boardLineColor} 1px, transparent 1px), linear-gradient(to bottom, ${boardLineColor} 1px, transparent 1px)`,
                      backgroundSize: `calc(100% / ${STEPS}) calc(100% / ${STEPS})`,
                    }}
                  >
                    {board.map((row, r) =>
                      row.map((cell, c) => {
                        const left = (c / STEPS) * 100;
                        const top = (r / STEPS) * 100;
                        const isHovered =
                          hoverCell && hoverCell[0] === r && hoverCell[1] === c;
                        return (
                          <div
                            key={`${r}-${c}`}
                            onClick={() => handleClick(r, c)}
                            onMouseEnter={() => setHoverCell([r, c])}
                            onMouseLeave={() => setHoverCell(null)}
                            className={`absolute flex items-center justify-center ${
                              canInteract ? "cursor-none" : "cursor-not-allowed opacity-70"
                            }`}
                            style={{
                              left: `${left}%`,
                              top: `${top}%`,
                              width: `${HITBOX_PCT}%`,
                              height: `${HITBOX_PCT}%`,
                              transform: "translate(-50%, -50%)",
                            }}
                          >
                            {/* Ghost preview when empty and hover */}
                            {!cell && isHovered && canInteract && (
                              stoneStyle === "emoji" ? (
                                <div className="flex items-center justify-center text-3xl" style={{ opacity: 0.7 }}>
                                  {humanColor === "black" ? blackEmoji : whiteEmoji}
                                </div>
                              ) : (
                                <div
                                  className="rounded-full opacity-60"
                                  style={{
                                    width: "70%",
                                    height: "70%",
                                    background:
                                      stoneStyle === "pattern"
                                        ? `radial-gradient(circle at 30% 30%, ${
                                            humanColor === "black" ? blackStone : whiteStone
                                          } 0%, ${
                                            humanColor === "black" ? blackStone : whiteStone
                                          } 55%, rgba(0,0,0,0.45) 100%)`
                                        : humanColor === "black" ? blackStone : whiteStone,
                                    border:
                                      stoneStyle === "classic"
                                        ? "1px solid rgba(0,0,0,0.25)"
                                        : "none",
                                  }}
                                />
                              )
                            )}

                            {/* Placed stone */}
                            {renderStone(cell)}

                            {/* Highlight AI last move */}
                            {cell && lastAiMove && lastAiMove.x === r && lastAiMove.y === c && (
                              <>
                                <div className="absolute rounded-full animate-ping" style={{ width: "85%", height: "85%", backgroundColor: "rgba(59,130,246,0.25)", zIndex: 0, pointerEvents: "none" }} />
                                <div className="absolute rounded-full" style={{ width: "75%", height: "75%", border: "2px solid rgba(59,130,246,0.9)", boxShadow: "0 0 14px rgba(59,130,246,0.6)", zIndex: 0, pointerEvents: "none" }} />
                              </>
                            )}
                          </div>
                        );
                      })
                    )}
                  </div>

                  {isGameOver && (
                    <div className="absolute inset-0 z-20 bg-black/30 flex items-center justify-center rounded-xl">
                      <div className="bg-white dark:bg-[#0F2538] px-6 py-4 rounded-lg shadow-md text-center z-30">
                        <div className="text-xl font-semibold mb-2">
                          {winnerCode === 0
                            ? "Draw"
                            : winnerCode === 1
                            ? "Black wins!"
                            : "White wins!"}
                        </div>
                        <button className="mt-1 px-4 py-1.5 rounded bg-[#EF7C00] text-white" onClick={resetGame}>
                          Restart
                        </button>
                      </div>
                    </div>
                  )}
                  </div>
                </div>

                <div className="flex justify-between mt-3 w-full max-w-[600px] text-sm sm:text-base font-semibold">
                  <span className="text-[#003D7C] dark:text-[#FFD166]">⚫ Black: {humanColor === "black" ? "You" : "AI"}</span>
                  <span className="text-[#EF7C00]">⚪ White: {humanColor === "white" ? "You" : "AI"}</span>
                </div>
              </div>
            </SectionCard>
          </div>

          {/* Controls sidebar */}
          <div className="w-full lg:w-[320px] flex flex-col gap-3">
            <SectionCard className="bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-md p-3 sm:p-4">
              <div className="flex items-center justify-between mb-2">
                <h2 className="text-base sm:text-lg font-semibold">Game Controls</h2>
                <button
                  className="px-2 py-1 text-xs rounded bg-gray-200 dark:bg-gray-700"
                  onClick={() => setControlsOpen((v) => !v)}
                >
                  {controlsOpen ? 'Hide' : 'Show'}
                </button>
              </div>
              {controlsOpen && (
              <div className="flex flex-col gap-2">
                {/* Tutorial disabled */}
                <div className="flex items-center justify-between" data-tour="control-player">
                  <span>You play as</span>
                  <select
                    className="border rounded px-2 py-1 text-sm bg-white dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 focus:outline-none focus:ring-1 focus:ring-[#EF7C00]"
                    value={humanColor}
                    onChange={(e) => {
                      if (historyRef.current.length > 0) return; // don't switch mid-game
                      setHumanColor(e.target.value);
                      setTurn("black");
                    }}
                  >
                    <option value="black">Black</option>
                    <option value="white">White</option>
                  </select>
                </div>

                <div className="flex items-center justify-between" data-tour="control-style">
                  <span>Style</span>
                  <select className="border rounded px-2 py-1 text-sm bg-white dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 focus:outline-none focus:ring-1 focus:ring-[#EF7C00]" value={style} onChange={(e) => setStyle(e.target.value)}>
                    <option value="OFFENSE">Offense</option>
                    <option value="BALANCE">Balance</option>
                    <option value="DEFENSE">Defense</option>
                  </select>
                </div>

                <div className="flex items-center justify-between" data-tour="control-k">
                  <span>Candidate K</span>
                  <input
                    type="number"
                    min={4}
                    max={40}
                    value={k}
                    onChange={(e) => setK(Math.max(4, Math.min(40, Number(e.target.value) || 10)))}
                    className="border rounded px-2 py-1 w-20 text-sm bg-white dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 focus:outline-none focus:ring-1 focus:ring-[#EF7C00]"
                  />
                </div>

                <div className="flex gap-2 mt-2" data-tour="control-undo-restart">
                  <button className="flex-1 px-3 py-1.5 rounded bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 dark:text-gray-200 disabled:opacity-50" onClick={handleUndo} disabled={busy || isGameOver || historyRef.current.length < 2}>
                    Undo
                  </button>
                  <button className="flex-1 px-3 py-1.5 rounded bg-[#EF7C00] text-white hover:bg-[#D66D00] shadow-sm" onClick={resetGame}>
                    Restart
                  </button>
                </div>

                <div className="flex gap-2" data-tour="control-surrender-draw">
                  <button className="flex-1 px-3 py-1.5 rounded bg-red-500 text-white hover:bg-red-600 dark:bg-red-600 dark:hover:bg-red-500 shadow-sm disabled:opacity-50" onClick={handleSurrender} disabled={isGameOver}>
                    Surrender
                  </button>
                  <button className="flex-1 px-3 py-1.5 rounded bg-blue-500 text-white hover:bg-blue-600 dark:bg-blue-600 dark:hover:bg-blue-500 shadow-sm disabled:opacity-50" onClick={handleDraw} disabled={isGameOver}>
                    Offer Draw
                  </button>
                </div>

                <button
                  className="mt-2 px-3 py-1.5 rounded bg-gray-700 text-white hover:bg-gray-800 dark:bg-gray-800 dark:hover:bg-gray-700"
                  onClick={() => navigate("/")}
                >
                  Leave Match
                </button>
              </div>
              )}
            </SectionCard>

            <SectionCard className="bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-md p-3 sm:p-4">
              <div className="flex items-center justify-between mb-2">
                <h2 className="text-base sm:text-lg font-semibold">How it works</h2>
                <button
                  className="px-2 py-1 text-xs rounded bg-gray-200 dark:bg-gray-700"
                  onClick={() => setInfoOpen((v) => !v)}
                >
                  {infoOpen ? 'Hide' : 'Show'}
                </button>
              </div>
              {infoOpen && (
                <p className="text-sm text-gray-600 dark:text-gray-300">
                  Play against an AI that searches the whole board for immediate wins/blocks and scores all empty positions.
                  Use Style and Candidate K to fine-tune its behavior. Undo removes your last move and the AI reply.
                </p>
              )}
            </SectionCard>
          </div>
        </div>
      </div>
      {/* Tutorial modal disabled */}

      {/* Walkthrough disabled */}
    </div>
  );
}
