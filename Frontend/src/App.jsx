import React from "react";
import {BrowserRouter as Router, Route, Routes, useLocation} from "react-router-dom";
import {AnimatePresence, motion} from "framer-motion";
import {ToastContainer} from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import {SettingsProvider} from "./context/SettingsContext";
import {LoaderProvider} from "./context/LoaderContext";
import Navbar from "./components/Navbar";
// import GlobalWalkthrough from "./components/GlobalWalkthrough";
import InstallPrompt from "./components/InstallPrompt";
import BackgroundMusic from "./components/BackgroundMusic";
import SoundEffectsBridge from "./components/SoundEffectsBridge";

import Loader from "./pages/Loader";
import Home from "./pages/Home";
import Leaderboard from "./pages/Leaderboard";
import Settings from "./pages/Settings";
import Profile from "./pages/Profile";
import GameRoom from "./pages/GameRoom";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import ResetPassword from "./pages/ResetPassword";
import Credits from "./pages/Credits";
import NotFound from "./pages/NotFound";
import Practice from "./pages/Practice";
import PublicProfile from "./pages/PublicProfile";

export default function App() {
  return (
    <SettingsProvider>
      <LoaderProvider>
        <Router>
          <AppContent />
          <BackgroundMusic />
          <SoundEffectsBridge />
          {/* Install prompt only shown on Home via AppContent */}
          <ToastContainer
            position="top-left"
            autoClose={3000}
            hideProgressBar={false}
            newestOnTop={false}
            closeOnClick
            rtl={false}
            pauseOnFocusLoss
            draggable
            pauseOnHover
            theme="light"
          />
        </Router>
      </LoaderProvider>
    </SettingsProvider>
  );
}

/* 🌀 Handles smooth transitions between pages */
function AppContent() {
  const location = useLocation();

  return (
    <div className="min-h-screen flex flex-col items-center bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-gray-100 overflow-x-hidden">
      <AnimatePresence mode="wait">
        <Routes location={location} key={location.pathname}>
          {/* Loader Page (no transition, no navbar) */}
          <Route
            path="/loader"
            element={
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.5 }}
              >
                <Loader />
              </motion.div>
            }
          />

          {/* Home Page (has built-in navbar inside Hero) */}
          <Route
            path="/"
            element={
              <PageTransition>
                <Home />
              </PageTransition>
            }
          />

          {/* Other pages with Navbar */}
          <Route
            path="*"
            element={
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.4 }}
                className="w-full flex flex-col items-center"
              >
                {!(location.pathname.startsWith('/room') || location.pathname.startsWith('/practice')) && (
                  <Navbar />
                )}
                {/* Walkthrough disabled (Option A) */}
                {/* <GlobalWalkthrough /> */}
                {/* Show install prompt only on Home */}
                {location.pathname === '/' && <InstallPrompt />}
                <Routes>
                  <Route
                    path="/leaderboard"
                    element={
                      <PageTransition>
                        <Leaderboard />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/settings"
                    element={
                      <PageTransition>
                        <Settings />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/profile"
                    element={
                      <PageTransition>
                          <Profile/>
                      </PageTransition>
                    }
                  />
                    <Route
                        path="/profile/:userId"
                        element={
                            <PageTransition>
                        <Profile />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/players/:playerId"
                    element={
                      <PageTransition>
                        <PublicProfile />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/room"
                    element={
                      <PageTransition>
                        <GameRoom />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/room/:roomId"
                    element={
                      <PageTransition>
                        <GameRoom />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/login"
                    element={
                      <PageTransition>
                        <Login />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/signup"
                    element={
                      <PageTransition>
                        <Signup />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/reset-password"
                    element={
                      <PageTransition>
                        <ResetPassword />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/credits"
                    element={
                      <PageTransition>
                        <Credits />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="/practice"
                    element={
                      <PageTransition>
                        <Practice />
                      </PageTransition>
                    }
                  />
                  <Route
                    path="*"
                    element={
                      <PageTransition>
                        <NotFound />
                      </PageTransition>
                    }
                  />
                </Routes>
              </motion.div>
            }
          />
        </Routes>
      </AnimatePresence>
    </div>
  );
}

/* 🌈 Smooth transition wrapper for all routes */
function PageTransition({ children }) {
  return (
    <motion.div
      className="w-full"
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -15 }}
      transition={{ duration: 0.4, ease: "easeInOut" }}
    >
      {children}
    </motion.div>
  );
}
