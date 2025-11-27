import React from "react";
import { useNavigate } from "react-router-dom";

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="relative min-h-screen w-full bg-[#F1F1F1] dark:bg-[#0D1B2A] text-gray-900 dark:text-gray-100 flex flex-col items-center justify-center px-6 text-center">
      <h1 className="text-6xl sm:text-7xl font-extrabold mb-4 bg-gradient-to-r from-[#003D7C] to-[#EF7C00] bg-clip-text text-transparent drop-shadow">
        404
      </h1>
      <p className="text-lg sm:text-xl mb-8 max-w-xl text-gray-600 dark:text-gray-300">
        Oops! The page you are looking for has drifted off the Gomoku board. Let’s get you back into the game.
      </p>
      <div className="flex flex-wrap items-center justify-center gap-4">
        <button
          onClick={() => navigate("/")}
          className="px-6 py-2 rounded-md bg-[#003D7C] hover:bg-[#EF7C00] text-white font-semibold shadow-md transition"
        >
          Return Home
        </button>
        <button
          onClick={() => navigate("/room")}
          className="px-6 py-2 rounded-md border-2 border-[#EF7C00] text-[#EF7C00] hover:bg-[#EF7C00] hover:text-white font-semibold shadow-md transition"
        >
          Join Game Room
        </button>
      </div>
    </div>
  );
}
