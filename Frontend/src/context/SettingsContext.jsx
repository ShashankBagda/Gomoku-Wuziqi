import React, { createContext, useContext, useEffect, useMemo, useState } from "react";

const SettingsContext = createContext(null);

export function SettingsProvider({ children }) {
  const [dark, setDark] = useState(() => localStorage.getItem("dark") === "1");
  const [sound, setSound] = useState(
    () => localStorage.getItem("sound-enabled") !== "0"
  );
  const [music, setMusic] = useState(
    () => localStorage.getItem("music-enabled") !== "0"
  );
  const [sfxVolume, setSfxVolume] = useState(() => {
    const v = parseFloat(localStorage.getItem("sfx-volume"));
    return Number.isFinite(v) ? Math.min(1, Math.max(0, v)) : 0.1; // default 10%
  });
  const [musicVolume, setMusicVolume] = useState(() => {
    const v = parseFloat(localStorage.getItem("music-volume"));
    return Number.isFinite(v) ? Math.min(1, Math.max(0, v)) : 0.1; // default 10%
  });

  const [boardColor, setBoardColor] = useState(
    () => localStorage.getItem("board-color") || "#F3F4F6"
  );
  const [blackStone, setBlackStone] = useState(
    () => localStorage.getItem("stone-black") || "#111827"
  );
  const [whiteStone, setWhiteStone] = useState(
    () => localStorage.getItem("stone-white") || "#FFFFFF"
  );

  const [stoneStyle, setStoneStyle] = useState(
    () => localStorage.getItem("stone-style") || "classic"
  );
  const [blackEmoji, setBlackEmoji] = useState(
    () => localStorage.getItem("stone-emoji-black") || "⚫"
  );
  const [whiteEmoji, setWhiteEmoji] = useState(
    () => localStorage.getItem("stone-emoji-white") || "⚪"
  );

  useEffect(() => {
    document.documentElement.classList.toggle("dark", dark);
    localStorage.setItem("dark", dark ? "1" : "0");
  }, [dark]);

  useEffect(() => {
    localStorage.setItem("sound-enabled", sound ? "1" : "0");
  }, [sound]);

  useEffect(() => {
    localStorage.setItem("music-enabled", music ? "1" : "0");
  }, [music]);
  useEffect(() => {
    localStorage.setItem("sfx-volume", String(sfxVolume));
  }, [sfxVolume]);
  useEffect(() => {
    localStorage.setItem("music-volume", String(musicVolume));
  }, [musicVolume]);

  useEffect(() => {
    localStorage.setItem("board-color", boardColor);
  }, [boardColor]);

  useEffect(() => {
    localStorage.setItem("stone-black", blackStone);
  }, [blackStone]);

  useEffect(() => {
    localStorage.setItem("stone-white", whiteStone);
  }, [whiteStone]);

  useEffect(() => {
    localStorage.setItem("stone-style", stoneStyle);
  }, [stoneStyle]);

  useEffect(() => {
    localStorage.setItem("stone-emoji-black", blackEmoji);
  }, [blackEmoji]);

  useEffect(() => {
    localStorage.setItem("stone-emoji-white", whiteEmoji);
  }, [whiteEmoji]);

  const value = useMemo(
    () => ({
      dark,
      setDark,
      sound,
      setSound,
      music,
      setMusic,
      sfxVolume,
      setSfxVolume,
      musicVolume,
      setMusicVolume,
      boardColor,
      setBoardColor,
      blackStone,
      setBlackStone,
      whiteStone,
      setWhiteStone,
      stoneStyle,
      setStoneStyle,
      blackEmoji,
      setBlackEmoji,
      whiteEmoji,
      setWhiteEmoji,
    }),
    [
      dark,
      sound,
      music,
      sfxVolume,
      musicVolume,
      boardColor,
      blackStone,
      whiteStone,
      stoneStyle,
      blackEmoji,
      whiteEmoji,
    ]
  );

  return <SettingsContext.Provider value={value}>{children}</SettingsContext.Provider>;
}

export const useSettings = () => useContext(SettingsContext);
