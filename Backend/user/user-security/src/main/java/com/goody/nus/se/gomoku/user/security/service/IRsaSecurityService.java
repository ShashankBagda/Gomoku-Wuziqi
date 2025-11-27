package com.goody.nus.se.gomoku.user.security.service;

/**
 * rsa security service
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
public interface IRsaSecurityService {

    /**
     * get user public key for client to encrypt password
     *
     * @return public key
     */
    String publicKey() throws Exception;
}
