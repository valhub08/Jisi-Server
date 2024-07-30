package com.example.Jisi_Server.global.security.jwt.filter;

import com.example.Jisi_Server.domain.user.dto.UserDTO;
import com.example.Jisi_Server.global.exception.CustomConflictException;
import com.example.Jisi_Server.global.security.jwt.JwtUtil;
import com.example.Jisi_Server.global.security.properties.JwtProperties;
import com.example.Jisi_Server.global.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        try {
            UserDTO loginRequest = objectMapper.readValue(request.getReader(), UserDTO.class);

            String phoneNumber = loginRequest.getPhoneNumber();
            String password = loginRequest.getPassword();

            if (!phoneNumber.split("-")[0].equals("010")) {
                throw new CustomConflictException("your phone number must start with 010");
            }

            if (!isPhoneNumberCorrect(loginRequest)) {
                throw new CustomConflictException("your phone number is incorrect\ntip(010-xxxx-xxxx)");
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(phoneNumber, password, null);
            return authenticationManager.authenticate(authToken);
        } catch(IOException e) {
            throw new RuntimeException("Json 파싱 error");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        //유저 정보
        String phoneNumber = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성
        String access = jwtUtil.createJwt("access", phoneNumber, role, jwtProperties.getAccess().getExpiration());
        String refresh = jwtUtil.createJwt("refresh", phoneNumber, role, jwtProperties.getRefresh().getExpiration());

        //응답 설정
        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh, jwtProperties.getRefresh().getExpiration()));
        refreshTokenService.addRefreshEntity(phoneNumber, refresh, jwtProperties.getRefresh().getExpiration());

        response.setStatus(HttpStatus.OK.value());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private Cookie createCookie(String key, String value, Long maxAge) {
        Cookie cookie = new Cookie(key, value);
        int maxAgeInt = maxAge.intValue();
        cookie.setMaxAge(maxAgeInt);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

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
}
