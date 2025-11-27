"""
Test Data Generator
Generates test data for load testing
"""

import time
import random
import threading

# Global counter, thread-safe incrementing sequence number
# Starts from current timestamp to ensure no duplicates across runs
_counter_lock = threading.Lock()
_counter = int(time.time() * 1000000000)  # Millisecond-level timestamp as starting value


class TestDataGenerator:
    """Test data generator"""

    @staticmethod
    def _get_next_sequence() -> int:
        """
        Get next sequence number (thread-safe)

        Returns:
            Incrementing sequence number
        """
        global _counter

        with _counter_lock:
            _counter += 1
            return _counter

    @staticmethod
    def generate_password(length: int = 12) -> str:
        """
        Generate random password

        Args:
            length: Password length

        Returns:
            Random password
        """
        import string
        chars = string.ascii_letters + string.digits + "!@#$%^&*"
        return ''.join(random.choice(chars) for _ in range(length))

    @staticmethod
    def default_test_password() -> str:
        """
        Get default test password

        Returns:
            Default password
        """
        return "Test123456!"

    @staticmethod
    def generate_unique_credentials():
        """
        Generate unique user credentials (complete combination of email, nickname, password)

        Returns:
            UserCredentials object
        """
        from services.user_service import UserCredentials

        seq = TestDataGenerator._get_next_sequence()

        return UserCredentials(
            email=f"loadtest_{seq}@test.com",
            nickname=f"Player_{seq}",
            password=TestDataGenerator.default_test_password()
        )
