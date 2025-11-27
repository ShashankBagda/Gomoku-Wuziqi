import React, { useEffect, useState } from "react";
import { X } from "lucide-react";

export default function InstallPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const handler = (e) => {
      e.preventDefault();
      // Only show on Home route and only once
      const onHome = window.location.pathname === "/";
      const dismissed = localStorage.getItem("installPromptDismissed") === "1";
      const shown = localStorage.getItem("installPromptShown") === "1";
      if (!onHome || dismissed || shown) return;
      setDeferredPrompt(e);
      setVisible(true);
      localStorage.setItem("installPromptShown", "1");
    };
    window.addEventListener("beforeinstallprompt", handler);
    return () => window.removeEventListener("beforeinstallprompt", handler);
  }, []);

  const handleInstall = async () => {
    if (!deferredPrompt) return;
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    if (outcome === "accepted") console.log("✅ App installed!");
    setVisible(false);
  };

  const handleClose = () => {
    setVisible(false);
    setDeferredPrompt(null);
    localStorage.setItem("installPromptDismissed", "1");
  };

  useEffect(() => {
    if (localStorage.getItem("installPromptDismissed") === "1") setVisible(false);
  }, []);

  if (!visible) return null;

  return (
    <div
      className="
        fixed bottom-6 right-6 sm:right-6 sm:bottom-6
        left-1/2 sm:left-auto -translate-x-1/2 sm:translate-x-0
        bg-[#003D7C] text-white px-4 py-3 rounded-lg shadow-xl
        z-50 w-[90%] sm:w-64 max-w-xs animate-fadeIn
        border border-[#EF7C00]/50
      "
    >
      {/* ❌ Close Icon */}
      <button
        onClick={handleClose}
        className="absolute top-1.5 right-1.5 p-1 text-white/70 hover:text-white transition"
        aria-label="Close"
      >
        <X size={16} />
      </button>

      <p className="text-sm font-semibold mb-2 text-center sm:text-left">
        Install Gomoku App
      </p>
      <button
        onClick={handleInstall}
        className="w-full bg-[#EF7C00] hover:bg-[#FFD166] text-black font-semibold px-3 py-1.5 rounded-md text-xs transition"
      >
        Install
      </button>
    </div>
  );
}
