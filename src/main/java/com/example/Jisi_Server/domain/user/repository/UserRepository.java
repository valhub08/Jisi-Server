package com.example.Jisi_Server.domain.user.repository;

import com.example.Jisi_Server.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByPhoneNumber(String phoneNumber);
}
