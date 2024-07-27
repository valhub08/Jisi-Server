package com.example.Jisi_Server.domain.user.service;

import com.example.Jisi_Server.domain.user.dto.UserDTO;
import com.example.Jisi_Server.domain.user.entity.UserEntity;
import com.example.Jisi_Server.domain.user.repository.UserRepository;
import com.example.Jisi_Server.global.exception.CustomConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    public UserEntity register(UserDTO userDTO) {
        String phoneNumber = userDTO.getPhoneNumber();
        String rawPassword = userDTO.getPassword();
        String hashedPassword = bCryptPasswordEncoder.encode(rawPassword);

        Boolean isExist = userRepository.existsByPhoneNumber(phoneNumber);

        if (isExist) {
            throw new CustomConflictException("your phone number already exists");
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setPhoneNumber(phoneNumber);
        userEntity.setPassword(hashedPassword);
        userEntity.setRole("ROLE_ADMIN");

        return userRepository.save(userEntity);
    }
}
