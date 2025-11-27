import JSEncrypt from "jsencrypt";

export function rsaEncryptPkcs1v15(publicKeyPem, plaintext) {
  if (!publicKeyPem) throw new Error("Missing public key");
  const enc = new JSEncrypt();
  enc.setPublicKey(publicKeyPem);
  const cipherB64 = enc.encrypt(plaintext);
  if (!cipherB64) throw new Error("RSA encryption failed. Check key or plaintext length.");
  return cipherB64;
}

