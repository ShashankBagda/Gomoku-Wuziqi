"""
Leaderboard Service
Encapsulates leaderboard operations
"""

from typing import Optional
from utils.api_client import ApiClient


class LeaderboardService:
    """Leaderboard service for ranking queries"""

    def __init__(self, api_client: ApiClient):
        self.api_client = api_client

    def get_leaderboard(self) -> Optional[dict]:
        """
        Get leaderboard (public API, no auth required)

        Returns:
            Leaderboard data (contains DAILY, WEEKLY, MONTHLY)
        """
        response = self.api_client.get("/api/ranking/leaderboard")
        return response.data if response.success else None
