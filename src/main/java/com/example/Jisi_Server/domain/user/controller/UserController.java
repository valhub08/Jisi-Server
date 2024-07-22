package com.example.Jisi_Server.domain.user.controller;

import com.example.Jisi_Server.domain.user.dto.UserDTO;
import com.example.Jisi_Server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody UserDTO user) {
        return userService.register(user);
    }
}
