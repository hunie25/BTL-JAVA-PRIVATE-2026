package com.myapp.exception;

public class ForgotPasswordException extends AuthException {
    public ForgotPasswordException(String message) {
        super(message);
    }
}