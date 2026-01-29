package com.myapp.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import okhttp3.internal.ws.RealWebSocket;

import java.net.Authenticator;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "your_email@gmail.com";
    private static final String APP_PASSWORD = "xxxx xxxx xxxx xxxx"; // App Password

    public void sendOtp(String toEmail, String otp) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });


        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("OTP Reset Password");
            message.setText("Mã OTP của bạn là: " + otp + "\nCó hiệu lực trong 5 phút.");

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}