package com.hotel.reservation.repository;

import com.hotel.reservation.model.Reservation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends CrudRepository<Reservation, Integer> {
    List<Reservation> findByStartDateAfterOrEndDateBefore(Date endDate, Date startDate);
}
