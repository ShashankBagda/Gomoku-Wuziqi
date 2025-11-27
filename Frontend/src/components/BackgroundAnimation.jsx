import React from "react";
import StoneLayer from "./StoneLayer";

export default function BackgroundAnimation({
  stoneCount = 26,
  intensity = 10,
  showRadial = true,
  radialOpacity = 0.12,
}) {
  return (
    <>
      <StoneLayer count={stoneCount} intensity={intensity} />
      {showRadial && (
        <div
          className="absolute inset-0 pointer-events-none z-[1] bg-[radial-gradient(circle_at_center,rgba(239,124,0,0.4)_0%,transparent_60%)]"
          style={{ opacity: radialOpacity }}
        />
      )}
    </>
  );
}
