import React, { useMemo, useState, useEffect } from "react";

export default function StoneLayer({ count = 25, intensity = 10 }) {
  const [offset, setOffset] = useState({ x: 0, y: 0 });
  const [time, setTime] = useState(0);

  // 🎲 Random stones that gently drift
  const stones = useMemo(() => {
    return Array.from({ length: count }).map((_, i) => ({
      id: i,
      baseLeft: Math.random() * 100,
      baseTop: Math.random() * 100,
      color: Math.random() > 0.5 ? "black" : "white",
      size: 12 + Math.random() * 20,
      driftX: Math.random() * 2 - 1,
      driftY: Math.random() * 2 - 1,
      depth: 0.4 + Math.random() * 0.6,
      phase: Math.random() * Math.PI * 2,
    }));
  }, [count]);

  // 🎮 Mouse parallax
  useEffect(() => {
    const handleMouseMove = (e) => {
      const { innerWidth, innerHeight } = window;
      const x = (e.clientX / innerWidth - 0.5) * 2;
      const y = (e.clientY / innerHeight - 0.5) * 2;
      setOffset({ x, y });
    };
    window.addEventListener("mousemove", handleMouseMove);
    return () => window.removeEventListener("mousemove", handleMouseMove);
  }, []);

  // ⏰ Smooth drift over time
  useEffect(() => {
    const interval = setInterval(() => setTime((t) => t + 0.04), 100);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="absolute inset-0 pointer-events-none overflow-hidden z-0">
      {stones.map((stone) => {
        const dx = Math.sin(time + stone.phase) * stone.driftX * 2;
        const dy = Math.cos(time + stone.phase) * stone.driftY * 2;
        const opacity =
          0.25 + Math.abs(Math.sin(time * 0.6 + stone.phase)) * 0.35;

        return (
          <div
            key={stone.id}
            className="absolute rounded-full transition-all duration-700 ease-in-out"
            style={{
              width: `${stone.size}px`,
              height: `${stone.size}px`,
              backgroundColor:
                stone.color === "black" ? "#EF7C00" : "#4B8CD9",
              left: `calc(${stone.baseLeft + dx}% + ${
                offset.x * stone.depth * intensity
              }px)`,
              top: `calc(${stone.baseTop + dy}% + ${
                offset.y * stone.depth * intensity
              }px)`,
              opacity,
              filter: "blur(0.8px)",
              boxShadow:
                stone.color === "black"
                  ? "0 0 10px rgba(239,124,0,0.6)"
                  : "0 0 10px rgba(75,140,217,0.6)",
            }}
          />
        );
      })}

      {/* ✨ Floating colorful ribbons */}
      <RibbonLayer />
    </div>
  );
}

function RibbonLayer() {
  const [ribbons, setRibbons] = useState([]);

  useEffect(() => {
    const spawnRibbon = () => {
      const id = Date.now();
      const hue = Math.random() * 360;
      const startX = Math.random() * 100;
      const endX = Math.random() * 100;
      const direction = Math.random() > 0.5 ? 1 : -1; // flow up or down

      setRibbons((prev) => [
        ...prev.slice(-6),
        { id, hue, startX, endX, direction },
      ]);
    };

    // ⚡ More frequent ribbons (every 1.5–2.5s)
    const interval = setInterval(spawnRibbon, 1500 + Math.random() * 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <>
      {ribbons.map((r) => (
        <svg
          key={r.id}
          className="absolute inset-0 pointer-events-none"
          width="100%"
          height="100%"
        >
          <defs>
            <linearGradient id={`grad-${r.id}`} x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor={`hsl(${r.hue}, 90%, 65%)`} />
              <stop
                offset="100%"
                stopColor={`hsl(${(r.hue + 80) % 360}, 90%, 65%)`}
              />
            </linearGradient>
            <filter id={`blur-${r.id}`} x="-20%" y="-20%" width="140%" height="140%">
              <feGaussianBlur stdDeviation="2" />
            </filter>
          </defs>

          <path
            d={`M ${r.startX}% ${
              r.direction > 0 ? -20 : 120
            } C ${r.startX + 20}% ${r.direction > 0 ? 20 : 80}, ${
              r.endX - 20
            }% ${r.direction > 0 ? 60 : 40}, ${r.endX}% ${
              r.direction > 0 ? 120 : -20
            }`}
            stroke={`url(#grad-${r.id})`}
            strokeWidth="5"
            fill="none"
            filter={`url(#blur-${r.id})`}
            className="animate-ribbon"
            style={{
              opacity: 0.8,
              strokeLinecap: "round",
            }}
          />
        </svg>
      ))}
    </>
  );
}
