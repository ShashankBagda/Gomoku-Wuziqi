import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import BackgroundAnimation from "../components/BackgroundAnimation";
import { toast } from "react-toastify";
import { login as loginUser } from "../utils/auth";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const data = await loginUser({ username: email, password });
      toast.success(`Welcome back, ${data?.nickname || "Player"}!`);
      navigate("/");
    } catch (err) {
      const errorMsg = err.errorMsg || err.message || "Login failed";
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
          Log in to continue your Gomoku journey.
        </p>
      </div>

      {/* Login Card */}
      <div className="relative z-10 bg-white dark:bg-[#0F2538] rounded-2xl shadow-2xl p-8 w-full max-w-md border border-[#EF7C00]/30 backdrop-blur-md">
        <h2 className="text-2xl font-bold text-center mb-6 bg-gradient-to-r from-[#003D7C] to-[#EF7C00] bg-clip-text text-transparent">
          Sign In
        </h2>

        <form onSubmit={handleLogin} className="flex flex-col gap-5">
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

          {/* Password */}
          <div>
            <label className="text-sm font-medium mb-1 block text-gray-600 dark:text-gray-300">
              Password
            </label>
            <input
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="w-full px-3 py-2 border-2 border-gray-300 dark:border-gray-700 rounded-md bg-gray-50 dark:bg-gray-800 focus:ring-2 focus:ring-[#003D7C] focus:outline-none transition-all"
            />
          </div>

          {/* Forgot Password */}
          <div className="flex justify-end">
            <button
              type="button"
              onClick={() => navigate("/reset-password")}
              className="text-xs text-[#003D7C] dark:text-[#FFD166] hover:text-[#EF7C00] transition"
            >
              Forgot Password?
            </button>
          </div>

          {/* Sign In Button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-gradient-to-r from-[#003D7C] to-[#EF7C00] hover:from-[#EF7C00] hover:to-[#FFD166] text-white font-semibold py-2 rounded-md shadow-md hover:shadow-lg transition-all duration-300 disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>

        {/* Sign Up Link */}
        <p className="text-sm text-center mt-6 text-gray-600 dark:text-gray-400">
          Don't have an account?{" "}
          <span
            onClick={() => navigate("/signup")}
            className="text-[#EF7C00] hover:text-[#FFD166] cursor-pointer font-semibold transition"
          >
            Sign Up
          </span>
        </p>
      </div>

    </div>
  );
}
