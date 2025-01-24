package com.akcode.atm.exceptions;

public class AccountDoesNotExistsException extends RuntimeException {

    public AccountDoesNotExistsException(String message) {
        super(message);
    }
}
