"""
Phase 2: Matching & Room Module Load Test (Refactored - Fresh User Registration)
Using modular architecture

Goals:
1. Register two fresh users for each test (ensures no conflicts)
2. Two users match with each other (casual mode)
3. After both ready, black player surrenders immediately
4. Test complete matching-game flow

Architecture:
- One Locust virtual user = controls two game users
- Fresh user registration each test, no reuse
- Completely independent test flow, no dependency on other virtual users

Prerequisites:
    None (phase1_users.json no longer needed)

Usage:
    locust -f phase2_matching_room.py \
        --host=https://test-api-gomoku.goodyhao.me \
        --users=10 \
        --spawn-rate=2 \
        --run-time=10m \
        --headless

    Note: 10 virtual users = 20 game users testing simultaneously
"""

import sys
import io
import time
import random
import threading
from locust import HttpUser, task, between, events

# Import refactored modules
from utils.user_pool import UserPool
from utils.locust_adapter import LocustApiClient
from utils.test_data import TestDataGenerator
from services.user_service import UserService
from services.matching_service import MatchingService
from services.game_service import GameService

# Fix Windows console encoding
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# Initialize user pool
user_pool = UserPool("phase1_users.json")

# Statistics
stats = {
    "matched_count": 0,
    "surrendered_count": 0,
    "reported_count": 0,
    "match_canceled": 0
}
stats_lock = threading.Lock()


class Phase2_MatchingRoomTest(HttpUser):
    """
    Phase 2: Matching & Room Module Load Test (Refactored)
    Testing using MatchingService and GameService
    """

    wait_time = between(1, 3)

    def on_start(self):
        """Initialize: Register two fresh users"""
        # Initialize services
        self.api_client = LocustApiClient(self.client, self.host)
        self.user_service = UserService(self.api_client)
        self.matching_service = MatchingService(self.api_client)
        self.game_service = GameService(self.api_client)

        # Fixed to use ranking mode
        self.match_mode = "ranking"

        # No need to register in on_start, register new users in each task
        print(f"👥 Virtual user ready")

    @task(1)
    def matching_flow(self):
        """
        Complete matching flow (register and control two fresh users):
        1. Register two fresh users
        2. User1 and User2 join match queue simultaneously
        3. They match with each other immediately
        4. Both ready
        5. Black player surrenders
        """
        global stats

        # Step 0: Register two fresh users
        # Register user1
        credentials_1 = TestDataGenerator.generate_unique_credentials()
        response_1 = self.user_service.register(credentials_1)
        if not response_1.success:
            print(f"❌ User1 registration failed: [{response_1.error_code}] {response_1.error_msg}")
            return

        user_info_1 = self.user_service.login(credentials_1.email, credentials_1.password)
        if not user_info_1:
            print(f"❌ User1 login failed")
            return

        self.user1 = {
            "user_id": user_info_1.user_id,
            "token": user_info_1.token,
            "username": user_info_1.nickname
        }

        # Register user2
        credentials_2 = TestDataGenerator.generate_unique_credentials()
        response_2 = self.user_service.register(credentials_2)
        if not response_2.success:
            print(f"❌ User2 registration failed: [{response_2.error_code}] {response_2.error_msg}")
            return

        user_info_2 = self.user_service.login(credentials_2.email, credentials_2.password)
        if not user_info_2:
            print(f"❌ User2 login failed")
            return

        self.user2 = {
            "user_id": user_info_2.user_id,
            "token": user_info_2.token,
            "username": user_info_2.nickname
        }

        # Step 1: User1 starts matching
        match_result_1 = self.matching_service.start_match(
            self.match_mode,
            self.user1['user_id'],
            self.user1['token']
        )

        if not match_result_1:
            print(f"❌ {self.user1['username']} failed to start match")
            return

        # Step 2: User2 starts matching immediately (should match with User1)
        match_result_2 = self.matching_service.start_match(
            self.match_mode,
            self.user2['user_id'],
            self.user2['token']
        )

        if not match_result_2:
            print(f"❌ {self.user2['username']} failed to start match")
            return

        # Step 3: Get room ID
        room_id = None

        if match_result_1.status == "matched":
            room_id = match_result_1.room_id

        if match_result_2.status == "matched":
            room_id = match_result_2.room_id

        # If both still waiting, poll until match succeeds
        if not room_id:
            for i in range(10):
                time.sleep(0.5)

                # Query user1 status
                status_1 = self.matching_service.check_player_status(
                    self.user1['user_id'],
                    self.user1['token']
                )
                if status_1:
                    room_id = status_1.get("roomId") or status_1.get("currentRoomId")
                    if room_id:
                        break

        if not room_id:
            print(f"❌ Failed to match after 5s")
            return

        with stats_lock:
            stats["matched_count"] += 1

        # Step 4: Both ready
        self.both_ready_and_surrender(room_id)

    def both_ready_and_surrender(self, room_id: str):
        """
        Both ready, then black player surrenders
        """
        global stats

        # Step 1: User1 ready
        success_1, error_1 = self.game_service.ready(
            room_id,
            self.user1['user_id'],
            self.user1['token']
        )

        # Step 2: User2 ready
        success_2, error_2 = self.game_service.ready(
            room_id,
            self.user2['user_id'],
            self.user2['token']
        )

        # Step 3: Wait for game to start
        game_state = None
        for i in range(10):
            time.sleep(1)

            game_state = self.game_service.get_game_state(
                room_id,
                self.user1['user_id'],
                self.user1['token']
            )

            if game_state and game_state.get("status") == "PLAYING":
                break
        else:
            return

        # Step 4: Determine who is black player
        black_player = game_state.get("blackPlayerId")
        white_player = game_state.get("whitePlayerId")

        # Step 5: Determine black/white, black surrenders
        if str(black_player) == str(self.user1['user_id']):
            # User1 is black, User2 is white (winner)
            black_user = self.user1
            white_user = self.user2
        elif str(black_player) == str(self.user2['user_id']):
            # User2 is black, User1 is white (winner)
            black_user = self.user2
            white_user = self.user1
        else:
            return

        time.sleep(0.5)

        # Black surrenders
        success, error = self.game_service.surrender(
            room_id,
            black_user['user_id'],
            black_user['token']
        )

        if success:
            with stats_lock:
                stats["surrendered_count"] += 1

        # Step 6: Winner (white) reports settlement result
        time.sleep(0.5)

        success, error = self.game_service.settle_match(
            match_id=room_id,
            winner_id=white_user['user_id'],
            loser_id=black_user['user_id'],
            mode_type="RANKED",
            user_id=white_user['user_id'],
            token=white_user['token']
        )

        if success:
            with stats_lock:
                stats["reported_count"] = stats.get("reported_count", 0) + 1


