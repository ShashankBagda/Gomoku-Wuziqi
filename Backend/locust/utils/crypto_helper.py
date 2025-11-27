"""
Crypto Helper
RSA encryption utility
"""

import base64
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.backends import default_backend


class CryptoHelper:
    """RSA encryption helper"""

    @staticmethod
    def encrypt_password(password: str, public_key_pem: str) -> str:
        """
        Encrypt password using RSA public key

        Args:
            password: Plain text password
            public_key_pem: RSA public key in PEM format

        Returns:
            Base64-encoded encrypted password

        Raises:
            Exception: Raised when encryption fails
        """
        try:
            # Parse PEM format public key
            public_key = serialization.load_pem_public_key(
                public_key_pem.encode('utf-8'),
                backend=default_backend()
            )

            # RSA encryption (using PKCS1 padding to match Java backend default config)
            encrypted = public_key.encrypt(
                password.encode('utf-8'),
                padding.PKCS1v15()
            )

            # Base64 encoding
            return base64.b64encode(encrypted).decode('utf-8')

        except Exception as e:
            raise Exception(f"Password encryption failed: {e}")

    @staticmethod
    def is_valid_pem(public_key_pem: str) -> bool:
        """
        Validate if PEM format public key is valid

        Args:
            public_key_pem: Public key string in PEM format

        Returns:
            True if valid, False otherwise
        """
        try:
            serialization.load_pem_public_key(
                public_key_pem.encode('utf-8'),
                backend=default_backend()
            )
            return True
        except Exception:
            return False
