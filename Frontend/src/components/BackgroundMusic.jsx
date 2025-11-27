import React, { useEffect, useMemo, useState } from "react";
import { useSettings } from "../context/SettingsContext";

// Keep a single background audio instance across route changes and remounts
const getGlobalBgm = () => {
  if (typeof window === "undefined") return null;
  if (!window.__gomoku_bgm) {
    window.__gomoku_bgm = { audio: null, index: 0, cleanup: null, tryPlayDetach: null };
  }
  return window.__gomoku_bgm;
};

export default function BackgroundMusic() {
  const { music, musicVolume } = useSettings();
  const [, forceTick] = useState(0);

  // 🔹 Dynamically import all files in /assets/bgm
  const tracks = useMemo(
    () => importAll(require.context("../assets/bgm", false, /\.(mp3|wav)$/)),
    []
  );

  function importAll(r) {
    return r.keys().map(r);
  }

  useEffect(() => {
    if (!tracks.length) return;
    const global = getGlobalBgm();
    if (!global) return;

    // Create once
    if (!global.audio) {
      const a = new Audio(tracks[global.index % tracks.length]);
      a.preload = "auto";
      a.loop = false; // we manually loop across tracks
      a.volume = Math.min(1, Math.max(0, musicVolume ?? 0.3));

      const handleEnded = () => {
        global.index = (global.index + 1) % tracks.length;
        a.src = tracks[global.index];
        if (music) a.play().catch(() => {});
      };
      a.addEventListener("ended", handleEnded);
      global.cleanup = () => a.removeEventListener("ended", handleEnded);

      const tryPlay = () => { if (music) a.play().catch(() => {}); };
      document.addEventListener("click", tryPlay, { once: true });
      global.tryPlayDetach = () => document.removeEventListener("click", tryPlay);

      a.style.display = "none";
      document.body.appendChild(a);
      global.audio = a;
    }

    // Apply current settings without recreating
    try {
      global.audio.volume = Math.min(1, Math.max(0, musicVolume ?? 0.3));
      if (music) global.audio.play().catch(() => {});
      else global.audio.pause();
    } catch (_) {}

    // Force a render tick (no visible UI)
    forceTick((v) => v + 1);

    return () => {
      // Do not destroy the global audio on unmount to avoid restarts
    };
  }, [music, musicVolume, tracks]);

  return null;
}
