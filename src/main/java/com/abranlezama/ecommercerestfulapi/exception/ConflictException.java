package com.abranlezama.ecommercerestfulapi.exception;

public class ConflictException extends RuntimeException{
    public ConflictException(String message) {
        super(message);
    }
}
