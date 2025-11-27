"""
Matching Service
Encapsulates matchmaking operations
"""

from dataclasses import dataclass
from typing import Optional
from utils.api_client import ApiClient, ApiResponse


@dataclass
class MatchResult:
    """Match result"""
    status: str  # "matched" or "waiting"
    room_id: Optional[str] = None
    message: Optional[str] = None


class MatchingService:
    """Matching service for game matchmaking"""

    def __init__(self, api_client: ApiClient):
        self.api_client = api_client

    def start_match(self, mode: str, user_id: str, token: str) -> Optional[MatchResult]:
        """
        Start matchmaking

        Args:
            mode: Match mode ("ranked" or "casual")
            user_id: User ID
            token: User token

        Returns:
            MatchResult or None (on failure)
        """
        headers = {
            "Authorization": token,
            "X-User-Id": str(user_id)
        }

        response = self.api_client.post(
            "/api/gomoku/match",
            payload={"mode": mode},
            headers=headers
        )

        if not response.success:
            return None

        data = response.data
        return MatchResult(
            status=data.get("status"),
            room_id=data.get("roomId"),
            message=data.get("message")
        )

    def cancel_match(self, user_id: str, token: str) -> bool:
        """
        Cancel matchmaking

        Args:
            user_id: User ID
            token: User token

        Returns:
            success
        """
        headers = {
            "Authorization": token,
            "X-User-Id": str(user_id)
        }

        response = self.api_client.post(
            "/api/gomoku/match/cancel",
            payload={},
            headers=headers
        )

        return response.success

    def check_player_status(self, user_id: str, token: str) -> Optional[dict]:
        """
        Check player status

        Args:
            user_id: User ID
            token: User token

        Returns:
            Player status data
        """
        headers = {
            "Authorization": token,
            "X-User-Id": str(user_id)
        }

        response = self.api_client.get(
            "/api/gomoku/player/status",
            headers=headers
        )

        return response.data if response.success else None
