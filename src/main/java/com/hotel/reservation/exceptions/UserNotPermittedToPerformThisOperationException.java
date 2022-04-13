package com.hotel.reservation.exceptions;

public class UserNotPermittedToPerformThisOperationException extends RuntimeException {

    public UserNotPermittedToPerformThisOperationException(String message) {
        super(message);
    }
}
