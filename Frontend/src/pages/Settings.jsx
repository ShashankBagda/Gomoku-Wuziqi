import React, { useState } from "react";
import SectionCard from "../components/SectionCard";
import Toggle from "../components/Toggle";
import { useSettings } from "../context/SettingsContext";
import BackgroundAnimation from "../components/BackgroundAnimation";

export default function Settings() {
  const {
    dark,
    setDark,
    sound,
    setSound,
    music,
    setMusic,
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
  } = useSettings();

  const [activePicker, setActivePicker] = useState(null); // "black" | "white" | null

  const themePresets = [
    { name: "Classic NUS", board: "#F3F4F6", black: "#EF7C00", white: "#003D7C" },
    { name: "Ocean Blue", board: "#B8D8FF", black: "#00254C", white: "#EAF4FF" },
    { name: "Forest Wood", board: "#DDB892", black: "#3A2618", white: "#F5F3E0" },
    { name: "Midnight Glow", board: "#0D1B2A", black: "#EF7C00", white: "#F1F1F1" },
    { name: "Candy Pop", board: "#FFE5EC", black: "#FF477E", white: "#1A1A1A" },
    { name: "Royal Onyx", board: "#1A1A2E", black: "#0F3460", white: "#E94560" },
    { name: "Autumn Field", board: "#F5DEB3", black: "#2B2118", white: "#C84B31" },
    { name: "Crimson Sky", board: "#FFE5E5", black: "#C1121F", white: "#001F3F" },
    { name: "Lime Punch", board: "#D9ED92", black: "#004B23", white: "#1A759F" },
    { name: "Coral Reef", board: "#FEC5BB", black: "#9B2226", white: "#001219" },
    { name: "Sunset Gold", board: "#FFF4E0", black: "#EE6C4D", white: "#1D3557" },
    { name: "Cyber Grid", board: "#0D0D0D", black: "#00FFDD", white: "#FF006E" },
    { name: "Slate Mist", board: "#E0E1DD", black: "#415A77", white: "#FCA311" },
    { name: "Galaxy Warp", board: "#0B132B", black: "#6FFFE9", white: "#FF3366" },
    { name: "Emerald Dream", board: "#D8F3DC", black: "#081C15", white: "#40916C" },
    { name: "Amber Ice", board: "#FDFCDC", black: "#114B5F", white: "#FF6B35" },
    { name: "Lava Stone", board: "#FAF3E0", black: "#8B0000", white: "#F3E9D2" },
    { name: "Frost Nova", board: "#E0FBFC", black: "#3D5A80", white: "#EE6C4D" },
    { name: "Aqua Steel", board: "#E8F1F2", black: "#14213D", white: "#FCA311" },
    { name: "Obsidian Neon", board: "#121212", black: "#00E5FF", white: "#FF9100" },
  ];

  const applyPreset = (preset) => {
    setBoardColor(preset.board);
    setBlackStone(preset.black);
    setWhiteStone(preset.white);
  };

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-[#F1F1F1] dark:bg-gray-900 text-[#2F2F2F] dark:text-gray-100">
      <BackgroundAnimation stoneCount={26} intensity={12} />

      <div className="relative z-10 flex flex-col items-center px-4 py-10">
        <h1 className="text-4xl sm:text-5xl font-extrabold text-center mb-8 bg-gradient-to-r from-[#003D7C] to-[#EF7C00] bg-clip-text text-transparent">
        Game Settings
        </h1>

      {/* AUDIO SETTINGS */}
      <SectionCard title="Audio Settings" className="mb-6 w-full max-w-2xl bg-white/90 dark:bg-gray-800 rounded-xl p-6 shadow-lg">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-4">
          <AudioToggle label="Sound Effects" value={sound} onChange={setSound} />
          <AudioToggle label="Background Music" value={music} onChange={setMusic} />
        </div>
        <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 gap-6">
          <VolumeSlider label="SFX Volume" storageKey="sfx-volume" settingsKey="sfxVolume" />
          <VolumeSlider label="Music Volume" storageKey="music-volume" settingsKey="musicVolume" />
        </div>
      </SectionCard>

      {/* APPEARANCE SETTINGS */}
      <SectionCard title="Appearance Settings" className="mb-6 w-full max-w-2xl bg-white/90 dark:bg-gray-800 rounded-xl p-6 shadow-lg">
        <p className="text-gray-600 dark:text-gray-300 mb-3 text-sm">
          Customize the board and theme colors.
        </p>
        <Toggle checked={dark} onChange={setDark} label="Dark Mode" />
        <div className="flex justify-center items-center mt-8">
          <MiniBoardPreview
            boardColor={boardColor}
            blackStone={blackStone}
            whiteStone={whiteStone}
            styleType={stoneStyle}
            blackEmoji={blackEmoji}
            whiteEmoji={whiteEmoji}
            onStoneClick={(type) => setActivePicker(type)}
          />
        </div>
      </SectionCard>

      {/* STONE DESIGN */}
      <SectionCard title="Stone Design & Style" className="mb-6 w-full max-w-2xl bg-white/90 dark:bg-gray-800 rounded-xl p-6 shadow-lg">
        <div className="flex flex-wrap justify-center gap-4 mt-2">
          {["classic", "emoji", "pattern"].map((s) => (
            <button
              key={s}
              onClick={() => setStoneStyle(s)}
              className={`px-4 py-2 rounded-md border-2 text-sm font-semibold transition-all duration-300 ${
                stoneStyle === s
                  ? "border-[#EF7C00] text-[#EF7C00]"
                  : "border-gray-400 hover:border-[#EF7C00]"
              }`}
            >
              {s === "classic" && "🎯 Classic"}
              {s === "emoji" && "😀 Emoji"}
              {s === "pattern" && "🌈 Pattern"}
            </button>
          ))}
        </div>

        {stoneStyle === "emoji" && (
          <div className="flex justify-center items-center gap-6 mt-6 relative">
            <EmojiSelector
              label="Stone 1"
              emoji={blackEmoji}
              setEmoji={setBlackEmoji}
              onClick={() => setActivePicker("black")}
            />
            <EmojiSelector
              label="Stone 2"
              emoji={whiteEmoji}
              setEmoji={setWhiteEmoji}
              onClick={() => setActivePicker("white")}
            />
          </div>
        )}
      </SectionCard>

      {/* THEME PRESETS */}
      <SectionCard title="Theme Presets" className="w-full max-w-3xl bg-white/90 dark:bg-gray-800 rounded-xl p-6 shadow-lg">
        <p className="text-gray-600 dark:text-gray-300 mb-3 text-sm">
          Choose from 20 curated visual themes.
        </p>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 mt-4">
          {themePresets.map((preset) => (
            <button
              key={preset.name}
              onClick={() => applyPreset(preset)}
              className="flex flex-col items-center p-3 border-2 border-gray-300 dark:border-gray-600 rounded-lg hover:scale-105 hover:border-[#EF7C00] transition-transform duration-300 shadow-sm hover:shadow-md"
              title={preset.name}
            >
              <MiniBoardPreview
                boardColor={preset.board}
                blackStone={preset.black}
                whiteStone={preset.white}
                styleType={stoneStyle}
                blackEmoji={blackEmoji}
                whiteEmoji={whiteEmoji}
                onStoneClick={(type) => setActivePicker(type)}
                size={32}
              />
              <span className="text-xs mt-2 text-gray-700 dark:text-gray-300 text-center">
                {preset.name}
              </span>
            </button>
          ))}
        </div>
      </SectionCard>

      {/* Emoji Picker Popup */}
      {activePicker && (
        <EmojiPicker
          onSelect={(emoji) => {
            activePicker === "black" ? setBlackEmoji(emoji) : setWhiteEmoji(emoji);
            setActivePicker(null);
          }}
          onClose={() => setActivePicker(null)}
        />
      )}
      </div>
    </div>
  );
}

