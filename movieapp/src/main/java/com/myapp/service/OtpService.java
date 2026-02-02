package com.myapp.service;

import com.myapp.exception.ForgotPasswordException;
import com.myapp.util.OtpGeneratorUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class OtpService {

    private final EmailService emailService = new EmailService();

    private final Map<String, String> otpStore = new HashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new HashMap<>();

    public void sendOtp(String email) throws ForgotPasswordException {

        if (email == null || email.isBlank()) {throw new ForgotPasswordException("Email không được để trống");
        }

        String otp = OtpGeneratorUtil.generateOtp();

        otpStore.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));

        emailService.sendOtp(email, otp);
    }

    public boolean verifyOtp(String email, String inputOtp) throws ForgotPasswordException {

        if (!otpStore.containsKey(email)) {throw new ForgotPasswordException("OTP không tồn tại");
        }

        if (LocalDateTime.now().isAfter(otpExpiry.get(email))) {
            throw new ForgotPasswordException("OTP đã hết hạn");
        }

        if (!otpStore.get(email).equals(inputOtp)) {
            throw new ForgotPasswordException("OTP không đúng");
        }

        if (email == null) {
            throw new ForgotPasswordException("Phiên làm việc đã hết hạn");
        }


        return true;
    }
}