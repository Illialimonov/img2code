package com.example.ilia.aidemo.util;

public class InsufficientCreditsException extends RuntimeException {
    public InsufficientCreditsException(String message) {
        super(message);
    }
}

