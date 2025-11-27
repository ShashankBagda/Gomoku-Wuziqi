"""
Phase 1: User Module Load Test (Refactored)
Using modular architecture

Goals:
1. Test user registration, login, verification interface performance
2. Generate real test user data
3. Prepare user pool for subsequent test phases

Usage:
    locust -f phase1_user_module.py --host=https://test-api-gomoku.goodyhao.me --users=100 --spawn-rate=100 --run-time=1m --headless

Output:
    phase1_users.json - Contains all successfully registered user data
"""

import time
import random
import threading
from locust import HttpUser, task, between, events

# Import refactored modules
from utils.test_data import TestDataGenerator
from utils.user_pool import UserPool
from utils.locust_adapter import LocustApiClient
from services.user_service import UserService, UserCredentials

# Global user pool to store successfully registered users
registered_users = []
user_lock = threading.Lock()

# Counters
success_count = 0
fail_count = 0


class Phase1_UserModuleTest(HttpUser):
    """
    Phase 1: User Module Load Test (Refactored)
    Testing using UserService and ApiClient
    """
    wait_time = between(1, 1)

    @task(1)
    def user_registration_flow(self):
        """
        Complete user registration flow
        1. Get public key
        2. Register user
        3. Login and get token
        4. Verify token
        5. (Optional) Test password reset
        6. Save user data
        """
        global success_count, fail_count

        # Use ApiClient to wrap Locust's client
        # Note: Create an adapter to let ApiClient use Locust's HttpUser.client
        self.api_client = LocustApiClient(self.client, self.host)
        self.user_service = UserService(self.api_client)

        # Generate unique test credentials
        self.credentials = TestDataGenerator.generate_unique_credentials()

        self.user_info = None

        # Step 1: Register user (internally gets public key and encrypts password)
        response = self.user_service.register(self.credentials)

        if not response.success:
            print(f"❌ [{self.credentials}] Registration failed: [{response.error_code}] {response.error_msg}")
            fail_count += 1
            return

        # Step 2: Login and get token
        user_info = self.user_service.login(self.credentials.email, self.credentials.password)

        if not user_info:
            print(f"❌ [{self.credentials}] Login failed")
            fail_count += 1
            return

        self.user_info = user_info

        # Step 3: Verify token
        is_valid = self.user_service.verify_token(user_info.token)

        if not is_valid:
            print(f"❌ [{self.credentials}] Token verification failed")
            fail_count += 1
            return


        # Step 4: Test password reset (required)
        if not self.test_reset_password():
            print(f"⚠️  [{self.credentials}] Password reset failed, but continuing...")

        # Step 5: Re-login with new password to verify
        new_password = "NewPass123456!"
        user_info_after_reset = self.user_service.login(self.credentials.email, new_password)

        if not user_info_after_reset:
            print(f"❌ [{self.credentials}] Login with new password failed")
            fail_count += 1
            return

        # Update user_info with new token
        self.user_info = user_info_after_reset

        # Step 6: Save user data to global pool
        self.save_user_to_pool()
        success_count += 1


        # Note: Don't call quit(), let test end naturally to save all users

    def test_reset_password(self) -> bool:
        """Test password reset functionality"""
        new_password = "NewPass123456!"
        response = self.user_service.reset_password(self.credentials.email, new_password)

        if response.success:
            print(f"🔑 [{self.credentials.nickname}] Password reset successful")
            return True
        else:
            print(f"❌ [{self.credentials.nickname}] Password reset failed: [{response.error_code}] {response.error_msg}")
            return False

    def save_user_to_pool(self):
        """Save user data to global pool (using info after password reset)"""
        user_data = {
            "userId": self.user_info.user_id,
            "username": self.user_info.nickname,
            "email": self.user_info.email,
            "token": self.user_info.token,
            "password": "NewPass123456!"  # Save new password after reset
        }

        with user_lock:
            registered_users.append(user_data)
            print(f"💾 [{self.credentials.nickname}] Saved to user pool (total: {len(registered_users)})")


# ============================================
# Locust Event Handlers
# ============================================

@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    """Print info when test starts"""
    print("\n" + "=" * 60)
    print("🚀 Phase 1: User Module Load Test Started (Refactored)")
    print("=" * 60)
    print("Using modular architecture:")
    print("  - UserService for business logic")
    print("  - ApiClient for HTTP requests")
    print("  - TestDataGenerator for unique data")
    print("=" * 60)
    print("Target: Register users and generate user pool")
    print("=" * 60 + "\n")


@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    """Save user data to file when test ends"""
    output_file = "phase1_users.json"

    print("\n" + "=" * 60)
    print("📊 Saving user data...")
    print(f"   Collected users: {len(registered_users)}")
    print("=" * 60)

    # Use UserPool to save data
    user_pool = UserPool(output_file)
    user_pool.users = registered_users
    user_pool.save_users()

    print("\n" + "=" * 60)
    print("🎉 Phase 1: User Module Load Test Completed!")
    print("=" * 60)
    print(f"✅ Successfully registered: {success_count} users")
    print(f"❌ Failed: {fail_count} users")
    print(f"💾 Total users saved to {output_file}: {len(registered_users)}")
    print("=" * 60)

    # Show first 3 users as examples
    if registered_users:
        for i, user in enumerate(registered_users[:3], 1):
            print(f"   {i}. {user['username']} ({user['email']})")

    print("\n📋 Next Steps:")
    print("   1. Check phase1_users.json for all user data")
    print("   2. All users have password: NewPass123456!")
    print("   3. Run Phase 2: locust -f phase2_matching_room.py")
    print("=" * 60 + "\n")


# ============================================
# Usage Instructions
# ============================================
if __name__ == "__main__":
    import sys
    import io

    # Fix Windows console encoding
    if sys.platform == 'win32':
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

    print("=" * 60)
    print("Phase 1: User Module Load Test (Refactored)")
    print("=" * 60)
    print()
    print("✨ Using modular architecture:")
    print("  - services/user_service.py")
    print("  - utils/api_client.py")
    print("  - utils/test_data.py")
    print()
    print("This test will:")
    print("  1. Register new users")
    print("  2. Test login and token verification")
    print("  3. Generate phase1_users.json for next phases")
    print()
    print("Run with:")
    print("  locust -f phase1_user_module.py \\")
    print("    --host=https://test-api-gomoku.goodyhao.me \\")
    print("    --users=100 \\")
    print("    --spawn-rate=10 \\")
    print("    --run-time=5m \\")
    print("    --headless")
    print()
    print("=" * 60)
