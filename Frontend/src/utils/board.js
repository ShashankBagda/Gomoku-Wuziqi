/**
 * Board utility helpers for Gomoku gameplay.
 * Centralized here so we can unit test the core board logic in isolation.
 */

export const BOARD_DEFAULT_SIZE = 15;

export const WIN_DIRECTIONS = [
  [0, 1], // horizontal →
  [1, 0], // vertical ↓
  [1, 1], // diagonal ↘
  [1, -1], // diagonal ↗
];

export const createEmptyBoard = (size = BOARD_DEFAULT_SIZE) =>
  Array.from({ length: size }, () => Array.from({ length: size }, () => null));

export const mapBoardSnapshot = (snapshot) => {
  if (!snapshot || !Array.isArray(snapshot.board)) {
    return createEmptyBoard(snapshot?.boardSize || BOARD_DEFAULT_SIZE);
  }

  return snapshot.board.map((row) =>
    row.map((value) => {
      if (value === 1) return "black";
      if (value === 2) return "white";
      return null;
    })
  );
};

export function findWinningSequence(board, targetColor, requiredCount = 5) {
  if (!Array.isArray(board) || !board.length || !targetColor) {
    return [];
  }

  const size = board.length;

  for (let row = 0; row < size; row += 1) {
    for (let col = 0; col < size; col += 1) {
      if (!board[row] || board[row][col] !== targetColor) {
        continue;
      }

      for (const [dr, dc] of WIN_DIRECTIONS) {
        const prevRow = row - dr;
        const prevCol = col - dc;

        if (
          prevRow >= 0 &&
          prevRow < size &&
          prevCol >= 0 &&
          prevCol < size &&
          board[prevRow] &&
          board[prevRow][prevCol] === targetColor
        ) {
          // Not the start of the sequence, skip to avoid duplicates.
          continue;
        }

        const sequence = [];
        let r = row;
        let c = col;

        while (
          r >= 0 &&
          r < size &&
          c >= 0 &&
          c < size &&
          board[r] &&
          board[r][c] === targetColor
        ) {
          sequence.push({ row: r, col: c });
          r += dr;
          c += dc;
        }

        if (sequence.length >= requiredCount) {
          return sequence.slice(0, requiredCount);
        }
      }
    }
  }

  return [];
}

