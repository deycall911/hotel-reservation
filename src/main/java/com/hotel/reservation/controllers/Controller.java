package com.hotel.reservation.controllers;

import com.hotel.reservation.Role;
import com.hotel.reservation.services.HotelService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.hotel.reservation.GraphQLDataFetchers.DATE_FORMAT;

@RestController
@AllArgsConstructor
public class Controller {

    private final HotelService hotelService;

    @PostMapping("/createUser")
    public void createUser(@RequestParam final String login, @RequestParam final Role role) {
        hotelService.createUser(login, role);
    }

    @PostMapping("/addRoom")
    public Integer addRoom(@RequestParam final String login) {
        return hotelService.addRoom(login);
    }

    @PostMapping("/deleteRoom")
    public void deleteRoom(@RequestParam final String login, @RequestParam final int roomId) {
        hotelService.deleteRoom(login, roomId);
    }

    @PostMapping("/deleteReservation")
    public void deleteReservation(@RequestParam final String login, @RequestParam final int reservationId) {
        hotelService.deleteReservation(login, reservationId);
    }

    @PostMapping("/reserveRoom")
    public void reserveRoom(@RequestParam final String login, @RequestParam final int roomId,
                            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) Date startDate,
                            @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) Date endDate) {
        hotelService.reserveRoom(login, roomId, startDate, endDate);
    }


    @PostMapping("/reserveRoomAdmin")
    public void reserveRoomAdmin(@RequestParam final String login, @RequestParam final int roomId, @RequestParam final String reservationLogin,
                                 @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) Date startDate,
                                 @RequestParam @DateTimeFormat(pattern = DATE_FORMAT) Date endDate) {
        hotelService.reserveRoomAdmin(login, roomId, reservationLogin, startDate, endDate);
    }
}
