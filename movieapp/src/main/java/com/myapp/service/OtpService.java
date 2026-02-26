package com.myapp.service;

import com.myapp.exception.ForgotPasswordException;
import com.myapp.exception.RegisterException;
import com.myapp.util.OtpGeneratorUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class OtpService {

    private final EmailService emailService = new EmailService();

    private final static Map<String, String> otpStore = new HashMap<>();
    private final static Map<String, LocalDateTime> otpExpiry = new HashMap<>();
    private final static Map<String, String> otpType = new HashMap<>(); // "REGISTER" hoặc "RESET_PASSWORD"

    public void sendOtpForRegistration(String email) throws RegisterException {
        if (email == null || email.isBlank()) {
            throw new RegisterException("Email không được để trống");
        }

        String otp = OtpGeneratorUtil.generateOtp();
        otpStore.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));
        otpType.put(email, "REGISTER");

        try {
            emailService.sendOtp(email, otp);
        } catch (Exception e) {
            throw new RegisterException("Không thể gửi OTP về email. Vui lòng thử lại.");
        }
    }

    public void sendOtp(String email) throws ForgotPasswordException {
        if (email == null || email.isBlank()) {
            throw new ForgotPasswordException("Email không được để trống");
        }

        String otp = OtpGeneratorUtil.generateOtp();
        otpStore.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));
        otpType.put(email, "RESET_PASSWORD");

        try {
            emailService.sendOtp(email, otp);
        } catch (Exception e) {
            throw new ForgotPasswordException("Không thể gửi OTP về email. Vui lòng thử lại.");
        }
    }

    public boolean verifyOtp(String email, String inputOtp) throws ForgotPasswordException {
        if (!otpStore.containsKey(email)) {
            throw new ForgotPasswordException("OTP không tồn tại");
        }

        if (LocalDateTime.now().isAfter(otpExpiry.get(email))) {
            otpStore.remove(email);
            otpExpiry.remove(email);
            throw new ForgotPasswordException("OTP đã hết hạn");
        }

        if (!otpStore.get(email).equals(inputOtp)) {
            throw new ForgotPasswordException("OTP không đúng");
        }

        if (email == null) {
            throw new ForgotPasswordException("Phiên làm việc đã hết hạn");
        }
        otpStore.remove(email);
        otpExpiry.remove(email);
        otpType.remove(email);

        return true;
    }

    public boolean verifyOtpForRegistration(String email, String inputOtp) throws RegisterException {
        if (!otpStore.containsKey(email)) {
            throw new RegisterException("OTP không tồn tại");
        }

        if (LocalDateTime.now().isAfter(otpExpiry.get(email))) {
            otpStore.remove(email);
            otpExpiry.remove(email);
            throw new RegisterException("OTP đã hết hạn");
        }

        if (!otpStore.get(email).equals(inputOtp)) {
            throw new RegisterException("OTP không đúng");
        }

        otpStore.remove(email);
        otpExpiry.remove(email);
        otpType.remove(email);

        return true;
    }
}