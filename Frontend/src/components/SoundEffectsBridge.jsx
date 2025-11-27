import { useEffect, useRef } from "react";
import { useSettings } from "../context/SettingsContext";

const CLICK_SOUND_SRC = "/sounds/pop.mp3";

export default function SoundEffectsBridge() {
  const { sound, sfxVolume } = useSettings();
  const audioRef = useRef(null);

  useEffect(() => {
    const baseAudio = new Audio(CLICK_SOUND_SRC);
    baseAudio.volume = Math.min(1, Math.max(0, sfxVolume ?? 0.3));
    audioRef.current = baseAudio;

    const handleClick = (event) => {
      if (!sound) return;

      const target = event.target;
      if (!(target instanceof Element)) return;

      // Only fire for interactive elements
      const interactive = target.closest(
        "button, [role='button'], a, input, select, textarea, summary"
      );
      if (!interactive) return;

      const audio = baseAudio.cloneNode(true);
      audio.volume = baseAudio.volume;
      audio.play().catch(() => {});
    };

    document.addEventListener("click", handleClick);
    return () => document.removeEventListener("click", handleClick);
  }, [sound, sfxVolume]);

  return null;
}
