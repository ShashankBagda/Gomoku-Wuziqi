"""
Phase 3: Private Room Full Game Flow Load Test (Refactored)
Using modular architecture

Goals:
1. Test private room creation and joining
2. Test complete game flow
3. Black always wins (plays vertically in one column)

Architecture:
- One Locust virtual user = controls two game users
- Fresh user registration each test, no reuse
- Completely independent test flow

Prerequisites:
    None (phase1_users.json not needed)

Usage:
    locust -f phase3_full_game.py --host=https://test-api-gomoku.goodyhao.me --users=10 --spawn-rate=2 --run-time=10m --headless

    Note: 10 virtual users = 20 game users testing simultaneously
"""

import sys
import io
import time
import threading
from locust import HttpUser, task, between, events

# Import refactored modules
from utils.locust_adapter import LocustApiClient
from utils.test_data import TestDataGenerator
from services.user_service import UserService
from services.room_service import RoomService
from services.game_service import GameService

# Fix Windows console encoding
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# Statistics
stats = {
    "rooms_created": 0,
    "games_finished": 0,
    "black_wins": 0,
    "total_moves": 0
}
stats_lock = threading.Lock()


class Phase3_PrivateRoomGameTest(HttpUser):
    """
    Phase 3: Private Room Full Game Flow Load Test (Refactored)
    Testing using modular services
    """

    wait_time = between(1, 3)

    def on_start(self):
        """Initialize: Register services"""
        self.api_client = LocustApiClient(self.client, self.host)
        self.user_service = UserService(self.api_client)
        self.room_service = RoomService(self.api_client)
        self.game_service = GameService(self.api_client)

        print(f"👥 Virtual user ready")

    @task(1)
    def private_room_game_flow(self):
        """
        Complete private room game flow (register and control two fresh users):
        1. Register two fresh users
        2. User1 creates private room
        3. User1 joins own created room
        4. User2 joins room
        5. Both ready
        6. Play a complete game (black always wins)
        """
        global stats

        # Step 0: Register two fresh users
        print(f"📝 Registering two new users...")

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

        print(f"✅ Registered: {self.user1['username']} & {self.user2['username']}")

        # Step 1: User1 creates private room
        print(f"🏠 {self.user1['username']} creating private room...")
        success, room_id, room_code = self.room_service.create_private_room(
            self.user1['user_id'],
            self.user1['token']
        )

        if not success:
            print(f"❌ {self.user1['username']} failed to create room")
            return

        with stats_lock:
            stats["rooms_created"] += 1

        print(f"✅ Room created: {room_code} (ID: {room_id})")

        # Step 2: User1 (creator) joins own created room
        print(f"🚪 {self.user1['username']} joining own room {room_code}...")
        join_success_1, joined_room_id_1 = self.room_service.join_private_room(
            room_code,
            self.user1['user_id'],
            self.user1['token']
        )

        if not join_success_1:
            print(f"❌ {self.user1['username']} (creator) failed to join room")
            return

        print(f"✅ {self.user1['username']} (creator) joined room")

        # Step 3: User2 joins room
        print(f"🚪 {self.user2['username']} joining room {room_code}...")
        join_success_2, joined_room_id_2 = self.room_service.join_private_room(
            room_code,
            self.user2['user_id'],
            self.user2['token']
        )

        if not join_success_2 or not joined_room_id_2:
            print(f"❌ {self.user2['username']} failed to join room or get room ID")
            return

        # Use roomId returned from join (status should be matched)
        room_id = joined_room_id_2
        print(f"✅ {self.user2['username']} joined room {room_code} (ID: {room_id})")

        # Step 4: Both ready, then play
        self.both_ready_and_play(room_id)

    def both_ready_and_play(self, room_id: str):
        """
        Both ready, then play a complete game
        """
        global stats

        # Step 1: 用户1 ready
        success_1, error_1 = self.game_service.ready(
            room_id,
            self.user1['user_id'],
            self.user1['token']
        )
        if success_1:
            print(f"✅ {self.user1['username']} is ready")
        else:
            print(f"⚠️  {self.user1['username']} ready failed: {error_1}")

        # Step 2: 用户2 ready
        success_2, error_2 = self.game_service.ready(
            room_id,
            self.user2['user_id'],
            self.user2['token']
        )
        if success_2:
            print(f"✅ {self.user2['username']} is ready")
        else:
            print(f"⚠️  {self.user2['username']} ready failed: {error_2}")

        # Step 3: Wait for game to start
        print(f"⏳ Waiting for game to start...")
        game_state = None
        for i in range(10):
            time.sleep(1)

            game_state = self.game_service.get_game_state(
                room_id,
                self.user1['user_id'],
                self.user1['token']
            )

            if game_state and game_state.get("status") == "PLAYING":
                print(f"🎮 Game started (waited {i+1}s)")
                break
        else:
            print(f"⏱️  Game did not start after 10s")
            return

        # Step 4: Determine who is black player
        black_player = game_state.get("blackPlayerId")
        white_player = game_state.get("whitePlayerId")

        print(f"🎲 Game info - black: {black_player}, white: {white_player}")
        print(f"   User1: {self.user1['user_id']} ({self.user1['username']})")
        print(f"   User2: {self.user2['user_id']} ({self.user2['username']})")

        # Determine black/white players
        if str(black_player) == str(self.user1['user_id']):
            black_user = self.user1
            white_user = self.user2
        elif str(black_player) == str(self.user2['user_id']):
            black_user = self.user2
            white_user = self.user1
        else:
            print(f"❌ Cannot determine black player!")
            return

        # Step 5: Play until black wins
        # Strategy: BLACK plays (0,0), (0,1), (0,2), (0,3), (0,4) - 5 in a row
        #          WHITE plays (1,0), (1,1), (1,2), (1,3) - won't win
        self.play_until_black_wins(room_id, room_code, black_user, white_user)

    def play_until_black_wins(self, room_id: str, room_code: str, black_user: dict, white_user: dict):
        """
        Play until black wins
        Black strategy: (0,0), (0,1), (0,2), (0,3), (0,4) - vertically in one column
        White strategy: (1,0), (1,1), (1,2), (1,3) - also vertically in one column
        """
        global stats

        black_move_count = 0  # Number of black moves
        white_move_count = 0  # Number of white moves
        max_rounds = 10  # Max 10 rounds

        for round_num in range(max_rounds):
            # Get current game state
            game_state = self.game_service.get_game_state(
                room_id,
                black_user['user_id'],
                black_user['token']
            )

            if not game_state:
                print(f"❌ Failed to get game state")
                break

            status = game_state.get("status")

            # Check if game is over
            if status in ["BLACK_WIN", "WHITE_WIN", "DRAW", "FINISHED"]:
                print(f"🏆 Game over: {status}")
                if status == "BLACK_WIN":
                    with stats_lock:
                        stats["black_wins"] += 1
                with stats_lock:
                    stats["games_finished"] += 1

                # Step 6: Both players leave the room
                time.sleep(0.5)
                self.room_service.leave_room(room_code, black_user['user_id'], black_user['token'])
                self.room_service.leave_room(room_code, white_user['user_id'], white_user['token'])
                print(f"🚪 Both players left room {room_code}")
                break

            # Get current turn from currentState
            current_state = game_state.get("currentState", {})
            current_turn = current_state.get("currentTurn")  # "BLACK" or "WHITE"

            # Determine current player and position
            if current_turn == "BLACK":
                # Black's turn
                current_user = black_user
                x = 0  # Black plays column 0
                y = black_move_count  # Black: (0,0), (0,1), (0,2), (0,3), (0,4)
                color = "BLACK"
            elif current_turn == "WHITE":
                # White's turn
                current_user = white_user
                x = 1  # White plays column 1
                y = white_move_count  # White: (1,0), (1,1), (1,2), (1,3)
                color = "WHITE"
            else:
                print(f"❌ Unknown current turn: {current_turn}")
                break

            # Make move
            success = self.make_move(room_id, current_user, x, y, color)

            if success:
                # Update corresponding counter
                if current_turn == "BLACK":
                    black_move_count += 1
                else:
                    white_move_count += 1

                with stats_lock:
                    stats["total_moves"] += 1
            else:
                print(f"❌ Move failed, stopping game")
                break

            # Brief wait
            time.sleep(0.3)

    def make_move(self, room_id: str, user: dict, x: int, y: int, color: str) -> bool:
        """
        Place stone

        Args:
            room_id: Room ID
            user: User info
            x: X coordinate
            y: Y coordinate
            color: Stone color

        Returns:
            success
        """
        headers = {
            "Authorization": user['token'],
            "X-User-Id": str(user['user_id'])
        }

        response = self.api_client.post(
            f"/api/gomoku/game/{room_id}/action",
            payload={
                "type": "MOVE",
                "position": {"x": x, "y": y}
            },
            headers=headers
        )

        if response.success:
            print(f"♟️  {user['username']} ({color}) moved to ({x}, {y})")
            return True
        else:
            print(f"❌ {user['username']} ({color}) move failed: [{response.error_code}] {response.error_msg}")
            return False


