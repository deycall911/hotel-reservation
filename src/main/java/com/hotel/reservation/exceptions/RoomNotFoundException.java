package com.hotel.reservation.exceptions;

import static java.lang.String.format;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(Integer roomId) {
        super(format("Room with ID: %s not found", roomId));
    }
}
