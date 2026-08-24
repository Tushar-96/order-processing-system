package com.ecommerce.auth.exception;

public class PasswordConfirmationMismatchException
        extends RuntimeException {

    public PasswordConfirmationMismatchException() {
        super("New password and confirmation do not match");
    }

    public PasswordConfirmationMismatchException(
            String message) {

        super(message);
    }
}