# ============================================
# Locust Event Handlers
# ============================================

@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    """Print info when test starts"""
    print("\n" + "=" * 60)
    print("🚀 Phase 3: Private Room Game Flow Load Test Started")
    print("=" * 60)
    print("Architecture:")
    print("  - Each virtual user = controls 2 game users")
    print("  - Fresh registration for each test")
    print("  - Complete isolation, no user conflicts")
    print("=" * 60)
    print("Test flow per virtual user:")
    print("  1. Register 2 new users")
    print("  2. User1 creates private room")
    print("  3. User1 joins own room")
    print("  4. User2 joins the room")
    print("  5. Both users ready")
    print("  6. Play a complete game (BLACK always wins)")
    print("=" * 60)
    print("Game Strategy:")
    print("  - BLACK: (0,0)→(0,1)→(0,2)→(0,3)→(0,4) - 5 in a row")
    print("  - WHITE: (1,0)→(1,1)→(1,2)→(1,3) - parallel column")
    print("=" * 60 + "\n")


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    """Print statistics when test ends"""
    print("\n" + "=" * 60)
    print("🎉 Phase 3: Private Room Game Flow Load Test Completed!")
    print("=" * 60)
    print(f"🏠 Rooms created: {stats['rooms_created']}")
    print(f"🏁 Games finished: {stats['games_finished']}")
    print(f"🏆 Black wins: {stats['black_wins']}")
    print(f"♟️  Total moves: {stats['total_moves']}")

    if stats['games_finished'] > 0:
        avg_moves = stats['total_moves'] / stats['games_finished']
        print(f"📊 Average moves per game: {avg_moves:.1f}")

    print("=" * 60 + "\n")


# ============================================
# 使用说明
# ============================================
if __name__ == "__main__":
    print("=" * 60)
    print("Phase 3: Private Room Game Flow Load Test (Refactored)")
    print("=" * 60)
    print()
    print("✨ Using modular architecture:")
    print("  - services/user_service.py")
    print("  - services/room_service.py")
    print("  - services/game_service.py")
    print("  - utils/locust_adapter.py")
    print()
    print("This test will:")
    print("  1. Register 2 new users per iteration")
    print("  2. Test private room creation and joining")
    print("  3. Play a complete game with BLACK always winning")
    print("  4. No user pool needed (fresh registration)")
    print()
    print("Run with:")
    print("  locust -f phase3_full_game.py \\")
    print("    --host=https://test-api-gomoku.goodyhao.me \\")
    print("    --users=10 \\")
    print("    --spawn-rate=2 \\")
    print("    --run-time=10m \\")
    print("    --headless")
    print()
    print("=" * 60)
