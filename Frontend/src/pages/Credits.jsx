import React from "react";
import SectionCard from "../components/SectionCard";
import { Linkedin } from "lucide-react"; // LinkedIn icon
import BackgroundAnimation from "../components/BackgroundAnimation";

export default function Credits() {
  const developers = [
    {
      name: "Shashank Bagda",
      role: "Frontend Lead & AI Gameroom Developer",
      contribution:
        "React PWA, Tailwind UI, AI Module (Factory + Strategy Pattern), Quality & Security Testing, Game Room Communications (LiveKIT).",
      img: "/images/shashank.png",
      linkedin: "https://www.linkedin.com/in/shashank-bagda/",
    },
    {
      name: "Hao Tian",
      role: "Backend Lead & DevOps Engineer",
      contribution:
        "SpringBoot Microservices, User Module, CI/CD, Security & Performance Testing, Deployment, Game Room, Game Logic.",
      img: "/images/haotian.jpg",
      linkedin: "https://www.linkedin.com/in/tian-hao-898b35383/",
    },
    {
      name: "Li YuanXing",
      role: "Backend Developer - Matchmaking",
      contribution:
        "Redis Room Management, Real-Time Pairing, Game Room Matching, Prometheus & Grafana.",
      img: "/images/liyuanxing.png",
      linkedin: "https://www.linkedin.com/in/yuanxing-li-752811385/",
    },
    {
      name: "Cheng Muqin",
      role: "Backend Developer - Leaderboard",
      contribution:
        "Ranking Service Implementation, Database Integration.",
      img: "/images/muqin.png",
      linkedin: "https://www.linkedin.com/in/muqin-cheng-23645a382/",
    },
    {
      name: "Ding Zuhao",
      role: "QA Engineer",
      contribution:
        "JMeter Load Testing.",
      img: "/images/dingzuhao.jpg",
      linkedin: "https://www.linkedin.com/in/zuhao-ding-471572381/",
    },
  ];

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-[#F1F1F1] dark:bg-[#0D1B2A] text-[#2F2F2F] dark:text-gray-100">
      <BackgroundAnimation stoneCount={24} intensity={10} />

      <div className="relative z-10 flex flex-col items-center px-4 py-10">
      {/* 🏫 NUS ISS Logo */}
      <div className="flex justify-center items-center mb-6">
        <img
          src="/images/nus-iss-logo.png"
          alt="NUS ISS Logo"
          className="h-16 sm:h-20 object-contain"
        />
      </div>

      {/* Title */}
      <h1 className="text-4xl sm:text-5xl font-extrabold text-center mb-8 bg-gradient-to-r from-[#003D7C] to-[#EF7C00] bg-clip-text text-transparent">
        Credits & Legal
      </h1>

      {/* 🧑‍💻 Developer Section */}
      <SectionCard
        title="Development Team"
        className="w-full max-w-5xl mb-10 bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-lg p-6"
      >
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          {developers.map((dev) => (
            <div
              key={dev.name}
              className="flex flex-col items-center text-center bg-gray-50 dark:bg-gray-800 rounded-lg shadow-sm hover:shadow-md transition-transform duration-300 hover:scale-[1.02] p-4"
            >
              <img
                src={dev.img}
                alt={dev.name}
                className="w-24 h-24 rounded-full object-cover border-4 border-[#EF7C00] mb-3"
              />
              <h3 className="font-bold text-lg">{dev.name}</h3>
              <p className="text-[#EF7C00] text-sm mb-1">{dev.role}</p>

              {/* LinkedIn Link */}
              <a
                href={dev.linkedin}
                target="_blank"
                rel="noreferrer"
                className="flex items-center gap-1 text-[#003D7C] dark:text-[#FFD166] text-sm font-semibold hover:text-[#EF7C00] transition-colors duration-300 mb-2"
              >
                <Linkedin size={16} /> LinkedIn
              </a>

              <p className="text-sm text-gray-600 dark:text-gray-300">
                {dev.contribution}
              </p>
            </div>
          ))}
        </div>
      </SectionCard>

      {/* 📜 Acknowledgments */}
      <SectionCard
        title="Acknowledgments & Attributions"
        className="w-full max-w-4xl mb-10 bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-lg p-6"
      >
        <ul className="list-disc list-inside space-y-2 text-sm sm:text-base">
          <li>
            Background music and sound effects are used under fair use or open
            Creative Commons licenses. Source citations are listed below.
          </li>
          <li>
            Project inspired by traditional Gomoku (五目並べ), implemented as a
            multiplayer web platform for academic learning purposes.
          </li>
          <li>
            Built using React 18, TailwindCSS 3.4, Java Spring Boot, and Redis.
          </li>
          <li>
            Hosted on GitLab with CI/CD pipelines configured by the DevOps team.
          </li>
        </ul>
      </SectionCard>

      {/* 🎵 Media Licensing */}
      <SectionCard
        title="Music & Asset Licensing"
        className="w-full max-w-4xl mb-10 bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-lg p-6"
      >
        <p className="text-sm sm:text-base text-gray-600 dark:text-gray-300 mb-3">
          Audio, image, and design resources are credited below:
        </p>
        <ul className="list-disc list-inside text-sm sm:text-base">
          <li>
            🎵 Background tracks from{" "}
            <a
              href="https://freetouse.com/music"
              target="_blank"
              rel="noreferrer"
              className="text-[#EF7C00] hover:underline"
            >
              Freetouse Music
            </a>{" "}
            (Royalty-Free). List of Soundtracks:
            <ul className="list-disc list-inside ml-6 mt-1 space-y-1">
              <li>Snap Crackle — Aetheric</li>
              <li>Last Summer — Aylex</li>
              <li>Sweet Talks — Limujii</li>
              <li>Take Off — Luke Bergs & Waesto</li>
              <li>Office — Aylex</li>
              <li>Mud — Dagored</li>
              <li>Edge of Motion — Aetheric</li>
              <li>Ovanea — Moavii</li>
              <li>Summer Sound — Aylex</li>
              <li>Energizer — Aylex</li>
              <li>Pineapple — Walen</li>
              <li>Carnival — Aylex</li>
              <li>Mountain — Milky Wayvers</li>
              <li>Follow The Sun — Luke Bergs & Waesto</li>
              <li>Joy — Limujii</li>
            </ul>
          </li>
          <li>
            ⚙️ Sound effects from{" "}
            <a
              href="https://mixkit.co/"
              target="_blank"
              rel="noreferrer"
              className="text-[#EF7C00] hover:underline"
            >
              Mixkit
            </a>{" "}
            under CC license.
          </li>
          <li>🖼️ Icons from Lucide Icons and HeroIcons (MIT License).</li>
        </ul>
      </SectionCard>

      {/* ⚖️ Legal Info */}
      <SectionCard
        title="Terms of Use & Privacy Policy"
        className="w-full max-w-4xl mb-10 bg-white/90 dark:bg-[#0F2538] rounded-xl shadow-lg p-6"
      >
        <p className="text-sm sm:text-base text-gray-600 dark:text-gray-300 mb-4">
          By using this web application, you agree to our terms outlined below:
        </p>
        <ul className="list-disc list-inside space-y-2 text-sm sm:text-base">
          <li>
            This platform is developed for academic and non-commercial
            purposes.
          </li>
          <li>
            No personal data is collected beyond gameplay, settings, and
            leaderboard data stored locally or in our secure backend.
          </li>
          <li>
            Cookies and local storage are used only to preserve settings and
            login sessions.
          </li>
          <li>
            All intellectual property (game logic, design, and assets) belong to
            the Gomoku Project Team.
          </li>
        </ul>
      </SectionCard>

      {/* 🧾 Version Info */}
      <footer className="text-center text-xs text-gray-500 dark:text-gray-400 mt-6">
        © {new Date().getFullYear()} Gomoku Multiplayer  
        <br />
        Made with ❤️ at NUS ISS
      </footer>
      </div>
    </div>
  );
}
