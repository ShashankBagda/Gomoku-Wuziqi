import React from "react";
import { useLocation } from "react-router-dom";
import Walkthrough from "./Walkthrough";

export default function GlobalWalkthrough() {
  const { pathname } = useLocation();

  const common = [
    {
      selector: "[data-tour='nav-brand']",
      title: "Home",
      content: "Click the brand to return to Home anytime.",
    },
    {
      selector: "[data-tour='nav-login']",
      title: "Login / Logout",
      content: "Sign in to save progress, see ranking, and settle matches.",
    },
  ];

  const perRoute = () => {
    // Skip walkthrough on auth pages
    if (pathname.startsWith('/login') || pathname.startsWith('/signup') || pathname.startsWith('/reset-password')) {
      return [];
    }
    if (pathname.startsWith("/room")) {
      return [
        ...common,
        { selector: "[data-tour='game-board']", title: "Game Board", content: "Place stones on your turn. First to 5 wins." },
        { selector: "[data-tour='ready-toggle']", title: "Ready", content: "Toggle when you’re ready to start." },
        { selector: "[data-tour='draw-undo-restart']", title: "Draw/Undo/Restart", content: "Offer draw, request undo, or restart with opponent." },
        { selector: "[data-tour='voice-chat']", title: "Voice & Chat", content: "Talk or chat with your opponent (if enabled)." },
      ];
    }
    if (pathname.startsWith("/practice")) {
      return [
        ...common,
        { selector: "[data-tour='practice-board']", title: "Practice Board", content: "Preview shows where you will place your stone." },
        { selector: "[data-tour='control-player']", title: "Choose Side", content: "Play Black (first) or White (second)." },
        { selector: "[data-tour='control-style']", title: "AI Style", content: "Offense, Balance, or Defense affects AI play." },
        { selector: "[data-tour='control-k']", title: "Candidate K", content: "Number of best moves the AI evaluates." },
        { selector: "[data-tour='control-undo-restart']", title: "Undo & Restart", content: "Undo removes your last move and AI reply. Restart resets the board." },
      ];
    }
    if (pathname.startsWith("/leaderboard")) {
      return [
        ...common,
        { selector: "body", title: "Leaderboard", content: "Check top players and your rank progress." },
      ];
    }
    if (pathname.startsWith("/settings")) {
      return [
        ...common,
        { selector: "body", title: "Settings", content: "Customize stones, board color, sounds, and theme." },
      ];
    }
    if (pathname.startsWith("/profile")) {
      return [
        ...common,
        { selector: "body", title: "Profile", content: "View your stats, rating, and recent matches." },
      ];
    }
    return common;
  };

  return <Walkthrough storageKey="walkthrough-global-v1" steps={perRoute()} />;
}
