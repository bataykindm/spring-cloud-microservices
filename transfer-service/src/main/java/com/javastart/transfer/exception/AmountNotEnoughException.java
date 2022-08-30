package com.javastart.transfer.exception;

public class AmountNotEnoughException extends RuntimeException{
    public AmountNotEnoughException(String message) {
        super(message);
    }
}
