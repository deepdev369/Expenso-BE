package com.holytrinity.expenso.auth.application.port.out;

public interface EmailProviderPort {
    void sendEmail(String to, String subject, String body);
}
