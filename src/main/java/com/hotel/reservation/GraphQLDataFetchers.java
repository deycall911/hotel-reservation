package com.hotel.reservation;

import com.hotel.reservation.exceptions.RoomNotFoundException;
import com.hotel.reservation.exceptions.UserNotFoundException;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.model.User;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.reservation.repository.RoomRepository;
import com.hotel.reservation.repository.UserRepository;
import graphql.schema.DataFetcher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class GraphQLDataFetchers {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public DataFetcher getRoomByIdWithLogin() {
        return dataFetchingEnvironment -> {
            Integer roomId = dataFetchingEnvironment.getArgument("id");
            String login = dataFetchingEnvironment.getArgument("login");
            final Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
            final User user = userRepository.findByLogin(login).orElseThrow(() ->
                    new UserNotFoundException(String.format("User with login %s not found", login)));

            return room.getReservations().stream()
                    .filter(reservation -> reservation.getUser().equals(user))
                    .map(Reservation::getRoom)
                    .collect(Collectors.toList());
        };
    }

    public DataFetcher findUsers() {
        return dataFetchingEnvironment -> {
            String login = dataFetchingEnvironment.getArgument("login");
            return userRepository.findByLogin(login).orElseThrow(() ->
                    new UserNotFoundException(String.format("User with login %s not found", login)));
        };
    }

    public DataFetcher availabilityFromTo() {
        return dataFetchingEnvironment -> {

            String fromDateString = dataFetchingEnvironment.getArgument("fromDate");
            String toDateString = dataFetchingEnvironment.getArgument("toDate");

            final SimpleDateFormat simpleFormat = new SimpleDateFormat(DATE_FORMAT);
            // Find by startDate after provided toDate And endDate is before provided startDate
            final Stream<Room> roomsWithReservationThatDoNotCollide =
                    reservationRepository.findByStartDateAfterOrEndDateBefore(simpleFormat.parse(toDateString), simpleFormat.parse(fromDateString)).stream().map(Reservation::getRoom);
            final Stream<Room> fullyAvailableRooms = roomRepository.findByReservationsIsEmpty().stream();
            return Stream.concat(roomsWithReservationThatDoNotCollide, fullyAvailableRooms);



        };
    }


}
