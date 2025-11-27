import React, { useState, useEffect } from "react";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { isAuthenticated, logout as performLogout } from "../utils/auth";
import { addAuthChangeListener } from "../utils/authState";

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated());
  const location = useLocation();
  const navigate = useNavigate();
  const isHome = location.pathname === "/";

  useEffect(() => {
    const handleAuthToggle = (state) => setIsLoggedIn(state);
    const unsubscribe = addAuthChangeListener(() =>
      handleAuthToggle(isAuthenticated())
    );

    const handleStorageChange = () => handleAuthToggle(isAuthenticated());
    window.addEventListener("storage", handleStorageChange);

    return () => {
      unsubscribe();
      window.removeEventListener("storage", handleStorageChange);
    };
  }, []);

  const navItems = [
    { name: "Home", path: "/" },
    { name: "Leaderboard", path: "/leaderboard" },
    { name: "Settings", path: "/settings" },
    { name: "Profile", path: "/profile" },
    { name: "About Us", path: "/credits" },
  ];

  const handleLoginClick = async () => {
    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    try {
      await performLogout();
      setIsLoggedIn(false);
      toast.success("You have been logged out successfully.");
      navigate("/");
    } catch (error) {
      toast.error(error?.message || "Failed to log out. Please try again.");
    }
  };

  const buttonText = isLoggedIn ? "Logout" : "Login";

  return (
    <nav
      className={`w-full z-50 ${
        isHome
          ? "bg-transparent text-white"
          : "sticky top-0 bg-[#003D7C] text-white shadow-md"
      } transition-all duration-300`}
    >
      {/* Main Navbar */}
      <div
        className={`flex items-center justify-between max-w-6xl mx-auto px-4 ${
          isHome ? "py-2 sm:py-3" : "py-3"
        }`}
      >
        {/* Brand */}
        <div
          data-tour="nav-brand"
          className="text-2xl font-bold tracking-wide cursor-pointer"
          onClick={() => navigate("/")}
        >
          <span className="text-[#EF7C00]">G</span>omoku
        </div>

        {/* Desktop Menu */}
        <div className="hidden md:flex items-center gap-8 text-lg font-semibold">
          {navItems.map(({ name, path }) => (
            <NavLink
              key={name}
              to={path}
              end
              data-tour={`nav-${name.toLowerCase()}`}
              className={({ isActive }) =>
                `group relative transition duration-300 ${
                  isActive ? "text-[#EF7C00]" : "hover:text-[#EF7C00]"
                }`
              }
            >
              {name}
              <span
                className={`absolute left-0 bottom-[-4px] h-[2px] bg-[#EF7C00] transition-transform duration-300 scale-x-0 group-hover:scale-x-100 origin-left ${
                  window.location.pathname === path ? "scale-x-100" : ""
                }`}
              ></span>
            </NavLink>
          ))}

          {/* Desktop Login/Logout */}
          <button
            data-tour="nav-login"
            onClick={handleLoginClick}
            className={`ml-6 px-5 py-1.5 rounded-md border-2 text-sm font-semibold transition-all duration-300
            ${
              isLoggedIn
                ? "border-[#EF7C00] text-[#EF7C00] hover:bg-[#EF7C00] hover:text-white"
                : "border-white text-white hover:border-[#EF7C00] hover:text-[#EF7C00]"
            }`}
          >
            {buttonText}
          </button>
        </div>

        {/* Hamburger Icon */}
        <button
          onClick={() => setMenuOpen(!menuOpen)}
          className="md:hidden focus:outline-none text-white transition-transform duration-200"
          aria-label="Toggle menu"
        >
          {menuOpen ? (
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="w-7 h-7"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          ) : (
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="w-7 h-7"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 6h16M4 12h16M4 18h16"
              />
            </svg>
          )}
        </button>
      </div>

      {/* Mobile Dropdown */}
      <div
        className={`md:hidden transition-all duration-500 ease-in-out ${
          menuOpen
            ? "max-h-[500px] opacity-100 py-4"
            : "max-h-0 opacity-0 py-0"
        } ${
          isHome
            ? "bg-[#00254C]/95 border-t border-[#EF7C00]/30 backdrop-blur-md"
            : "bg-[#003D7C]/95 border-t border-[#EF7C00]/30"
        } overflow-hidden`}
      >
        <ul className="flex flex-col items-center space-y-4 text-lg font-semibold">
          {navItems.map(({ name, path }) => (
            <li key={name}>
              <NavLink
                to={path}
                end
                onClick={() => setMenuOpen(false)}
                className={({ isActive }) =>
                  `block px-4 py-2 transition duration-200 ${
                    isActive
                      ? "text-[#EF7C00]"
                      : "text-white hover:text-[#EF7C00]"
                  }`
                }
              >
                {name}
              </NavLink>
            </li>
          ))}

          {/* Always Visible Mobile Login/Logout */}
          <li className="pt-2">
            <button
              onClick={() => {
                handleLoginClick();
                setMenuOpen(false);
              }}
              className={`px-6 py-2 rounded-md border-2 text-sm font-semibold transition-all duration-300 w-36 text-center
              ${
                isLoggedIn
                  ? "border-[#EF7C00] text-[#EF7C00] hover:bg-[#EF7C00] hover:text-white"
                  : "border-white text-white hover:border-[#EF7C00] hover:text-[#EF7C00]"
              }`}
            >
              {buttonText}
            </button>
          </li>
        </ul>
      </div>
    </nav>
  );
}
