package com.hotel.reservation.controllers;

import com.hotel.reservation.model.User;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.reservation.repository.RoomRepository;
import com.hotel.reservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.hotel.reservation.Role.ADMIN;
import static com.hotel.reservation.Role.USER;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    public void setUp() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        roomRepository.deleteAll();

        userRepository.save(User.builder().login("user-USER").role(USER).build());
        userRepository.save(User.builder().login("user-ADMIN").role(ADMIN).build());
    }

    @Test
    public void createUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/createUser")
                        .param("login", "user")
                        .param("role", "ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("/createUser")
                        .param("login", "user")
                        .param("role", "ADMIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addRoom() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/addRoom")
                        .param("login", "user-ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("/addRoom")
                        .param("login", "user-USER"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteRoom() throws Exception {
        final String existingRoomIdString = mockMvc.perform(MockMvcRequestBuilders.post("/addRoom")
                        .param("login", "user-ADMIN"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        mockMvc.perform(MockMvcRequestBuilders.post("/deleteRoom")
                        .param("login", "user-USER")
                        .param("roomId", existingRoomIdString))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(MockMvcRequestBuilders.post("/deleteRoom")
                        .param("login", "user-ADMIN")
                        .param("roomId", existingRoomIdString))
                .andExpect(status().isOk());
    }

    @Test
    public void reserveRoomAdmin() throws Exception {
        final String existingRoomIdString = mockMvc.perform(MockMvcRequestBuilders.post("/addRoom")
                        .param("login", "user-ADMIN"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        mockMvc.perform(MockMvcRequestBuilders.post("/reserveRoomAdmin")
                        .param("login", "user-USER")
                        .param("roomId", existingRoomIdString)
                        .param("reservationLogin", "user-USER")
                        .param("startDate", "2023-01-01 00:00:00")
                        .param("endDate", "2023-01-02 00:00:00"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(MockMvcRequestBuilders.post("/reserveRoomAdmin")
                        .param("login", "user-ADMIN")
                        .param("roomId", existingRoomIdString)
                        .param("reservationLogin", "user-USER")
                        .param("startDate", "2023-01-01 00:00:00")
                        .param("endDate", "2023-01-02 00:00:00"))
                .andExpect(status().isOk());
    }

    @Test
    public void reserveRoom() throws Exception {
        final String existingRoomIdString = mockMvc.perform(MockMvcRequestBuilders.post("/addRoom")
                        .param("login", "user-ADMIN"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        mockMvc.perform(MockMvcRequestBuilders.post("/reserveRoom")
                        .param("login", "user-USER")
                        .param("roomId", existingRoomIdString)
                        .param("startDate", "2023-01-03 00:00:00")
                        .param("endDate", "2023-01-04 00:00:00"))
                .andExpect(status().isOk());

        final int reservationId = roomRepository.findById(Integer.parseInt(existingRoomIdString)).get().getReservations().get(0).getId();

        mockMvc.perform(MockMvcRequestBuilders.post("/reserveRoom")
                        .param("login", "user-ADMIN")
                        .param("roomId", existingRoomIdString)
                        .param("startDate", "2023-01-05 00:00:00")
                        .param("endDate", "2023-01-06 00:00:00"))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("/reserveRoom")
                        .param("login", "user-ADMIN")
                        .param("roomId", existingRoomIdString)
                        .param("startDate", "2023-01-01 00:00:00")
                        .param("endDate", "2023-01-04 00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(String.format("This reservation collides with reservation with ID: %d", reservationId)));
    }

    @Test
    public void deleteReservation() throws Exception {
        final String existingRoomIdString = mockMvc.perform(MockMvcRequestBuilders.post("/addRoom")
                        .param("login", "user-ADMIN"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        mockMvc.perform(MockMvcRequestBuilders.post("/reserveRoom")
                        .param("login", "user-USER")
                        .param("roomId", existingRoomIdString)
                        .param("startDate", "2023-01-03 00:00:00")
                        .param("endDate", "2023-01-04 00:00:00"))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("/deleteReservation")
                        .param("login", "user-USER")
                        .param("reservationId", "0"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(MockMvcRequestBuilders.post("/deleteReservation")
                        .param("login", "user-ADMIN")
                        .param("reservationId", "0"))
                .andExpect(content().string("Reservation with ID: 0 not found"))
                .andExpect(status().isNotFound());

        final int reservationId = reservationRepository.findAll().iterator().next().getId();

        mockMvc.perform(MockMvcRequestBuilders.post("/deleteReservation")
                        .param("login", "user-ADMIN")
                        .param("reservationId", String.valueOf(reservationId)))
                .andExpect(status().isOk());
    }
}