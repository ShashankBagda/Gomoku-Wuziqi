"""
Room Service
Encapsulates room-related operations
"""

from typing import Optional, Tuple
from utils.api_client import ApiClient


class RoomService:
    """Room service for private room operations"""

    def __init__(self, api_client: ApiClient):
        self.api_client = api_client

    def create_private_room(self, user_id: str, token: str) -> Tuple[bool, Optional[str], Optional[str]]:
        """
        Create a private room

        Args:
            user_id: User ID
            token: User token

        Returns:
            (success, room_id, room_code)
        """
        headers = {
            "Authorization": token
        }

        response = self.api_client.post(
            "/api/gomoku/lobby/create-room",
            payload={},
            headers=headers
        )

        if response.success:
            room_id = response.data.get("roomId")
            room_code = response.data.get("roomCode")
            return True, str(room_id), room_code
        else:
            return False, None, None

    def join_private_room(self, room_code: str, user_id: str, token: str) -> Tuple[bool, Optional[str]]:
        """
        Join a private room

        Args:
            room_code: Room code
            user_id: User ID
            token: User token

        Returns:
            (success, room_id)
        """
        headers = {
            "Authorization": token
        }

        response = self.api_client.post(
            "/api/gomoku/lobby/join-room",
            payload={"roomCode": room_code},
            headers=headers
        )

        if response.success:
            room_id = response.data.get("roomId")
            return True, str(room_id) if room_id else None
        else:
            return False, None

    def leave_room(self, room_code: str, user_id: str, token: str) -> bool:
        """
        Leave a room

        Args:
            room_code: Room code
            user_id: User ID
            token: User token

        Returns:
            success
        """
        headers = {
            "Authorization": token
        }

        response = self.api_client.post(
            "/api/gomoku/lobby/leave",
            payload={"roomCode": room_code},
            headers=headers
        )

        return response.success
