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

    private Boolean isPhoneNumberCorrect(UserDTO userDTO) {
        String[] phoneNumbers = userDTO.getPhoneNumber().split("-");
        if (userDTO.getPhoneNumber().length() != 13) {
            return false;
        }
        if (phoneNumbers[1].length() != 4 || phoneNumbers[2].length() != 4) {
            return false;
        }
        if (phoneNumbers.length != 3) {
            return false;
        }
        try {
            Integer.parseInt(userDTO.getPhoneNumber().replaceAll("-", ""));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public UserEntity register(UserDTO userDTO) {
        String phoneNumber = userDTO.getPhoneNumber();
        String rawPassword = userDTO.getPassword();
        String hashedPassword = bCryptPasswordEncoder.encode(rawPassword);

        if (!phoneNumber.split("-")[0].equals("010")) {
            throw new CustomConflictException("your phone number must start with 010");
        }

        if (!isPhoneNumberCorrect(userDTO)) {
            throw new CustomConflictException("your phone number is incorrect\ntip(010-xxxx-xxxx)");
        }

        phoneNumber = phoneNumber.replaceAll("-", "");

        System.out.println(phoneNumber);

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
