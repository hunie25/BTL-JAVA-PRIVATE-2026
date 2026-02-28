package com.myapp.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "tranhuyen08052006@gmail.com";
    private static final String APP_PASSWORD = "zfktvsizdzfutbid"; // App Password

    public void sendOtp(String toEmail, String otp) {
        sendEmail(toEmail, "OTP Xác Minh Đăng Ký", buildOtpContent(otp));
    }

    public void sendWelcome(String toEmail, String username) {
        String content = "Chào mừng " + username + "!\n\n" +
                "Tài khoản của bạn đã được tạo thành công.\n\n" +
                "Bạn có thể đăng nhập ngay bây giờ.";
        sendEmail(toEmail, "Đăng Ký Thành Công", content);
    }

    private void sendEmail(String toEmail, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(
                props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                    }
                }
        );

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể gửi email");
        }
    }

    public void sendOtpForReset(String toEmail, String otp) {
        String subject = "Mã OTP đặt lại mật khẩu";
        String content = "Mã xác nhận để đặt lại mật khẩu của bạn là: " + otp +
                "\nHiệu lực trong 5 phút. Nếu không phải bạn yêu cầu, vui lòng bỏ qua email này.";
        sendEmail(toEmail, subject, content);
    }
    private String buildOtpContent(String otp) {
        return "Mã OTP của bạn là: " + otp +
                "\n\nCó hiệu lực trong 5 phút.\n\n" +
                "Vui lòng không chia sẻ mã này cho bất kỳ ai.";
    }
}