# ============================================
# Locust Event Handlers
# ============================================

@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    """Print info when test starts"""
    print("\n" + "=" * 60)
    print("🚀 Phase 2: Matching & Room Load Test Started")
    print("=" * 60)
    print("Architecture:")
    print("  - Each virtual user = controls 2 game users")
    print("  - Fresh registration for each test")
    print("  - Complete isolation, no user conflicts")
    print("=" * 60)
    print("Test flow per virtual user:")
    print("  1. Register 2 new users")
    print("  2. Both users join ranking match queue")
    print("  3. They match with each other")
    print("  4. Both users ready")
    print("  5. Black player surrenders")
    print("  6. Winner (white) settles match result")
    print("=" * 60 + "\n")


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    """Print statistics when test ends"""
    print("\n" + "=" * 60)
    print("🎉 Phase 2: Matching & Room Load Test Completed!")
    print("=" * 60)
    print(f"✅ Successful matches: {stats['matched_count']}")
    print(f"🏳️  Black surrenders: {stats['surrendered_count']}")
    print(f"📊 Results reported: {stats['reported_count']}")
    print(f"🚫 Matches canceled: {stats['match_canceled']}")
    print("=" * 60)
    print("\n📋 Next Steps:")
    print("   Run Phase 3: locust -f phase3_full_game.py")
    print("=" * 60 + "\n")


# ============================================
# Usage Instructions
# ============================================
if __name__ == "__main__":
    print("=" * 60)
    print("Phase 2: Matching & Room Load Test (Refactored)")
    print("=" * 60)
    print()
    print("✨ Using modular architecture:")
    print("  - services/matching_service.py")
    print("  - services/game_service.py")
    print("  - utils/locust_adapter.py")
    print()
    print("Prerequisites:")
    print("  - phase1_users.json must exist (run Phase 1 first)")
    print()
    print("This test will:")
    print("  1. Test matchmaking performance (ranking mode)")
    print("  2. Black player surrenders after match")
    print("  3. Both players report game result")
    print()
    print("Run with:")
    print("  locust -f phase2_matching_room.py \\")
    print("    --host=https://test-api-gomoku.goodyhao.me \\")
    print("    --users=50 \\")
    print("    --spawn-rate=5 \\")
    print("    --run-time=10m \\")
    print("    --headless")
    print()
    print("=" * 60)