/* 🔊 Audio Toggle */
function AudioToggle({ label, value, onChange }) {
  return (
    <div className="flex flex-col items-center justify-center bg-gray-100/40 dark:bg-gray-700/40 rounded-lg py-3 px-3 hover:bg-gray-200/50 dark:hover:bg-gray-700/60 transition-all duration-300">
      <span className="text-sm font-medium mb-2">{label}</span>
      <Toggle checked={value} onChange={onChange} />
    </div>
  );
}

/* 🔊 Volume Slider */
function VolumeSlider({ label, storageKey, settingsKey }) {
  const ctx = useSettings();
  const value = Math.round(((ctx[settingsKey] ?? 0.3) * 100));
  const onInput = (e) => {
    const v = Math.min(100, Math.max(0, Number(e.target.value) || 0));
    const f = v / 100;
    if (settingsKey && typeof ctx[`set${settingsKey.charAt(0).toUpperCase()}${settingsKey.slice(1)}`] === 'function') {
      ctx[`set${settingsKey.charAt(0).toUpperCase()}${settingsKey.slice(1)}`](f);
    }
    localStorage.setItem(storageKey, String(f));
  };
  return (
    <div className="flex flex-col">
      <div className="flex items-center justify-between mb-1">
        <span className="text-sm font-medium">{label}</span>
        <span className="text-xs text-gray-600 dark:text-gray-300">{value}%</span>
      </div>
      <input type="range" min="0" max="100" value={value} onChange={onInput} className="w-full accent-[#EF7C00]" />
    </div>
  );
}

