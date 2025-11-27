import React, { useEffect, useMemo, useRef, useState } from "react";

/**
 * Lightweight walkthrough overlay with step-by-step tooltips.
 *
 * Props:
 * - steps: Array<{ selector: string, title?: string, content?: string }>
 * - storageKey?: string (localStorage key to remember completion)
 * - open?: boolean (force open)
 * - onClose?: () => void
 */
export default function Walkthrough({ steps = [], storageKey = "app-walkthrough-v1", open, onClose }) {
  const [visible, setVisible] = useState(false);
  const [index, setIndex] = useState(0);
  const [rect, setRect] = useState(null);
  const dontShowRef = useRef(true);

  const active = open ?? visible;

  const step = steps[index] || null;

  const recalc = () => {
    if (!step || !step.selector) return setRect(null);
    const el = document.querySelector(step.selector);
    if (!el) return setRect(null);
    const r = el.getBoundingClientRect();
    setRect({
      top: r.top + window.scrollY,
      left: r.left + window.scrollX,
      width: r.width,
      height: r.height,
    });
  };

  useEffect(() => {
    // auto open when first time
    if (open === undefined) {
      const done = localStorage.getItem(`${storageKey}-done`) === "1";
      if (!done) {
        setVisible(true);
      }
    } else {
      setVisible(!!open);
    }
  }, [open, storageKey]);

  useEffect(() => {
    if (!active) return;
    const onResize = () => recalc();
    const onScroll = () => recalc();
    recalc();
    window.addEventListener("resize", onResize);
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => {
      window.removeEventListener("resize", onResize);
      window.removeEventListener("scroll", onScroll);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [active, index, step?.selector]);

  const close = (remember = true) => {
    if (remember) localStorage.setItem(`${storageKey}-done`, "1");
    setVisible(false);
    onClose && onClose();
  };

  if (!active || !step) return null;

  // Tooltip placement
  const tip = (() => {
    const margin = 12;
    if (!rect) return { top: 80, left: 20 };
    let t = rect.top + rect.height + margin;
    let l = rect.left + rect.width / 2;
    return { top: t, left: l };
  })();

  return (
    <div className="fixed inset-0 z-[9999]">
      {/* dim background */}
      <div className="absolute inset-0 bg-black/50" />

      {/* highlight box */}
      {rect && (
        <div
          className="absolute rounded-lg ring-4 ring-[#EF7C00] bg-transparent pointer-events-none shadow-[0_0_30px_rgba(239,124,0,0.6)]"
          style={{ top: rect.top, left: rect.left, width: rect.width, height: rect.height }}
        />
      )}

      {/* tooltip */}
      <div
        className="absolute -translate-x-1/2 bg-white dark:bg-[#0F2538] text-gray-800 dark:text-gray-100 rounded-xl shadow-2xl border p-4 max-w-[420px]"
        style={{ top: tip.top, left: tip.left }}
      >
        <div className="text-sm font-semibold text-[#003D7C] dark:text-[#FFD166] mb-1">{step.title || "Tip"}</div>
        {step.content && <div className="text-sm mb-3">{step.content}</div>}
        <div className="flex items-center justify-between gap-2">
          <label className="flex items-center gap-2 text-xs text-gray-600 dark:text-gray-300">
            <input
              type="checkbox"
              defaultChecked
              onChange={(e) => (dontShowRef.current = e.target.checked)}
            />
            Don’t show again
          </label>
          <div className="flex gap-2">
            <button
              className="px-3 py-1 rounded bg-gray-200 dark:bg-gray-700"
              onClick={() => close(dontShowRef.current)}
            >
              Skip
            </button>
            {index > 0 && (
              <button
                className="px-3 py-1 rounded bg-gray-200 dark:bg-gray-700"
                onClick={() => setIndex((i) => Math.max(0, i - 1))}
              >
                Back
              </button>
            )}
            <button
              className="px-3 py-1 rounded bg-[#EF7C00] text-white"
              onClick={() => {
                if (index + 1 >= steps.length) {
                  close(dontShowRef.current);
                } else {
                  setIndex((i) => i + 1);
                }
              }}
            >
              {index + 1 >= steps.length ? "Done" : "Next"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

