package com.hotel.reservation.services;

import com.hotel.reservation.exceptions.*;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.model.User;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.reservation.repository.RoomRepository;
import com.hotel.reservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static com.hotel.reservation.Role.ADMIN;
import static com.hotel.reservation.Role.USER;
import static org.apache.commons.lang.time.DateUtils.addDays;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HotelServiceTest {

    private static final String USER_ADMIN = "user-ADMIN";
    private static final String USER_USER = "user-USER";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private HotelService hotelService;

    @BeforeEach
    public void setUp() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        roomRepository.deleteAll();

        userRepository.save(User.builder().login(USER_USER).role(USER).build());
        userRepository.save(User.builder().login(USER_ADMIN).role(ADMIN).build());
    }


    @Test
    public void createUserTest()  {
        assertFalse(userRepository.findByLogin("user").isPresent());
        assertDoesNotThrow(() -> hotelService.createUser("user", ADMIN));
        assertTrue(userRepository.findByLogin("user").isPresent());
        assertThrows(UserAlreadyExistException.class, () -> hotelService.createUser("user", ADMIN));
    }

    @Test
    public void addRoomTest() {
        assertFalse(roomRepository.findAll().iterator().hasNext());
        final int roomId = hotelService.addRoom(USER_ADMIN);
        assertTrue(roomRepository.findById(roomId).isPresent());
        assertThrows(UserNotPermittedToPerformThisOperationException.class, () -> hotelService.addRoom(USER_USER));
    }

    @Test
    public void deleteRoomTest() {
        final Room room = roomRepository.save(Room.builder().build());
        assertThrows(UserNotPermittedToPerformThisOperationException.class, () -> hotelService.deleteRoom(USER_USER, room.getId()));
        assertDoesNotThrow(() -> hotelService.deleteRoom(USER_ADMIN, room.getId()));
        assertThrows(RoomNotFoundException.class, () -> hotelService.deleteRoom(USER_ADMIN, 0));
    }

    @Test
    public void reserveRoomAdminTest() {
        final Room room = roomRepository.save(Room.builder().build());

        assertThrows(UserNotPermittedToPerformThisOperationException.class,
                () -> hotelService.reserveRoomAdmin(USER_USER,
                        room.getId(),
                        USER_USER,
                        addDays(new Date(), 1),
                        addDays(new Date(), 2))
        );

        assertThrows(RoomNotFoundException.class,
                () -> hotelService.reserveRoomAdmin(USER_ADMIN,
                        0,
                        USER_ADMIN,
                        addDays(new Date(), 1),
                        addDays(new Date(), 2))
        );

        assertThrows(ReservationException.class,
                () -> hotelService.reserveRoomAdmin(USER_ADMIN,
                        room.getId(),
                        USER_ADMIN,
                        addDays(new Date(), 3),
                        addDays(new Date(), 2))
        );

        assertThrows(ReservationException.class,
                () -> hotelService.reserveRoomAdmin(USER_ADMIN,
                        room.getId(),
                        USER_USER,
                        addDays(new Date(), 3),
                        new Date(0))
        );

        assertThrows(UserNotFoundException.class,
                () -> hotelService.reserveRoomAdmin(USER_ADMIN,
                        room.getId(),
                        "user-UNKNOWN",
                        addDays(new Date(), 1),
                        addDays(new Date(), 3))
        );

        assertFalse(reservationRepository.findAll().iterator().hasNext());

        assertDoesNotThrow(() -> hotelService.reserveRoomAdmin(USER_ADMIN,
                room.getId(),
                USER_USER,
                addDays(new Date(), 1),
                addDays(new Date(), 3))
        );

        assertTrue(reservationRepository.findAll().iterator().hasNext());
    }

    @Test
    public void reserveRoomTest() {
        final Room room = roomRepository.save(Room.builder().build());

        assertFalse(reservationRepository.findAll().iterator().hasNext());

        assertDoesNotThrow(() -> hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(new Date(), 1),
                addDays(new Date(), 3))
        );

        assertTrue(reservationRepository.findAll().iterator().hasNext());
    }

    @Test
    public void reserveRoomCollisionTest() {
        final Room room = roomRepository.save(Room.builder().build());
        final Room emptyRoom = roomRepository.save(Room.builder().build());

        final Date staticDateTomorrow = addDays(new Date(), 1);
        hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(staticDateTomorrow, 2),
                addDays(staticDateTomorrow, 10));

        assertDoesNotThrow(() -> hotelService.reserveRoom(USER_ADMIN,
                emptyRoom.getId(),
                addDays(staticDateTomorrow, 2),
                addDays(staticDateTomorrow, 10))
        );

        assertThrows(ReservationException.class, () -> hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(staticDateTomorrow, 1),
                addDays(staticDateTomorrow, 11))
        );

        assertThrows(ReservationException.class, () -> hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(staticDateTomorrow, 2),
                addDays(staticDateTomorrow, 10))
        );

        assertThrows(ReservationException.class, () -> hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(staticDateTomorrow, 3),
                addDays(staticDateTomorrow, 11))
        );

        assertThrows(ReservationException.class, () -> hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(staticDateTomorrow, 3),
                addDays(staticDateTomorrow, 9))
        );

        assertThrows(ReservationException.class, () -> hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(staticDateTomorrow, 1),
                addDays(staticDateTomorrow, 9))
        );

        assertDoesNotThrow(() -> hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(staticDateTomorrow, 0),
                addDays(staticDateTomorrow, 1))
        );

        assertDoesNotThrow(() -> hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(staticDateTomorrow, 11),
                addDays(staticDateTomorrow, 12))
        );
    }

    @Test
    public void deleteReservation() {
        final Room room = roomRepository.save(Room.builder().build());

        hotelService.reserveRoom(USER_ADMIN,
                room.getId(),
                addDays(new Date(), 2),
                addDays(new Date(), 10));

        final int reservationId = reservationRepository.findAll().iterator().next().getId();

        assertThrows(UserNotPermittedToPerformThisOperationException.class, () -> hotelService.deleteReservation(USER_USER, reservationId));
        assertThrows(UserNotFoundException.class, () -> hotelService.deleteReservation("user-UNKNOWN", reservationId));
        assertThrows(ReservationNotFoundException.class, () -> hotelService.deleteReservation(USER_ADMIN, 0));

        assertDoesNotThrow(() -> hotelService.deleteReservation(USER_ADMIN, reservationId));

        assertFalse(reservationRepository.findAll().iterator().hasNext());
    }
}