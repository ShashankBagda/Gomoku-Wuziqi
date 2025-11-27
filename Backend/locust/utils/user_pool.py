"""
User Pool Manager
Manages test user data pool
"""

import json
import random
import threading
from typing import List, Dict, Optional


class UserPool:
    """
    User pool manager
    Manages and allocates test users in performance testing
    """

    def __init__(self, user_data_file: str = "phase1_users.json"):
        """
        Initialize user pool

        Args:
            user_data_file: User data file path
        """
        self.user_data_file = user_data_file
        self.users: List[Dict] = []
        self.available_users: List[Dict] = []
        self.lock = threading.Lock()
        self.load_users()

    def load_users(self):
        """Load user data from file"""
        try:
            with open(self.user_data_file, 'r', encoding='utf-8') as f:
                self.users = json.load(f)
                self.available_users = list(self.users)
                print(f"✅ Loaded {len(self.users)} users from {self.user_data_file}")
        except FileNotFoundError:
            print(f"⚠️  User data file not found: {self.user_data_file}")
            print(f"⚠️  Please run Phase 1 first to generate user data!")
            self.users = []
            self.available_users = []
        except json.JSONDecodeError as e:
            print(f"❌ Failed to parse user data file: {e}")
            self.users = []
            self.available_users = []

    def get_random_user(self) -> Optional[Dict]:
        """
        Get a random user (repeatable)

        Returns:
            User data dict, None if pool is empty
        """
        if not self.users:
            return None
        return random.choice(self.users)

    def get_unique_user(self) -> Optional[Dict]:
        """
        Get a unique user (non-repeating, until exhausted)

        Returns:
            User data dict, None if pool is empty
        """
        with self.lock:
            if not self.available_users:
                print("⚠️  No more available users in pool!")
                return None
            return self.available_users.pop()

    def reset(self):
        """Reset user pool (mark all users as available)"""
        with self.lock:
            self.available_users = list(self.users)
            print(f"🔄 User pool reset, {len(self.available_users)} users available")

    def add_user(self, user_data: Dict):
        """
        Add user to pool

        Args:
            user_data: User data dict
        """
        with self.lock:
            self.users.append(user_data)
            self.available_users.append(user_data)

    def save_users(self, output_file: Optional[str] = None):
        """
        Save user data to file

        Args:
            output_file: Output file path, defaults to initialization file
        """
        output_file = output_file or self.user_data_file
        with self.lock:
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(self.users, f, indent=2, ensure_ascii=False)
            print(f"💾 Saved {len(self.users)} users to {output_file}")

    def get_count(self) -> int:
        """Get total user count"""
        return len(self.users)

    def get_available_count(self) -> int:
        """Get available user count"""
        return len(self.available_users)


class RoomPool:
    """
    Room pool manager
    Shares room information in tests
    """

    def __init__(self):
        self.rooms: List[Dict] = []
        self.lock = threading.Lock()

    def add_room(self, room_data: Dict):
        """
        Add room

        Args:
            room_data: Room data {"roomCode": "xxx", "roomId": 123, "creator": "xxx"}
        """
        with self.lock:
            self.rooms.append(room_data)

    def get_room(self) -> Optional[Dict]:
        """
        Get an available room (FIFO)

        Returns:
            Room data dict, None if no rooms available
        """
        with self.lock:
            if not self.rooms:
                return None
            return self.rooms.pop(0)

    def get_count(self) -> int:
        """Get available room count"""
        return len(self.rooms)

    def clear(self):
        """Clear room pool"""
        with self.lock:
            self.rooms.clear()
