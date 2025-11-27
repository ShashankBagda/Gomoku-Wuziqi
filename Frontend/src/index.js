import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import App from "./App";
import * as serviceWorkerRegistration from "./serviceWorkerRegistration";

// Build signature for cache-busting on CI/CD deployments
const BUILD_SIG = process.env.REACT_APP_BUILD_SIG || process.env.REACT_APP_VERSION || "dev";

try {
  const lastSig = localStorage.getItem("app-build-sig");
  if (lastSig && lastSig !== BUILD_SIG) {
    // Clear local/session storage and runtime caches on new deploy
    localStorage.clear();
    sessionStorage.clear();
    if ("caches" in window) {
      caches.keys().then((keys) => keys.forEach((k) => caches.delete(k))).catch(() => {});
    }
    localStorage.setItem("app-build-sig", BUILD_SIG);
  } else if (!lastSig) {
    localStorage.setItem("app-build-sig", BUILD_SIG);
  }
} catch (e) {}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);

// ✅ Enable PWA installability
serviceWorkerRegistration.register();
