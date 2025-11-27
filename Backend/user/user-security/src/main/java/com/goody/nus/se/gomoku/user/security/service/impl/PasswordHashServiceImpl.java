package com.goody.nus.se.gomoku.user.security.service.impl;

import com.goody.nus.se.gomoku.user.security.service.IPasswordHashService;
import com.goody.nus.se.gomoku.user.security.util.RsaCryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Password hashing service implementation using BCrypt with UUID-based salt
 *
 * @author HaoTian
 * @version 1.0, 2025/10/4
 */
@Service
@RequiredArgsConstructor
public class PasswordHashServiceImpl implements IPasswordHashService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Override
    public String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String hashPassword(String plainPassword, String salt) {
        // Combine plain password with UUID salt before BCrypt hashing
        String saltedPassword = plainPassword + salt;
        // BCrypt will hash the salted password and include its own internal salt
        return passwordEncoder.encode(saltedPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword, String salt) {
        // Combine plain password with the same UUID salt
        String saltedPassword = plainPassword + salt;
        // Verify using BCrypt's built-in comparison
        return passwordEncoder.matches(saltedPassword, hashedPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String decryptPassword(String encryptedPassword) throws Exception {
        return RsaCryptoUtil.decryptWith(encryptedPassword);
    }
}
