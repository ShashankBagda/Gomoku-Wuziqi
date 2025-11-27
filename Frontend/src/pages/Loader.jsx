import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import BackgroundAnimation from "../components/BackgroundAnimation";

export default function Loader() {
  const nav = useNavigate();
  useEffect(() => {
    const t = setTimeout(() => nav("/", { replace: true }), 1800);
    return () => clearTimeout(t);
  }, [nav]);

  return (
    <div className="relative min-h-screen flex flex-col items-center justify-center overflow-hidden bg-gradient-to-br from-[#003D7C] via-[#0D1B2A] to-[#EF7C00] text-white">
      <BackgroundAnimation stoneCount={20} intensity={9} showRadial={false} />

      <div className="relative z-10 w-20 h-20">
        <div className="absolute inset-0 border-4 border-white/30 border-t-[#FFD166] rounded-full animate-spin" />
        <div className="absolute inset-3 border-4 border-white/10 border-t-[#EF7C00] rounded-full animate-[spin_3s_linear_infinite]" />
      </div>
      <h1 className="relative z-10 mt-6 text-4xl font-extrabold tracking-wider">GOMOKU</h1>
      <p className="relative z-10 mt-2 text-sm text-gray-200 animate-pulse">Initializing...</p>
    </div>
  );
}
