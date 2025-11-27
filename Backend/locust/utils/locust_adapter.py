"""
Locust Adapter
Adapts Locust's HttpUser.client to standard ApiClient interface
"""


class LocustApiClient:
    """
    Locust adapter for ApiClient
    Wraps Locust's HttpUser.client into ApiClient interface
    """

    def __init__(self, locust_client, base_url: str):
        """
        Initialize adapter

        Args:
            locust_client: Locust's HttpUser.client
            base_url: Base URL
        """
        self.locust_client = locust_client
        self.base_url = base_url.rstrip('/')

    def get(self, endpoint: str, headers: dict = None):
        """Send GET request"""
        from utils.api_client import ApiResponse

        with self.locust_client.get(
            endpoint,
            headers=headers,
            catch_response=True,
            name=f"GET {endpoint}"
        ) as response:
            return self._parse_locust_response(response)

    def post(self, endpoint: str, payload: dict, headers: dict = None):
        """Send POST request"""
        from utils.api_client import ApiResponse

        with self.locust_client.post(
            endpoint,
            json=payload,
            headers=headers,
            catch_response=True,
            name=f"POST {endpoint}"
        ) as response:
            return self._parse_locust_response(response)

    def _parse_locust_response(self, response):
        """Parse Locust response and convert to ApiResponse"""
        from utils.api_client import ApiResponse

        # If HTTP status code is not 2xx, return failure directly
        if response.status_code >= 400:
            response.failure(f"HTTP {response.status_code}")
            return ApiResponse(
                success=False,
                status_code=response.status_code,
                error_msg=f"HTTP {response.status_code}",
                raw_response=response.text[:1000]
            )

        # Try to parse JSON
        try:
            result = response.json()
        except ValueError:
            response.failure("Invalid JSON response")
            return ApiResponse(
                success=False,
                status_code=response.status_code,
                error_msg="Invalid JSON response",
                raw_response=response.text[:1000]
            )

        # Check if business logic succeeded
        business_success = result.get("success", False)

        # Notify Locust whether request succeeded
        if business_success:
            response.success()
        else:
            error_msg = result.get("errorMsg", "Unknown error")
            error_code = result.get("errorCode", "N/A")
            response.failure(f"[{error_code}] {error_msg}")

        return ApiResponse(
            success=business_success,
            status_code=response.status_code,
            data=result.get("data"),
            error_code=result.get("errorCode"),
            error_msg=result.get("errorMsg"),
            raw_response=response.text if not business_success else None
        )