/* 😀 Emoji Selector Input */
function EmojiSelector({ label, emoji, setEmoji, onClick }) {
  return (
    <div className="flex flex-col items-center">
      <span className="text-sm mb-2">{label}</span>
      <button
        onClick={onClick}
        className="w-12 h-12 text-2xl border-2 border-gray-400 dark:border-gray-600 rounded-md bg-gray-50 dark:bg-gray-800 hover:border-[#EF7C00] transition-all"
      >
        {emoji}
      </button>
    </div>
  );
}

/* 🧩 Mini 2x2 Preview */
function MiniBoardPreview({ boardColor, blackStone, whiteStone, styleType, blackEmoji, whiteEmoji, onStoneClick, size = 40 }) {
  const cellStyle = {
    width: `${size}px`,
    height: `${size}px`,
    backgroundColor: boardColor,
    border: "1px solid rgba(0,0,0,0.2)",
  };
  return (
    <div className="grid grid-cols-2 grid-rows-2 rounded-md overflow-hidden shadow-inner border border-gray-400 dark:border-gray-700">
      <div style={cellStyle}>
        <Stone color={blackStone} type={styleType} emoji={blackEmoji} onClick={() => onStoneClick("black")} />
      </div>
      <div style={cellStyle}></div>
      <div style={cellStyle}></div>
      <div style={cellStyle}>
        <Stone color={whiteStone} type={styleType} emoji={whiteEmoji} onClick={() => onStoneClick("white")} />
      </div>
    </div>
  );
}

/* 🪨 Stone Display */
function Stone({ color, type, emoji, onClick }) {
  if (type === "emoji") {
    return (
      <button
        onClick={onClick}
        className="flex items-center justify-center w-full h-full text-2xl hover:scale-110 transition-transform"
      >
        {emoji}
      </button>
    );
  }
  const gradient = type === "pattern"
    ? `radial-gradient(circle at 30% 30%, ${color} 30%, #00000040 90%)`
    : color;
  return (
    <div
      onClick={onClick}
      className="rounded-full mx-auto my-auto transition-transform duration-300 hover:scale-110"
      style={{
        background: gradient,
        width: "60%",
        height: "60%",
        marginTop: "20%",
        boxShadow: `0 0 6px ${color}80`,
      }}
    ></div>
  );
}

/* 🧃 Emoji Picker Menu */
function EmojiPicker({ onSelect, onClose }) {
  const emojis = [
    "⚫", "⚪", "🔥", "💧", "💎", "🌸", "🍀", "⭐", "💥", "❄️",
    "☀️", "🌙", "⚡", "❤️", "💙", "🦊", "🐼", "🐉", "🪨", "⚙️"
  ];
  return (
    <div className="fixed inset-0 flex justify-center items-center bg-black/40 backdrop-blur-sm z-50">
      <div className="bg-white dark:bg-gray-800 rounded-xl p-5 shadow-xl w-[320px] max-w-[90%]">
        <h3 className="text-lg font-semibold text-center mb-3">Select Emoji</h3>
        <div className="grid grid-cols-5 gap-3 text-2xl text-center">
          {emojis.map((e) => (
            <button
              key={e}
              onClick={() => onSelect(e)}
              className="hover:scale-125 transition-transform"
            >
              {e}
            </button>
          ))}
        </div>
        <button
          onClick={onClose}
          className="mt-4 w-full py-2 bg-[#003D7C] text-white rounded-md hover:bg-[#EF7C00] transition"
        >
          Close
        </button>
      </div>
    </div>
  );
}
