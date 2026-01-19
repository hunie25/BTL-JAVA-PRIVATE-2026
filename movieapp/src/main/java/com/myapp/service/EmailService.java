package com.myapp.service;

public class EmailService {

    public void sendOtp(String email, String otp) {
        System.out.println("OTP gửi tới " + email + ": " + otp);
    }
}
