package com.myapp.service;

import com.myapp.dao.UserDAO;
import com.myapp.exception.ForgotPasswordException;
import com.myapp.util.OtpGeneratorUtil;
import com.myapp.util.SessionManager;

public class OtpService {

    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();

    public void sendOtp(String email) throws ForgotPasswordException {
        if (email == null || email.isBlank()) {
            throw new ForgotPasswordException("Vui lòng nhập email");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ForgotPasswordException("Email không hợp lệ");
        }

        if (!userDAO.existsByEmail(email)) {
            throw new ForgotPasswordException("Email không tồn tại");
        }

        String otp = OtpGeneratorUtil.generateOtp();

        emailService.sendOtp(email, otp);

        SessionManager.set("RESET_EMAIL", email);
        SessionManager.set("RESET_OTP", otp);
    }
}
