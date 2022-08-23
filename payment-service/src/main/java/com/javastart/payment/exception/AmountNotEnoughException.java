package com.javastart.payment.exception;

public class AmountNotEnoughException extends RuntimeException{
    public AmountNotEnoughException(String message) {
        super(message);
    }
}
