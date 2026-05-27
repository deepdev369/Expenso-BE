package com.holytrinity.expenso.auth.application.port.out;

public interface SmsProviderPort {
    /**
     * Sends an SMS message to the given phone number.
     *
     * @param phoneNumber the destination phone number
     * @param message     the message content
     */
    void sendSms(String phoneNumber, String message);
}
