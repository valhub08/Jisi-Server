package com.example.Jisi_Server.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private String username;

    private String password;

    public static UserEntity of(String phoneNumber, String username, String password) {
        return new UserEntity(null, phoneNumber, username, password);
    }
}