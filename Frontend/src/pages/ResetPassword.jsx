import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { toast } from "react-toastify";
import BackgroundAnimation from "../components/BackgroundAnimation";
import { userApi } from "../api";
import { clearAuthData } from "../utils/auth";

export default function ResetPassword() {
  const navigate = useNavigate();
  const location = useLocation();

  // Auto-fill email if passed from Profile page
  const prefillEmail = location.state?.email || "";

  // Form fields
  const [email, setEmail] = useState(prefillEmail);
  const [verificationCode, setVerificationCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [sendingCode, setSendingCode] = useState(false);
  const [codeSent, setCodeSent] = useState(false);
  const [error, setError] = useState("");
  const [countdown, setCountdown] = useState(0);

  // Send verification code
  const handleSendCode = async () => {
    const trimmedEmail = email.trim();
    if (!trimmedEmail) {
      toast.error("Please enter your email address");
      return;
    }

    setSendingCode(true);
    try {
      // Call send verification code API
      await userApi.sendVerificationCode({ email: trimmedEmail });

      toast.success("Verification code sent to your email!");
      setCodeSent(true);

      // Start countdown
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err) {
      const errorMsg = err.errorMsg || err.message || "Failed to send verification code";
      toast.error(errorMsg);
    } finally {
      setSendingCode(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const trimmedEmail = email.trim();
      const trimmedCode = verificationCode.trim();
      const trimmedPassword = newPassword.trim();
      const trimmedConfirm = confirmPassword.trim();

      // Validation
      if (!trimmedEmail) {
        throw new Error("Email cannot be empty");
      }
      if (!trimmedCode) {
        throw new Error("Verification code cannot be empty");
      }
      if (!trimmedPassword) {
        throw new Error("New password cannot be empty");
      }
      if (trimmedPassword !== trimmedConfirm) {
        throw new Error("Passwords do not match");
      }
      if (trimmedPassword.length < 6) {
        throw new Error("Password must be at least 6 characters");
      }

      const token =
        typeof window !== "undefined" && window?.localStorage
          ? localStorage.getItem("token")
          : null;
      const storedUserId =
        typeof window !== "undefined" && window?.localStorage
          ? localStorage.getItem("userId")
          : null;

      if (!token || !storedUserId) {
        throw new Error("You need to be signed in to reset your password.");
      }

      // Call reset password API
      await userApi.resetPassword({
        email: trimmedEmail,
        verificationCode: trimmedCode,
        newPassword: trimmedPassword,
        userId: storedUserId,
      });

      // Clear any existing auth data
      clearAuthData();

      toast.success("Password reset successfully! Please login with your new password.");
      navigate("/login");
    } catch (err) {
      const errorMsg = err.errorMsg || err.message || "Failed to reset password";
      setError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen flex flex-col items-center justify-center overflow-hidden bg-[#F1F1F1] dark:bg-[#0D1B2A] text-[#2F2F2F] dark:text-gray-100">
      <BackgroundAnimation stoneCount={24} intensity={10} />

      {/* Header */}
      <div className="relative z-10 text-center mb-8">
        <h1 className="text-5xl sm:text-6xl font-extrabold tracking-wide mb-2">
          <span className="text-[#EF7C00]">G</span>
          <span className="text-[#003D7C] dark:text-[#FFD166]">omoku</span>
        </h1>
        <p className="text-gray-600 dark:text-gray-400 text-sm sm:text-base">
          Reset your password to regain access to your account.
        </p>
      </div>

      {/* Reset Password Card */}
      <div className="relative z-10 bg-white dark:bg-[#0F2538] rounded-2xl shadow-2xl p-8 w-full max-w-md border border-[#EF7C00]/30 backdrop-blur-md">
        <h2 className="text-2xl font-bold text-center mb-6 bg-gradient-to-r from-[#003D7C] to-[#EF7C00] bg-clip-text text-transparent">
          Reset Password
        </h2>

        <form onSubmit={handleResetPassword} className="flex flex-col gap-5">
          {error && (
            <div className="text-sm text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 px-3 py-2 rounded">
              {error}
            </div>
          )}

          {/* Email */}
          <div>
            <label className="text-sm font-medium mb-1 block text-gray-600 dark:text-gray-300">
              Email Address
            </label>
            <input
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full px-3 py-2 border-2 border-gray-300 dark:border-gray-700 rounded-md bg-gray-50 dark:bg-gray-800 focus:ring-2 focus:ring-[#EF7C00] focus:outline-none transition-all"
            />
          </div>

          {/* Verification Code */}
          <div>
            <label className="text-sm font-medium mb-1 block text-gray-600 dark:text-gray-300">
              Verification Code
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Enter 6-digit code"
                value={verificationCode}
                onChange={(e) => setVerificationCode(e.target.value)}
                required
                maxLength={6}
                className="flex-1 px-3 py-2 border-2 border-gray-300 dark:border-gray-700 rounded-md bg-gray-50 dark:bg-gray-800 focus:ring-2 focus:ring-[#EF7C00] focus:outline-none transition-all"
              />
              <button
                type="button"
                onClick={handleSendCode}
                disabled={sendingCode || countdown > 0}
                className="px-4 py-2 bg-[#003D7C] hover:bg-[#EF7C00] text-white text-sm font-semibold rounded-md transition-all disabled:opacity-60 disabled:cursor-not-allowed whitespace-nowrap"
              >
                {sendingCode
                  ? "Sending..."
                  : countdown > 0
                  ? `${countdown}s`
                  : codeSent
                  ? "Resend"
                  : "Send Code"}
              </button>
            </div>
            {codeSent && (
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Check your email for the verification code
              </p>
            )}
          </div>

          {/* New Password */}
          <div>
            <label className="text-sm font-medium mb-1 block text-gray-600 dark:text-gray-300">
              New Password
            </label>
            <input
              type="password"
              placeholder="••••••••"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              minLength={6}
              className="w-full px-3 py-2 border-2 border-gray-300 dark:border-gray-700 rounded-md bg-gray-50 dark:bg-gray-800 focus:ring-2 focus:ring-[#003D7C] focus:outline-none transition-all"
            />
          </div>

          {/* Confirm Password */}
          <div>
            <label className="text-sm font-medium mb-1 block text-gray-600 dark:text-gray-300">
              Confirm Password
            </label>
            <input
              type="password"
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              minLength={6}
              className="w-full px-3 py-2 border-2 border-gray-300 dark:border-gray-700 rounded-md bg-gray-50 dark:bg-gray-800 focus:ring-2 focus:ring-[#003D7C] focus:outline-none transition-all"
            />
          </div>

          {/* Button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-gradient-to-r from-[#003D7C] to-[#EF7C00] hover:from-[#EF7C00] hover:to-[#FFD166] text-white font-semibold py-2 rounded-md shadow-md hover:shadow-lg transition-all duration-300 disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? "Resetting..." : "Reset Password"}
          </button>
        </form>

        <p className="text-sm text-center mt-6 text-gray-600 dark:text-gray-400">
          Remember your password?{" "}
          <span
            onClick={() => navigate("/login")}
            className="text-[#EF7C00] hover:text-[#FFD166] cursor-pointer font-semibold transition"
          >
            Sign In
          </span>
        </p>
      </div>
    </div>
  );
}
