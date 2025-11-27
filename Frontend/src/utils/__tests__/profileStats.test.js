import {
  createEmptyStats,
  coerceNumber,
  sanitizePercentage,
  mapUserStats,
  formatNumber,
  formatPercentage,
} from "../profileStats";

describe("profileStats utilities", () => {
  test("createEmptyStats returns neutral defaults", () => {
    expect(createEmptyStats()).toEqual({
      score: null,
      rank: null,
      wins: null,
      gamesPlayed: null,
      winRate: null,
      totalExp: null,
      level: null,
      xpPercent: null,
    });
  });

  describe("coerceNumber", () => {
    test("converts strings and numbers", () => {
      expect(coerceNumber("42")).toBe(42);
      expect(coerceNumber(13)).toBe(13);
    });

    test("returns null for invalid input", () => {
      expect(coerceNumber(undefined)).toBeNull();
      expect(coerceNumber(null)).toBeNull();
      expect(coerceNumber("not-a-number")).toBeNull();
      expect(coerceNumber(Infinity)).toBeNull();
      expect(coerceNumber(NaN)).toBeNull();
    });
  });

  describe("sanitizePercentage", () => {
    test("caps values between 0 and 100", () => {
      expect(sanitizePercentage(0)).toBe(0);
      expect(sanitizePercentage(50)).toBe(50);
      expect(sanitizePercentage(150)).toBe(100);
      expect(sanitizePercentage(-20)).toBe(0);
    });

    test("accepts ratios between 0 and 1", () => {
      expect(sanitizePercentage(0.5)).toBe(50);
      expect(sanitizePercentage(1)).toBe(100);
    });

    test("returns null for invalid inputs", () => {
      expect(sanitizePercentage()).toBeNull();
      expect(sanitizePercentage("NaN")).toBeNull();
    });
  });

  describe("mapUserStats", () => {
    test("returns empty stats for invalid payload", () => {
      expect(mapUserStats(null)).toEqual(createEmptyStats());
      expect(mapUserStats("invalid")).toEqual(createEmptyStats());
    });

    test("maps mixed payload keys and derives stats", () => {
      const payload = {
        data: {
          totalScore: "1234",
          rankPosition: "8",
          totalWins: "70",
          totalLosses: "30",
          gamesPlayed: undefined,
          winRate: 0.75,
          totalExp: "9000",
          levelId: "12",
          levelProgress: 0.4,
        },
      };

      expect(mapUserStats(payload)).toEqual({
        score: 1234,
        rank: 8,
        wins: 70,
        gamesPlayed: 100,
        winRate: 75,
        totalExp: 9000,
        level: 12,
        xpPercent: 40,
      });
    });

    test("handles raw payloads without data wrapper", () => {
      const payload = {
        score: 555,
        rank: 2,
        wins: 10,
        games: 20,
        winRate: 60,
        totalExp: 100,
        level: 5,
        progress: 80,
      };

      expect(mapUserStats(payload)).toEqual({
        score: 555,
        rank: 2,
        wins: 10,
        gamesPlayed: 20,
        winRate: 60,
        totalExp: 100,
        level: 5,
        xpPercent: 80,
      });
    });

    test("derives win rate when missing and normalizes xp ratio", () => {
      const payload = {
        data: {
          score: 200,
          wins: 15,
          losses: 5,
          games: null,
          winRate: null,
          levelProgress: 1.2,
        },
      };

      expect(mapUserStats(payload)).toEqual({
        score: 200,
        rank: null,
        wins: 15,
        gamesPlayed: 20,
        winRate: 75,
        totalExp: null,
        level: null,
        xpPercent: 1.2,
      });
    });

    test("preserves fractional win rate when games are zero and exercises fallbacks", () => {
      const payload = {
        data: {
          points: 99,
          winCount: "3",
          totalLosses: "2",
          games: 0,
          winRate: 0.5,
          currentProgress: 0.25,
        },
      };

      expect(mapUserStats(payload)).toEqual({
        score: 99,
        rank: null,
        wins: 3,
        gamesPlayed: 0,
        winRate: 0.5,
        totalExp: null,
        level: null,
        xpPercent: 25,
      });
    });

    test("keeps win rate null when insufficient data remains", () => {
      const payload = {
        data: {
          wins: null,
          losses: null,
          gamesPlayed: null,
          winRate: null,
        },
      };

      expect(mapUserStats(payload)).toEqual({
        score: null,
        rank: null,
        wins: null,
        gamesPlayed: null,
        winRate: null,
        totalExp: null,
        level: null,
        xpPercent: null,
      });
    });
  });

  describe("format helpers", () => {
    test("formatNumber returns dashes for null and formatted numbers otherwise", () => {
      expect(formatNumber(null)).toBe("—");
      expect(formatNumber("1000")).toBe("1,000");
    });

    test("formatPercentage returns dashes for null and formatted percentages otherwise", () => {
      expect(formatPercentage(null)).toBe("—");
      expect(formatPercentage(67.891)).toBe("67.9%");
    });
  });
});
