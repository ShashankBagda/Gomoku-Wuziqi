/**
 * API Unified Export Module
 *
 * This is the unified entry point for all frontend APIs
 * All API calls should be imported from here
 *
 * Usage examples:
 *
 * // Method 1: Named import (recommended)
 * import { userApi } from '@/api';
 * await userApi.login({ username: 'user@example.com', password: '123456' });
 *
 * // Method 2: Destructure specific methods
 * import { userApi } from '@/api';
 * const { login, register } = userApi;
 *
 * // Method 3: Import all APIs
 * import * as api from '@/api';
 * await api.userApi.login(...);
 */

import userApiModule from "./modules/user";
import rankingApiModule from "./modules/ranking";
import lobbyApiModule from "./lobby";
import gameApiModule from "./game";
import matchApiModule from "./match";
import playerApiModule from "./player";
import communicationApiModule from "./communication";

// ==================== Export HTTP Client ====================
export { api } from "./client";

// ==================== Export Utility Functions ====================
export { rsaEncryptPkcs1v15 } from "./utils/rsa";

// ==================== Export Business Module APIs ====================

/**
 * User Module API
 * Contains user registration, login, verification, password reset, etc.
 */
export { userApiModule as userApi };

/**
 * Lobby Module API
 * Handles private room lifecycle (create/join/leave)
 */
export { lobbyApiModule as lobbyApi };

/**
 * Game Module API
 * Handles gameplay actions and polling state
 */
export { gameApiModule as gameApi };

/**
 * Match Module API
 * Handles player matching for casual and ranked games
 */
export { matchApiModule as matchApi };

/**
 * Player Module API
 * Handles player status checking (queue and room state)
 */
export { playerApiModule as playerApi };

/**
 * Communication Module API
 * Exposes realtime communication helpers (online count, etc.)
 */
export { communicationApiModule as communicationApi };

/**
 * Ranking Module API
 * Handles player ranking, leaderboards, and match settlement
 */
export { rankingApiModule as rankingApi };

// ==================== Backward Compatibility: Export authApi ====================
/**
 * @deprecated Use userApi instead
 * Kept for backward compatibility with legacy code
 */
export const authApi = {
  getPublicKey: userApiModule.getPublicKey,
  registerUser: userApiModule.register,
  loginUser: userApiModule.login,
};

// ==================== Future Modules (To be implemented) ====================
// export { default as gameApi } from "./modules/game";
// export { default as rankingApi } from "./modules/ranking";
