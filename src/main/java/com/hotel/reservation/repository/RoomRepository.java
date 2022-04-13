package com.hotel.reservation.repository;

import com.hotel.reservation.model.Room;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface RoomRepository extends CrudRepository<Room, Integer> {

    List<Room> findByReservationsIsEmpty();
}
