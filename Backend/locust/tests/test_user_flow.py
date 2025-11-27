"""
User Flow Test Module
用户流程测试：封装测试逻辑和断言
"""

from typing import Optional
from dataclasses import dataclass
from services.user_service import UserService, UserCredentials, UserInfo
from utils.test_logger import TestLogger


@dataclass
class UserFlowTestResult:
    """用户流程测试结果"""
    success: bool
    user_info: Optional[UserInfo] = None
    error_step: Optional[str] = None
    error_message: Optional[str] = None


class UserFlowTester:
    """用户流程测试器"""

    def __init__(self, user_service: UserService, logger: TestLogger):
        """
        初始化测试器

        Args:
            user_service: 用户服务实例
            logger: 测试日志记录器
        """
        self.service = user_service
        self.logger = logger

    def test_registration_flow(self, credentials: UserCredentials) -> UserFlowTestResult:
        """
        测试完整的用户注册流程

        Args:
            credentials: 用户凭证

        Returns:
            UserFlowTestResult 测试结果
        """
        self.logger.test_start("User Registration Flow")
        self.logger.info(f"Email: {credentials.email}")
        self.logger.info(f"Nickname: {credentials.nickname}")
        self.logger.divider()

        # Step 1: 获取 RSA 公钥
        self.logger.step("Getting RSA public key...")
        public_key = self.service.get_public_key()
        if not public_key:
            self.logger.error("Failed to get public key!")
            return UserFlowTestResult(
                success=False,
                error_step="Get Public Key",
                error_message="Failed to retrieve RSA public key"
            )
        self.logger.success("Got RSA public key")
        self.logger.newline()

        # Step 2: 注册用户
        self.logger.step("Registering user...")
        response = self.service.register(credentials)

        if not response.success:
            self.logger.error("Registration failed!")
            self.logger.error(f"Error Code: {response.error_code}")
            self.logger.error(f"Error Message: {response.error_msg}")
            if response.raw_response:
                self.logger.debug(f"Response: {response.raw_response[:500]}")
            return UserFlowTestResult(
                success=False,
                error_step="Registration",
                error_message=f"[{response.error_code}] {response.error_msg}"
            )

        self.logger.success("Registration successful")
        self.logger.newline()

        # Step 3: 登录
        self.logger.step("Logging in...")
        user_info = self.service.login(credentials.email, credentials.password)

        if not user_info:
            self.logger.error("Login failed!")
            return UserFlowTestResult(
                success=False,
                error_step="Login",
                error_message="Failed to login with registered credentials"
            )

        self.logger.success("Login successful")
        self.logger.info(f"User ID: {user_info.user_id}")
        self.logger.info(f"Token: {user_info.token[:50]}...")
        self.logger.newline()

        # Step 4: 验证 Token
        self.logger.step("Verifying token...")
        is_valid = self.service.verify_token(user_info.token)

        if not is_valid:
            self.logger.error("Token verification failed!")
            return UserFlowTestResult(
                success=False,
                error_step="Verify Token",
                error_message="Token verification failed"
            )

        self.logger.success("Token verified")
        self.logger.newline()

        # 测试完成
        self.logger.test_complete("All steps completed successfully!")
        self.logger.divider()
        self.logger.info("User Data:")
        self.logger.info(f"  Email: {user_info.email}")
        self.logger.info(f"  Nickname: {user_info.nickname}")
        self.logger.info(f"  User ID: {user_info.user_id}")
        self.logger.info(f"  Token: {user_info.token}")
        self.logger.divider()

        return UserFlowTestResult(
            success=True,
            user_info=user_info
        )

    def test_login_only(self, username: str, password: str) -> UserFlowTestResult:
        """
        仅测试登录流程（用于已注册用户）

        Args:
            username: 用户名（email 或 nickname）
            password: 密码

        Returns:
            UserFlowTestResult 测试结果
        """
        self.logger.test_start("User Login Flow")
        self.logger.info(f"Username: {username}")
        self.logger.divider()

        # 登录
        self.logger.step("Logging in...")
        user_info = self.service.login(username, password)

        if not user_info:
            self.logger.error("Login failed!")
            return UserFlowTestResult(
                success=False,
                error_step="Login",
                error_message="Login failed"
            )

        self.logger.success("Login successful")
        self.logger.info(f"User ID: {user_info.user_id}")
        self.logger.newline()

        # 验证 Token
        self.logger.step("Verifying token...")
        is_valid = self.service.verify_token(user_info.token)

        if not is_valid:
            self.logger.error("Token verification failed!")
            return UserFlowTestResult(
                success=False,
                error_step="Verify Token",
                error_message="Token verification failed"
            )

        self.logger.success("Token verified")
        self.logger.test_complete()

        return UserFlowTestResult(
            success=True,
            user_info=user_info
        )

    def test_password_reset(self, email: str, new_password: str) -> UserFlowTestResult:
        """
        测试密码重置流程

        Args:
            email: 用户邮箱
            new_password: 新密码

        Returns:
            UserFlowTestResult 测试结果
        """
        self.logger.test_start("Password Reset Flow")
        self.logger.info(f"Email: {email}")
        self.logger.divider()

        # 重置密码
        self.logger.step("Resetting password...")
        response = self.service.reset_password(email, new_password)

        if not response.success:
            self.logger.error("Password reset failed!")
            self.logger.error(f"Error Code: {response.error_code}")
            self.logger.error(f"Error Message: {response.error_msg}")
            return UserFlowTestResult(
                success=False,
                error_step="Reset Password",
                error_message=f"[{response.error_code}] {response.error_msg}"
            )

        self.logger.success("Password reset successful")
        self.logger.newline()

        # 尝试用新密码登录
        self.logger.step("Testing login with new password...")
        user_info = self.service.login(email, new_password)

        if not user_info:
            self.logger.error("Login with new password failed!")
            return UserFlowTestResult(
                success=False,
                error_step="Login After Reset",
                error_message="Failed to login with new password"
            )

        self.logger.success("Login with new password successful")
        self.logger.test_complete()

        return UserFlowTestResult(
            success=True,
            user_info=user_info
        )
