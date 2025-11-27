# Gomoku Multiplayer – Full Stack Project

This repository is your **personal umbrella repo** for the complete Gomoku Multiplayer system.  
It contains:

- `Frontend/` – React-based PWA client for playing Gomoku in the browser.
- `Backend/` – Java/Spring Boot backend powering matchmaking, game rooms, ranking, and user management.

The original GitLab repositories from Hao Tian remain in sibling folders (`gomoku-frontend/`, `gomuku-backend/`) and are **not** part of this GitHub project. All active work for your copy should happen inside `Frontend/` and `Backend/`.

---

## Project Structure

- `Frontend/` – React + TailwindCSS SPA/PWA
  - Uses Create React App tooling.
  - Implements lobby, game room, leaderboard, profiles, and AI-enabled game room.
  - Communicates with the backend via REST/WebSocket APIs.
- `Backend/` – Spring Boot, Java 17, Maven multi-module project
  - Provides user, game, room, matchmaking, ranking and gateway services.
  - Uses MySQL, Redis, and Kafka (see backend README for exact details).

The root `.gitignore` is shared by both parts and excludes:

- Node/React build artifacts (`node_modules/`, `build/`, coverage, logs, etc.).
- Java/Maven outputs (`target/`, jars, etc.).
- Python/virtualenv caches.
- IDE and OS-specific files (`.idea/`, `.vscode/`, `.DS_Store`, etc.).

For **component-specific documentation**, see:

- `Frontend/README.md`
- `Backend/README.md`

---

## Getting Started

### 1. Prerequisites

- Node.js and npm (for `Frontend/`)
- Java 17 and Maven (for `Backend/`)
- MySQL (or another database as configured in backend)
- Redis, Kafka, and other infrastructure pieces as described in `Backend/README.md`

---

### 2. Backend – Local Setup (high level)

From `Backend/`:

1. Configure your databases and secrets via environment variables:
   - Create the required databases in MySQL (for example: `gomoku`, `user`, `ranking`, and their corresponding `test_*` databases if you run tests).
   - Set the backend connection details and secrets using environment variables (see **“Backend secrets & environment variables”** below).
2. Build and run:

   ```bash
   mvn clean compile
   mvn spring-boot:run -pl controller
   ```

   By default the backend runs on `http://localhost:8080`.

For more detailed module description, APIs, Swagger links, and test commands, see `Backend/README.md`.

---

### Backend secrets & environment variables

All sensitive credentials for the `Backend/` code are now provided via environment variables (or your own untracked Spring config), not hard-coded in the repo. Before running backend services, set at least:

- Core Gomoku services (match, room, controller, DAO, etc.)
  - `GOMOKU_DB_URL`, `GOMOKU_DB_USERNAME`, `GOMOKU_DB_PASSWORD`
  - `GOMOKU_MONGODB_URI`, `GOMOKU_MONGODB_DATABASE` (Mongo connection and database)
- User service
  - `USER_DB_URL`, `USER_DB_USERNAME`, `USER_DB_PASSWORD`
- Ranking service
  - `RANKING_DB_URL`, `RANKING_DB_USERNAME`, `RANKING_DB_PASSWORD`
- Shared infrastructure
  - `REDIS_PASSWORD` (if your Redis instance is password protected)
  - `SENDGRID_API_KEY` (required if you enable email verification flows)
  - `OPENAI_API_KEY` (required to use the GPT-based AI move suggester; optional otherwise)

These variables are only consumed by the copies under `Backend/` (and the React client under `Frontend/` where relevant). The original reference projects from Hao Tian now live outside this repo (for example under `../Gomoku_Gitlab/gomoku-frontend/` and `../Gomoku_Gitlab/gomuku-backend/`) and are **not** modified by this GitHub repo’s configuration.

You can provide the values by exporting environment variables before running the backend, or by wiring them into your own non-committed Spring configuration files that map to the same property names.

### 3. Frontend – Local Setup

From `Frontend/`:

1. Install dependencies:

   ```bash
   npm install
   ```

2. Run the development server:

   ```bash
   npm start
   ```

   The app will be available at `http://localhost:3000` (Create React App default).

3. Run tests / security checks (optional):

   ```bash
   npm test                       # Jest tests
   npm run test:coverage          # Coverage
   npm run sca                    # Software composition analysis
   npm run sast                   # Static application security testing
   npm run dast                   # OWASP ZAP baseline scan (see Frontend/README.md)
   ```

---

## Typical Development Workflow

1. **Start backend** (from `Backend/`):

   ```bash
   mvn spring-boot:run -pl controller
   ```

2. **Start frontend** (from `Frontend/`):

   ```bash
   npm start
   ```

3. Develop features in:
   - `Frontend/src/…` for UI/UX, game client, and PWA behaviour.
   - `Backend/…` modules for APIs, Game logic, and data persistence.

4. Commit changes from the root `Gomoku/` repo so that both `Frontend/` and `Backend/` changes are captured together.

---

## Relationship to Original GitLab Projects

- External clones (for example `../Gomoku_Gitlab/gomoku-frontend/` and `../Gomoku_Gitlab/gomuku-backend/`) are the **original repos** from Hao Tian’s GitLab.
- `Frontend/` and `Backend/` in this repo are **copies of the code only** (no `.git` directories) so you can:
  - Maintain your own Git history on GitHub.
  - Keep the original GitLab repositories untouched for reference.

If you fetch new changes from GitLab, you can manually sync selected files into `Frontend/` or `Backend/` as needed.

---

## Contributing / Branching Strategy (suggested)

For your personal GitHub repo, a simple approach is:

- `main` – stable branch with working frontend + backend.
- `feature/*` – feature branches for new UI, API endpoints, or refactors.
- `test` / `development` – integration branches that mirror your CI/CD setup (if you configure pipelines on GitHub).

Adjust as you like to match your existing GitLab workflow.
