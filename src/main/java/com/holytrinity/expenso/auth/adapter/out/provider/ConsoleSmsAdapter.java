package com.holytrinity.expenso.auth.adapter.out.provider;

import com.holytrinity.expenso.auth.application.port.out.SmsProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsoleSmsAdapter implements SmsProviderPort {

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("========== SMS SIMULATION ==========");
        log.info("To: {}", phoneNumber);
        log.info("Message: {}", message);
        log.info("====================================");
    }
}
