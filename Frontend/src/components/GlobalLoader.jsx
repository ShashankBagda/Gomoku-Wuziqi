import React from "react";
import { motion, AnimatePresence } from "framer-motion";

export default function GlobalLoader({ show }) {
  return (
    <AnimatePresence>
      {show && (
        <motion.div
          className="fixed inset-0 flex flex-col items-center justify-center bg-[#F1F1F1]/70 dark:bg-[#0D1B2A]/80 backdrop-blur-sm z-[9999]"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          <div className="relative w-16 h-16">
            <div className="absolute inset-0 border-4 border-[#003D7C]/40 border-t-[#EF7C00] rounded-full animate-spin" />
            <div className="absolute inset-2 border-4 border-[#EF7C00]/10 border-t-[#FFD166] rounded-full animate-[spin_3s_linear_infinite]" />
          </div>
          <p className="mt-5 text-[#003D7C] dark:text-[#FFD166] font-medium tracking-wide animate-pulse">
            Loading data…
          </p>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
