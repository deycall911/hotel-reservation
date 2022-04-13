package com.hotel.reservation.config;

import com.hotel.reservation.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class RestResponseExceptionHandler {

    @ExceptionHandler(value = { ReservationNotFoundException.class, RoomNotFoundException.class, UserNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(RuntimeException exe) {
        return new ResponseEntity<Object>(exe.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = { UserNotPermittedToPerformThisOperationException.class})
    protected ResponseEntity<Object> handleIncorrectPermissions(RuntimeException exe) {
        return new ResponseEntity<Object>(exe.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = { ReservationException.class, UserAlreadyExistException.class})
    protected ResponseEntity<Object> handleBadInputs(RuntimeException exe) {
        return new ResponseEntity<Object>(exe.getMessage(), HttpStatus.BAD_REQUEST);
    }
}