"""
Game Service
Encapsulates game-related operations
"""

from typing import Optional, Tuple
from utils.api_client import ApiClient, ApiResponse


class GameService:
    """Game service for game actions and state management"""

    def __init__(self, api_client: ApiClient):
        self.api_client = api_client

    def ready(self, room_id: str, user_id: str, token: str) -> Tuple[bool, Optional[str]]:
        """
        Mark player as ready

        Args:
            room_id: Room ID
            user_id: User ID
            token: User token

        Returns:
            (success, error_message)
        """
        headers = {
            "Authorization": token,
            "X-User-Id": str(user_id)
        }

        response = self.api_client.post(
            f"/api/gomoku/game/{room_id}/action",
            payload={"type": "READY"},
            headers=headers
        )

        if response.success:
            return True, None
        else:
            error_msg = f"[{response.error_code}] {response.error_msg}"
            return False, error_msg

    def surrender(self, room_id: str, user_id: str, token: str) -> Tuple[bool, Optional[str]]:
        """
        Surrender the game

        Args:
            room_id: Room ID
            user_id: User ID
            token: User token

        Returns:
            (success, error_message)
        """
        headers = {
            "Authorization": token,
            "X-User-Id": str(user_id)
        }

        response = self.api_client.post(
            f"/api/gomoku/game/{room_id}/action",
            payload={"type": "SURRENDER"},
            headers=headers
        )

        if response.success:
            return True, None
        else:
            error_msg = f"[{response.error_code}] {response.error_msg}"
            return False, error_msg

    def get_game_state(self, room_id: str, user_id: str, token: str) -> Optional[dict]:
        """
        Get current game state

        Args:
            room_id: Room ID
            user_id: User ID
            token: User token

        Returns:
            Game state data
        """
        headers = {
            "Authorization": token,
            "X-User-Id": str(user_id)
        }

        response = self.api_client.get(
            f"/api/gomoku/game/{room_id}/state",
            headers=headers
        )

        return response.data if response.success else None

    def is_black_player(self, game_state: dict, user_id: str) -> bool:
        """
        Check if user is the black player

        Args:
            game_state: Game state data
            user_id: User ID

        Returns:
            True if user is black player
        """
        black_player = game_state.get("blackPlayer")
        return str(black_player) == str(user_id)

    def settle_match(self, match_id: str, winner_id: str, loser_id: str,
                     mode_type: str, user_id: str, token: str) -> Tuple[bool, Optional[str]]:
        """
        Settle match result (ranking settle)

        Args:
            match_id: Match/Room ID
            winner_id: Winner ID
            loser_id: Loser ID
            mode_type: Game mode ("RANKED", "CASUAL", "PRIVATE")
            user_id: User ID
            token: User token

        Returns:
            (success, error_message)
        """
        headers = {
            "Authorization": token,
            "X-User-Id": str(user_id)
        }

        payload = {
            "matchId": int(match_id),
            "winnerId": int(winner_id),
            "loserId": int(loser_id),
            "modeType": mode_type
        }

        response = self.api_client.post(
            "/api/ranking/settle",
            payload=payload,
            headers=headers
        )

        if response.success:
            return True, None
        else:
            error_msg = f"[{response.error_code}] {response.error_msg}"
            return False, error_msg
