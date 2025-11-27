import {
  BOARD_DEFAULT_SIZE,
  createEmptyBoard,
  mapBoardSnapshot,
  findWinningSequence,
} from "../board";

describe("board utilities", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("creates an empty board with default size", () => {
    const board = createEmptyBoard();
    expect(board).toHaveLength(BOARD_DEFAULT_SIZE);
    board.forEach((row) => {
      expect(row).toHaveLength(BOARD_DEFAULT_SIZE);
      row.forEach((cell) => expect(cell).toBeNull());
    });
  });

  it("creates an empty board with custom size", () => {
    const board = createEmptyBoard(3);
    expect(board).toEqual([
      [null, null, null],
      [null, null, null],
      [null, null, null],
    ]);
  });

  it("maps backend snapshot to board colors", () => {
    const snapshot = {
      boardSize: 3,
      board: [
        [1, 0, 2],
        [2, 1, 0],
        [0, 0, 1],
      ],
    };

    expect(mapBoardSnapshot(snapshot)).toEqual([
      ["black", null, "white"],
      ["white", "black", null],
      [null, null, "black"],
    ]);
  });

  it("returns empty board when snapshot missing", () => {
    const emptyBoard = mapBoardSnapshot(null);
    expect(emptyBoard).toHaveLength(BOARD_DEFAULT_SIZE);
    expect(emptyBoard.flat().every((cell) => cell === null)).toBe(true);
  });

  it("detects horizontal five in a row", () => {
    const board = createEmptyBoard(7);
    for (let col = 1; col <= 5; col += 1) {
      board[3][col] = "black";
    }

    expect(findWinningSequence(board, "black")).toEqual([
      { row: 3, col: 1 },
      { row: 3, col: 2 },
      { row: 3, col: 3 },
      { row: 3, col: 4 },
      { row: 3, col: 5 },
    ]);
  });

  it("detects vertical five in a row", () => {
    const board = createEmptyBoard(8);
    for (let row = 2; row <= 6; row += 1) {
      board[row][5] = "white";
    }
    expect(findWinningSequence(board, "white")).toEqual([
      { row: 2, col: 5 },
      { row: 3, col: 5 },
      { row: 4, col: 5 },
      { row: 5, col: 5 },
      { row: 6, col: 5 },
    ]);
  });

  it("detects diagonal sequences and ignores extended chains", () => {
    const board = createEmptyBoard(10);
    for (let i = 0; i < 6; i += 1) {
      board[i + 1][i + 2] = "black";
    }

    expect(findWinningSequence(board, "black")).toEqual([
      { row: 1, col: 2 },
      { row: 2, col: 3 },
      { row: 3, col: 4 },
      { row: 4, col: 5 },
      { row: 5, col: 6 },
    ]);
  });

  it("returns empty array when no winning sequence exists", () => {
    const board = createEmptyBoard(5);
    board[0][0] = "black";
    board[0][1] = "white";
    board[1][0] = "white";
    board[1][1] = "black";
    expect(findWinningSequence(board, "black")).toEqual([]);
  });

  it("handles invalid input safely", () => {
    expect(findWinningSequence(null, "black")).toEqual([]);
    expect(findWinningSequence([], "black")).toEqual([]);
    expect(findWinningSequence(createEmptyBoard(), null)).toEqual([]);
  });
});

