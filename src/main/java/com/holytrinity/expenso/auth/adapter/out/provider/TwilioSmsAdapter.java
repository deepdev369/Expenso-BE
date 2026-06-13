package com.holytrinity.expenso.auth.adapter.out.provider;

import com.holytrinity.expenso.auth.application.port.out.SmsProviderPort;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
public class TwilioSmsAdapter implements SmsProviderPort {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully");
        } else {
            log.warn("Twilio credentials not configured. SMS will fail if attempted.");
        }
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    message
            ).create();
            log.info("SMS sent via Twilio to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}", phoneNumber, e);
            throw new RuntimeException("Failed to send SMS. Please check the phone number and try again.");
        }
    }
}
