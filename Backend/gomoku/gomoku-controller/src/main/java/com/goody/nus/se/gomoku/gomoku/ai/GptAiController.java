package com.goody.nus.se.gomoku.gomoku.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Lightweight GPT-powered AI move suggester for Practice mode.
 * Not used for ranked games.
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class GptAiController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String OPENAI_BASE = System.getenv().getOrDefault("OPENAI_BASE", "https://api.openai.com");
    private static final String OPENAI_MODEL = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4o-mini");

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    @PostMapping("/gpt-move")
    public ApiResult<AiMove> suggestMove(@RequestBody AiRequest req) {
        try {
            int[][] board = req.getBoard();
            String next = req.getNext();
            if (board == null || board.length == 0 || next == null) {
                return ApiResult.failed(400, "Invalid request");
            }
            int me = "WHITE".equalsIgnoreCase(next) ? 2 : 1;
            int opp = me == 1 ? 2 : 1;

            // 1) Win now
            AiMove win = findImmediateWin(board, me);
            if (win != null) return ApiResult.success(win);

            // 2) Block opponent win
            AiMove block = findImmediateWin(board, opp);
            if (block != null) return ApiResult.success(block);

            // 3) Top-K candidates by heuristic (K configurable)
            int k = req.getK() != null && req.getK() > 0 ? Math.min(40, req.getK()) : 10;
            List<ScoredMove> top = topCandidates(board, me, k, req.getStyle());
            if (!top.isEmpty()) {
                AiMove choice = chooseWithGpt(board, next, top);
                if (isLegalMove(board, choice)) return ApiResult.success(choice);
                return ApiResult.success(top.get(0).toMove());
            }

            // 4) Final fallback
            return ApiResult.success(heuristicMove(board, next));
        } catch (Exception e) {
            log.warn("AI move error", e);
            return ApiResult.success(heuristicMove(req.getBoard(), req.getNext()));
        }
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }

    private static boolean isLegalMove(int[][] b, AiMove m) {
        if (m == null) return false;
        int n = b.length;
        return m.x >= 0 && m.x < n && m.y >= 0 && m.y < n && b[m.x][m.y] == 0;
    }

    private static AiMove fallbackMove(int[][] b) {
        int n = b.length;
        int mid = n / 2;
        for (int r = Math.max(0, mid - 2); r < Math.min(n, mid + 3); r++) {
            for (int c = Math.max(0, mid - 2); c < Math.min(n, mid + 3); c++) {
                if (b[r][c] == 0) { AiMove m = new AiMove(); m.x = r; m.y = c; return m; }
            }
        }
        // absolute fallback
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) if (b[r][c] == 0) { AiMove m = new AiMove(); m.x = r; m.y = c; return m; }
        AiMove m = new AiMove(); m.x = 0; m.y = 0; return m;
    }

    private static AiMove heuristicMove(int[][] board, String next) {
        int self = "BLACK".equalsIgnoreCase(next) ? 1 : 2;
        int opp = self == 1 ? 2 : 1;
        int n = board.length;
        int bestR = -1, bestC = -1;
        int bestScore = -1;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board[r][c] != 0) continue;
                // Balanced default if not using request-scoped style
                int score = scoreMove(board, r, c, self) * 2 + scoreMove(board, r, c, opp) * 2;
                if (score > bestScore) { bestScore = score; bestR = r; bestC = c; }
            }
        }
        if (bestR >= 0) { AiMove m = new AiMove(); m.x = bestR; m.y = bestC; return m; }
        return fallbackMove(board);
    }

    private AiMove chooseWithGpt(int[][] board, String next, List<ScoredMove> top) {
        try {
            List<Map<String, Object>> options = new ArrayList<>();
            for (ScoredMove sm : top) options.add(Map.of("x", sm.x, "y", sm.y, "score", sm.score));
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", OPENAI_MODEL);
            payload.put("temperature", 0.1);
            payload.put("response_format", Map.of("type", "json_object"));
            String sys = "You are a Gomoku AI. 0=empty,1=black,2=white. Return JSON {x,y}. Choose from candidates when reasonable.";
            String user = "next=" + next + "; board=" + compressBoard(board) + "; candidates=" + options.toString();
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", sys),
                    Map.of("role", "user", "content", user)
            ));
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                log.warn("OPENAI_API_KEY is not configured; skipping GPT move selection");
                return null;
            }
            String body = MAPPER.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_BASE + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) return null;
            Map<String, Object> json = MAPPER.readValue(resp.body(), new TypeReference<>(){});
            String content = extractMessageContent(json);
            return MAPPER.readValue(content, AiMove.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static AiMove findImmediateWin(int[][] b, int player) {
        int n = b.length;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (b[r][c] != 0) continue;
                b[r][c] = player;
                boolean win = isFive(b, r, c, player);
                b[r][c] = 0;
                if (win) { AiMove m = new AiMove(); m.x = r; m.y = c; return m; }
            }
        }
        return null;
    }

    private static boolean isFive(int[][] b, int r, int c, int p) {
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        int n = b.length;
        for (int[] d : dirs) {
            int count = 1;
            count += countDir(b, r, c, d[0], d[1], p, n);
            count += countDir(b, r, c, -d[0], -d[1], p, n);
            if (count >= 5) return true;
        }
        return false;
    }

    private static int countDir(int[][] b, int r, int c, int dr, int dc, int p, int n) {
        int cnt = 0; int i = r + dr, j = c + dc;
        while (i >= 0 && i < n && j >= 0 && j < n && b[i][j] == p) { cnt++; i += dr; j += dc; }
        return cnt;
    }

    private static List<ScoredMove> topCandidates(int[][] b, int me, int k, String style) {
        int n = b.length; int opp = me == 1 ? 2 : 1;
        Style st = Style.from(style);
        List<ScoredMove> list = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (b[r][c] != 0) continue;
                int sMe = scoreMove(b, r, c, me);
                int sOpp = scoreMove(b, r, c, opp);
                int total = st.combine(sMe, sOpp);
                list.add(new ScoredMove(r, c, total));
            }
        }
        list.sort(Comparator.comparingInt((ScoredMove sm) -> sm.score).reversed());
        if (list.size() > k) return new ArrayList<>(list.subList(0, k));
        return list;
    }

    private static int scoreMove(int[][] b, int r, int c, int p) {
        int n = b.length; int score = 0;
        int[][] dirs = {{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d : dirs) score += patternScore(b, r, c, d[0], d[1], p, n);
        return score;
    }

    private static int patternScore(int[][] b, int r, int c, int dr, int dc, int p, int n) {
        int old = b[r][c]; b[r][c] = p;
        int len = 1; int openEnds = 0;
        int i = r + dr, j = c + dc;
        while (i >= 0 && i < n && j >= 0 && j < n && b[i][j] == p) { len++; i += dr; j += dc; }
        if (i >= 0 && i < n && j >= 0 && j < n && b[i][j] == 0) openEnds++;
        i = r - dr; j = c - dc;
        while (i >= 0 && i < n && j >= 0 && j < n && b[i][j] == p) { len++; i -= dr; j -= dc; }
        if (i >= 0 && i < n && j >= 0 && j < n && b[i][j] == 0) openEnds++;
        b[r][c] = old;

        if (len >= 5) return 1_000_000;
        if (len == 4 && openEnds == 2) return 100_000;
        if (len == 4 && openEnds == 1) return 20_000;
        if (len == 3 && openEnds == 2) return 5_000;
        if (len == 3 && openEnds == 1) return 500;
        if (len == 2 && openEnds == 2) return 120;
        if (len == 2 && openEnds == 1) return 30;
        return 5;
    }

    private static class ScoredMove {
        final int x; final int y; final int score;
        ScoredMove(int x, int y, int score) { this.x = x; this.y = y; this.score = score; }
        AiMove toMove() { AiMove m = new AiMove(); m.x = x; m.y = y; return m; }
    }

    private enum Style {
        OFFENSE(3, 1), BALANCE(2, 2), DEFENSE(2, 3);
        final int a; final int d;
        Style(int attackWeight, int defenseWeight) { this.a = attackWeight; this.d = defenseWeight; }
        int combine(int sMe, int sOpp) { return sMe * a + sOpp * d; }
        static Style from(String s) {
            if (s == null) return BALANCE;
            String t = s.trim().toUpperCase();
            switch (t) {
                case "OFFENSE": return OFFENSE;
                case "DEFENSE": return DEFENSE;
                default: return BALANCE;
            }
        }
    }

    private static String compressBoard(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                sb.append(board[i][j]);
            }
            if (i < board.length - 1) sb.append('/');
        }
        return sb.toString();
    }

    private static String extractMessageContent(Map<String, Object> json) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) json.get("choices");
            if (choices == null || choices.isEmpty()) return "{}";
            Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
            Object content = msg.get("content");
            return content == null ? "{}" : content.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @Data
    public static class AiRequest {
        private int[][] board; // [row][col]
        private String next;   // BLACK|WHITE
        private Integer k;     // number of candidates to consider (optional)
        private String style;  // OFFENSE|BALANCE|DEFENSE (optional)
    }

    @Data
    public static class AiMove {
        public int x;
        public int y;
    }
}
