package com.example.Jisi_Server.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private String username;

    private String password;

    public UserEntity(String phoneNumber, String username, String password) {
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.password = password;
    }

    public UserEntity() {

    }
}