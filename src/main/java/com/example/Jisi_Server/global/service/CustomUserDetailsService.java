package com.example.Jisi_Server.global.service;

import com.example.Jisi_Server.domain.user.entity.UserEntity;
import com.example.Jisi_Server.domain.user.repository.UserRepository;
import com.example.Jisi_Server.global.security.jwt.details.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {

        UserEntity userData = userRepository.findByPhoneNumber(phoneNumber);

        if (userData != null) {
            return new CustomUserDetails(userData);
        }

        throw new UsernameNotFoundException("User not found with phone number: " + phoneNumber);
    }
}
