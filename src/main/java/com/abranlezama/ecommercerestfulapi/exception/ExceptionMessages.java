package com.abranlezama.ecommercerestfulapi.exception;

public class ExceptionMessages {

    public static final String ACCOUNT_DISABLED = "Account must be activated. Check your email for account activation link";
    public static final String ACCOUNT_LOCKED = "Your account has been locked. Contact customer service for assistance";
    public static final String FAILED_AUTHENTICATION = "Authentication failed. Check your credentials and try again";
    public static final String REGISTER_PASSWORDS_MISMATCH = "Registration failed. Provided passwords must match";
    public static final String REGISTER_EMAIL_MUST_BE_UNIQUE = "Registration failed. Email is already taken";
    public static final String ACCOUNT_ACTIVATION_TOKEN_NOT_FOUND = "Activation token could not be found. Verify token or request a new one";
    public static final String ACCOUNT_IS_ACTIVE_ALREADY = "Account is activated already";
    public static final String PASSWORD_RESET_REQUEST_FOR_NON_EXISTING_USER = "Password reset request failed. Verify your email";
    public static final String ACCOUNT_MUST_BE_ENABLED_TO_RESET_PASSWORD = "Password reset request failed. Account must be activated";
    public static final String  USER_NOT_FOUND = "No user was found associated with provided email";

}
