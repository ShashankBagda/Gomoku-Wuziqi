"""
Test Logger Module
Test logging utility
"""

import sys
import io


class TestLogger:
    """Test logger"""

    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

    def __init__(self, use_color: bool = True):
        """
        Initialize logger

        Args:
            use_color: Whether to use colored output
        """
        self.use_color = use_color
        # Fix Windows console encoding for emojis
        if sys.platform == 'win32':
            sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

    def _colorize(self, text: str, color: str) -> str:
        """Add color"""
        if self.use_color:
            return f"{color}{text}{self.ENDC}"
        return text

    def test_start(self, name: str):
        """Test start"""
        print()
        print("=" * 60)
        print(self._colorize(f"🐛 {name}", self.HEADER + self.BOLD))
        print("=" * 60)

    def test_complete(self, message: str = "Test completed!"):
        """Test complete"""
        print()
        print("=" * 60)
        print(self._colorize(f"🎉 {message}", self.OKGREEN + self.BOLD))
        print("=" * 60)
        print()

    def divider(self):
        """Divider line"""
        print("=" * 60)

    def newline(self):
        """Empty line"""
        print()

    def step(self, message: str):
        """Step info"""
        print(self._colorize(f"▶ {message}", self.OKCYAN))

    def success(self, message: str):
        """Success message"""
        print(self._colorize(f"✅ {message}", self.OKGREEN))

    def error(self, message: str):
        """Error message"""
        print(self._colorize(f"❌ {message}", self.FAIL))

    def warning(self, message: str):
        """Warning message"""
        print(self._colorize(f"⚠️  {message}", self.WARNING))

    def info(self, message: str):
        """Info message"""
        print(f"   {message}")

    def debug(self, message: str):
        """Debug message"""
        print(self._colorize(f"   [DEBUG] {message}", self.OKBLUE))
