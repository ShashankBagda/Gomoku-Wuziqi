package com.goody.nus.se.gomoku.user.security.service.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.user.security.service.IEmailService;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.goody.nus.se.gomoku.redis.service.RedisService;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.UNKNOWN_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final RedisService redisService;

    @Value("${sendgrid.verify-switch-off:true}")
    private boolean verifySwitchOff;

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Override
    public void sendVerificationCode(String email) {
        if (verifySwitchOff) {
            return;
        }
        String code = generateCode();

        // store code in Redis with expiration
        String redisKey = "email:verify:" + email;
        redisService.set(redisKey, code, 5, TimeUnit.MINUTES);

        // send email
        sendEmail(email, code);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        if (verifySwitchOff) {
            return true;
        }
        String redisKey = "email:verify:" + email;
        Object storedCode = redisService.get(redisKey);
        if(storedCode == null) {
            return false;
        }
        redisService.delete(redisKey);
        return storedCode.toString().equals(code);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }

    private void sendEmail(String toEmail, String code) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        String subject = "Your GoMoku Verification Code";
        String contentText = "Your verification code is: " + code + "\nThis code will expire in 5 minutes.";
        Content content = new Content("text/plain", contentText);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            log.info("SendGrid email sent to {} with status {}", toEmail, response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to send email to {}", toEmail, e);
            throw new BizException(UNKNOWN_ERROR, "Failed to send verification email");
        }
    }
}
