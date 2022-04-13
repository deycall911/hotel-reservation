package com.hotel.reservation.model;

import com.hotel.reservation.Role;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes =
    @Index(name = "login_unique", columnList = "login", unique = true)
)
public class User {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(length = 50)
    private String login;

    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Reservation> reservations;
}
