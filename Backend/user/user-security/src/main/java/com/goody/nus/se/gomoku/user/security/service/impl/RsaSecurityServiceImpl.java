package com.goody.nus.se.gomoku.user.security.service.impl;

import com.goody.nus.se.gomoku.user.security.service.IRsaSecurityService;
import com.goody.nus.se.gomoku.user.security.util.RsaCryptoUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@IRsaSecurityService} impl
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
@Service
@RequiredArgsConstructor
public class RsaSecurityServiceImpl implements IRsaSecurityService {

    @PostConstruct
    public void init() throws Exception {
        // Ensure key is ready at startup
        RsaCryptoUtil.getPublicKeyPem();
    }

    @Override
    public String publicKey() throws Exception {
        return RsaCryptoUtil.getPublicKeyPem();
    }
}
