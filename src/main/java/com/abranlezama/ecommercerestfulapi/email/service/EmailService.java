package com.abranlezama.ecommercerestfulapi.email.service;

public interface EmailService<T> {

    void sendEmail(T data);
}
