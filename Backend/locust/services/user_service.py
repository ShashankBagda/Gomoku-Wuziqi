"""
User Service Module
Encapsulates user-related business logic
"""

from typing import Optional
from dataclasses import dataclass
from utils.api_client import ApiClient, ApiResponse
from utils.crypto_helper import CryptoHelper


@dataclass
class UserCredentials:
    """User credentials"""
    email: str
    nickname: str
    password: str


@dataclass
class UserInfo:
    """User information"""
    user_id: int
    nickname: str
    email: str
    token: str


class UserService:
    """User service: handles user registration, login, verification, etc."""

    def __init__(self, api_client: ApiClient):
        """
        Initialize user service

        Args:
            api_client: API client instance
        """
        self.api = api_client

    def get_public_key(self) -> Optional[str]:
        """
        Get RSA public key

        Returns:
            Public key string, None on failure
        """
        response = self.api.get("/api/user/public-key")
        if response.success:
            return response.data
        return None

    def register(self, credentials: UserCredentials, verification_code: str = "123456") -> ApiResponse:
        """
        Register a new user

        Args:
            credentials: User credentials
            verification_code: Email verification code (default 123456 in test environment)

        Returns:
            ApiResponse object
        """
        # 1. Get public key
        public_key = self.get_public_key()
        if not public_key:
            return ApiResponse(
                success=False,
                status_code=0,
                error_msg="Failed to get RSA public key"
            )

        # 2. Encrypt password
        try:
            encrypted_password = CryptoHelper.encrypt_password(credentials.password, public_key)
        except Exception as e:
            return ApiResponse(
                success=False,
                status_code=0,
                error_msg=f"Password encryption failed: {str(e)}"
            )

        # 3. Send registration request
        payload = {
            "email": credentials.email,
            "nickname": credentials.nickname,
            "encryptedPassword": encrypted_password,
            "verificationCode": verification_code
        }

        return self.api.post("/api/user/register", payload)

    def login(self, username: str, password: str) -> Optional[UserInfo]:
        """
        User login

        Args:
            username: Username (email or nickname)
            password: Plain text password

        Returns:
            UserInfo on success, None on failure
        """
        # 1. Get public key
        public_key = self.get_public_key()
        if not public_key:
            return None

        # 2. Encrypt password
        try:
            encrypted_password = CryptoHelper.encrypt_password(password, public_key)
        except Exception:
            return None

        # 3. Send login request
        payload = {
            "username": username,
            "encryptedPassword": encrypted_password
        }

        response = self.api.post("/api/user/login", payload)

        if response.success and response.data:
            return UserInfo(
                user_id=response.data["userId"],
                nickname=response.data.get("nickname", username),
                email=response.data.get("email", username),
                token=response.data["token"]
            )

        return None

    def verify_token(self, token: str) -> bool:
        """
        Verify if token is valid

        Args:
            token: JWT Token

        Returns:
            True if token is valid, False otherwise
        """
        response = self.api.get("/api/user/verify", headers={"Authorization": token})
        return response.success

    def reset_password(self, email: str, new_password: str, verification_code: str = "123456") -> ApiResponse:
        """
        Reset password

        Args:
            email: User email
            new_password: New password (plain text)
            verification_code: Email verification code (default 123456 in test environment)

        Returns:
            ApiResponse object
        """
        # 1. Get public key
        public_key = self.get_public_key()
        if not public_key:
            return ApiResponse(
                success=False,
                status_code=0,
                error_msg="Failed to get RSA public key"
            )

        # 2. Encrypt password
        try:
            encrypted_password = CryptoHelper.encrypt_password(new_password, public_key)
        except Exception as e:
            return ApiResponse(
                success=False,
                status_code=0,
                error_msg=f"Password encryption failed: {str(e)}"
            )

        # 3. Send password reset request
        payload = {
            "email": email,
            "encryptedNewPassword": encrypted_password,
            "verificationCode": verification_code
        }

        return self.api.post("/api/user/reset-password", payload)
