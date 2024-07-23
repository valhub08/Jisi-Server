package com.example.Jisi_Server.domain.user.service;

import com.example.Jisi_Server.domain.user.dto.UserDTO;
import com.example.Jisi_Server.domain.user.entity.UserEntity;
import com.example.Jisi_Server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    public void save(UserDTO user) {
        String phoneNumber = user.getPhoneNumber();
        String username = user.getUsername();
        String rawPassword = user.getPassword();

        String hashedPassword = bCryptPasswordEncoder.encode(rawPassword);

        userRepository.save(UserEntity.of(phoneNumber, username, hashedPassword));
    }

    public String register(UserDTO user) {
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            return "phoneNumberExist";
        }
        try {
            save(user);
            return "register_success";
        } catch (Exception e) {
            return "register_fail";
        }

    }
}
