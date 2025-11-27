import { userApi } from "./index";

export async function getPublicKey() {
  return userApi.getPublicKey();
}

export async function registerUser(params) {
  return userApi.register(params);
}

export async function loginUser({ email, username, password }) {
  const response = await userApi.login({
    username: (username || email || "").trim(),
    password,
  });
  return response;
}

export const authApi = {
  getPublicKey,
  registerUser,
  loginUser,
};

export default authApi;
