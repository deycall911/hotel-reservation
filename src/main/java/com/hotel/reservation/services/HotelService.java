package com.hotel.reservation.services;

import com.hotel.reservation.Role;
import com.hotel.reservation.exceptions.*;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.model.User;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.reservation.repository.RoomRepository;
import com.hotel.reservation.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

import static java.lang.String.format;

@Service
@AllArgsConstructor
public class HotelService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public void createUser(final String login, final Role role) {
        final User newUser = User.builder().login(login).role(role).build();
        if (userRepository.findByLogin(login).isPresent()) {
            throw new UserAlreadyExistException(format("User with login: %s already exist", login));
        }
        userRepository.save(newUser);
    }

    public Integer addRoom(final String login) {
        isAdminRole(login);
        return roomRepository.save(Room.builder().build()).getId();
    }

    public void reserveRoomAdmin(final String login, final int roomId, final String reservationLogin, final Date startDate, final Date endDate) {
        isAdminRole(login);
        reservationForUser(startDate, endDate, reservationLogin, roomId);
    }

    public void reserveRoom(final String login, final int roomId, final Date startDate, final Date endDate) {
        reservationForUser(startDate, endDate, login, roomId);
    }

    public void deleteRoom(final String login, final int roomId) {
        isAdminRole(login);
        final Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
        roomRepository.delete(room);
    }


    public void deleteReservation(final String login, final int reservationId) {
        isAdminRole(login);
        final Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() ->
                new ReservationNotFoundException(String.format("Reservation with ID: %d not found", reservationId)));
        reservationRepository.delete(reservation);
    }

    private void isAdminRole(final String login) {
        final User user = userRepository.findByLogin(login).orElseThrow(() -> new UserNotFoundException(format("User with login %s not found", login)));
        if (user.getRole() != Role.ADMIN) {
            throw new UserNotPermittedToPerformThisOperationException(format("User %s is not allowed to perform this operation", login));
        }
    }


    private void reservationCollisionCheck(final Room room, final Date startDate, final Date endDate) {
        room.getReservations().stream().filter(reservation ->
                reservation.getStartDate().equals(startDate) ||
                reservation.getEndDate().equals(endDate) ||
                reservation.getStartDate().after(startDate) && reservation.getStartDate().before(endDate) ||
                reservation.getStartDate().before(startDate) && reservation.getEndDate().after(startDate) ||
                reservation.getStartDate().before(startDate) && reservation.getEndDate().after(endDate) ||
                reservation.getStartDate().after(startDate) && reservation.getEndDate().before(endDate))
                .findAny()
                .ifPresent(reservation -> {
                    throw new ReservationException(format("This reservation collides with reservation with ID: %s", reservation.getId()));
                });
    }

    private void reservationForUser(Date startDate, Date endDate, String reservationLogin, int roomId) {
        if (startDate.after(endDate)) {
            throw new ReservationException("Start date of reservation cannot be after end date");
        }
        if (endDate.before(new Date())) {
            throw new ReservationException("End date of reservation cannot be in the past");
        }
        final User reservationUser = userRepository.findByLogin(reservationLogin).orElseThrow(() ->
                new UserNotFoundException(format("Reservation user with login %s not found", reservationLogin)));

        final Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
        reservationCollisionCheck(room, startDate, endDate);
        final Reservation reservation = Reservation.builder()
                .room(room)
                .startDate(startDate)
                .endDate(endDate)
                .user(reservationUser)
                .build();
        reservationRepository.save(reservation);
    }
}
