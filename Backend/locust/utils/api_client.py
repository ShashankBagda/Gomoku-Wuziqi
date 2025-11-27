"""
API Client Module
Provides unified API interface, encapsulating request and response handling logic
"""

import requests
from typing import Dict, Optional, Any
from dataclasses import dataclass


@dataclass
class ApiResponse:
    """API response data class"""
    success: bool
    status_code: int
    data: Optional[Any] = None
    error_code: Optional[int] = None
    error_msg: Optional[str] = None
    raw_response: Optional[str] = None


class ApiClient:
    """Unified API client"""

    def __init__(self, base_url: str, timeout: int = 10):
        """
        Initialize API client

        Args:
            base_url: API base URL
            timeout: Request timeout in seconds
        """
        self.base_url = base_url.rstrip('/')
        self.timeout = timeout
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})

    def get(self, endpoint: str, headers: Optional[Dict[str, str]] = None) -> ApiResponse:
        """
        Send GET request

        Args:
            endpoint: API endpoint (e.g., /api/user/verify)
            headers: Additional headers

        Returns:
            ApiResponse object
        """
        url = f"{self.base_url}{endpoint}"
        try:
            response = requests.get(
                url,
                headers=headers,
                timeout=self.timeout
            )
            return self._parse_response(response)
        except requests.RequestException as e:
            return ApiResponse(
                success=False,
                status_code=0,
                error_msg=f"Request failed: {str(e)}"
            )

    def post(self, endpoint: str, payload: Dict, headers: Optional[Dict[str, str]] = None) -> ApiResponse:
        """
        Send POST request

        Args:
            endpoint: API endpoint (e.g., /api/user/login)
            payload: Request body data
            headers: Additional headers

        Returns:
            ApiResponse object
        """
        url = f"{self.base_url}{endpoint}"
        try:
            response = requests.post(
                url,
                json=payload,
                headers=headers,
                timeout=self.timeout
            )
            return self._parse_response(response)
        except requests.RequestException as e:
            return ApiResponse(
                success=False,
                status_code=0,
                error_msg=f"Request failed: {str(e)}"
            )

    def _parse_response(self, response: requests.Response) -> ApiResponse:
        """
        Parse HTTP response

        Args:
            response: requests.Response object

        Returns:
            ApiResponse object
        """
        raw_text = response.text

        # If HTTP status code is not 2xx, return failure directly
        if response.status_code >= 400:
            return ApiResponse(
                success=False,
                status_code=response.status_code,
                error_msg=f"HTTP {response.status_code}",
                raw_response=raw_text[:1000]
            )

        # Try to parse JSON
        try:
            result = response.json()
        except ValueError:
            return ApiResponse(
                success=False,
                status_code=response.status_code,
                error_msg="Invalid JSON response",
                raw_response=raw_text[:1000]
            )

        # Check if business logic succeeded
        business_success = result.get("success", False)

        return ApiResponse(
            success=business_success,
            status_code=response.status_code,
            data=result.get("data"),
            error_code=result.get("errorCode"),
            error_msg=result.get("errorMsg"),
            raw_response=raw_text if not business_success else None
        )

    def close(self):
        """Close session"""
        self.session.close()
