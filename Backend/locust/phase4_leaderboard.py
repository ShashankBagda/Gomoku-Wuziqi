"""
Phase 4: Leaderboard Load Test (Refactored)
Using modular architecture

Goals:
1. Dedicated stress test for leaderboard interface (GET /api/ranking/leaderboard)
2. Test performance under high concurrency query scenarios
3. Verify cache strategy effectiveness

Usage:
    locust -f phase4_leaderboard.py --host=https://test-api-gomoku.goodyhao.me --users=100 --spawn-rate=20 --run-time=5m --headless

Performance Goals:
    - P50 < 100ms, P95 < 300ms, P99 < 500ms
    - Throughput > 500 req/s
    - Error rate < 0.1%
"""

import sys
import io
from locust import HttpUser, task, between, events

# Import refactored modules
from utils.locust_adapter import LocustApiClient
from services.leaderboard_service import LeaderboardService

# Fix Windows console encoding
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')


class Phase4_LeaderboardTest(HttpUser):
    """
    Phase 4: Leaderboard Load Test (Refactored)
    Testing using LeaderboardService
    """

    wait_time = between(0.5, 2)

    def on_start(self):
        """Initialize services"""
        self.api_client = LocustApiClient(self.client, self.host)
        self.leaderboard_service = LeaderboardService(self.api_client)

    @task
    def get_leaderboard(self):
        """
        Get leaderboard (public API)
        Returns Top 50 players in three leaderboards: DAILY, WEEKLY, MONTHLY
        """
        data = self.leaderboard_service.get_leaderboard()

        if data:
            # Optional: verify data structure
            daily = data.get("daily", [])
            weekly = data.get("weekly", [])
            monthly = data.get("monthly", [])
            # print(f"📊 Leaderboard: {len(daily)} daily, {len(weekly)} weekly, {len(monthly)} monthly")


# ============================================
# Locust Event Handlers
# ============================================

@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    """Print info when test starts"""
    print("\n" + "=" * 60)
    print("🚀 Phase 4: Leaderboard Load Test Started (Refactored)")
    print("=" * 60)
    print("Target: GET /api/ranking/leaderboard")
    print("Strategy: Pure leaderboard query (public API)")
    print("Goals: P50<100ms, P95<300ms, P99<500ms, RPS>500")
    print("=" * 60 + "\n")


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    """Print summary when test ends"""
    stats = environment.stats.total

    print("\n" + "=" * 60)
    print("🎉 Phase 4: Leaderboard Load Test Completed!")
    print("=" * 60)
    print(f"📊 Total requests: {stats.num_requests}")
    print(f"❌ Failed: {stats.num_failures} ({stats.fail_ratio * 100:.2f}%)")
    print(f"⚡ Average RPS: {stats.total_rps:.2f}")
    print(f"⏱️  P50: {stats.get_response_time_percentile(0.5):.0f}ms")
    print(f"⏱️  P95: {stats.get_response_time_percentile(0.95):.0f}ms")
    print(f"⏱️  P99: {stats.get_response_time_percentile(0.99):.0f}ms")
    print("=" * 60 + "\n")


# ============================================
# 使用说明
# ============================================
if __name__ == "__main__":
    print("=" * 60)
    print("Phase 4: Leaderboard Load Test (Refactored)")
    print("=" * 60)
    print()
    print("✨ Using modular architecture:")
    print("  - services/leaderboard_service.py")
    print("  - utils/locust_adapter.py")
    print()
    print("This test will:")
    print("  1. Pure leaderboard query pressure test")
    print("  2. Test database/cache performance")
    print("  3. No authentication required (public API)")
    print()
    print("Run with:")
    print("  locust -f phase4_leaderboard.py \\")
    print("    --host=https://test-api-gomoku.goodyhao.me \\")
    print("    --users=100 \\")
    print("    --spawn-rate=20 \\")
    print("    --run-time=5m \\")
    print("    --headless")
    print()
    print("Performance Targets:")
    print("  - P50 < 100ms, P95 < 300ms, P99 < 500ms")
    print("  - Throughput > 500 req/s")
    print("  - Error rate < 0.1%")
    print()
    print("=" * 60)
