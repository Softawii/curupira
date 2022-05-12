package com.softawii.curupira.exceptions;

public class MissingPermissionsException extends Exception {

    public MissingPermissionsException() {
    }

    public MissingPermissionsException(String message) {
        super(message);
    }
}
