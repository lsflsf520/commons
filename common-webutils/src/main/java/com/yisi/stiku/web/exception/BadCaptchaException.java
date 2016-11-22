package com.yisi.stiku.web.exception;

public class BadCaptchaException extends RuntimeException{

    public BadCaptchaException(String message, Throwable e) {
        super(message, e);
    }

    public BadCaptchaException(String message) {
        super(message);
    }

}
