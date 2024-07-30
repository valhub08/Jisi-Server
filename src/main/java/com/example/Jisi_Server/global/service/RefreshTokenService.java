package com.example.Jisi_Server.global.service;

import com.example.Jisi_Server.global.security.jwt.JwtUtil;
import com.example.Jisi_Server.global.security.properties.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TOKEN_PREFIX = "refresh_token:";
    private static final String INVERSE_INDEX_PREFIX = "refresh_to_phoneNumber:";
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    /**
     * reissue 다시 access token 발급 및 refresh 토큰을 발급해주는 서비스
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return new ResponseEntity<?>
     */
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return new ResponseEntity<>("can't find all of the cookies", HttpStatus.UNAUTHORIZED);
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
                break;
            }
        }

        if (refresh == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        try {
            jwtUtil.getPhoneNumber(refresh);
        } catch (Exception e) {
            return new ResponseEntity<>("unknown token", HttpStatus.UNAUTHORIZED);
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        if (!existsByRefreshToken(refresh)) {
            return new ResponseEntity<>("refresh token not found", HttpStatus.BAD_REQUEST);
        }

        String phoneNumber = jwtUtil.getPhoneNumber(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccess = jwtUtil.createJwt("access", phoneNumber, role, jwtProperties.getAccess().getExpiration());
        String newRefresh = jwtUtil.createJwt("refresh", phoneNumber, role, jwtProperties.getRefresh().getExpiration());

        deleteByRefreshToken(refresh);
        addRefreshEntity(phoneNumber, newRefresh, jwtProperties.getRefresh().getExpiration());

        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh,jwtProperties.getRefresh().getExpiration()));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * logout 을 구현하기 위한 서비스<br>
     * 토큰의 유효성, 조작 등 검사 후 redis 에서 삭제
     * @param request
     * @param response
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    public void logout(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
                break;
            }
        }

        if (refresh == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Boolean isExist = existsByRefreshToken(refresh);
        if (!isExist) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        deleteByRefreshToken(refresh);
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    public void deleteByRefreshToken(String refresh) {
        String phoneNumber = findByRefreshToken(refresh.replaceFirst(INVERSE_INDEX_PREFIX, ""));
        redisTemplate.delete(TOKEN_PREFIX + phoneNumber);
        redisTemplate.delete(INVERSE_INDEX_PREFIX + refresh.replaceFirst(INVERSE_INDEX_PREFIX, ""));
    }

    public void addRefreshEntity(String phoneNumber, String refresh, Long expiredMs) {
        String key = TOKEN_PREFIX + phoneNumber;
        String refreshKey = INVERSE_INDEX_PREFIX + refresh;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            deleteByRefreshToken(INVERSE_INDEX_PREFIX + findByPhoneNumber(phoneNumber));
        }

        redisTemplate.opsForValue().set(key, refresh, expiredMs, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(refreshKey, phoneNumber, expiredMs, TimeUnit.MILLISECONDS);
    }

    public String findByPhoneNumber(String phoneNumber) {
        String key = TOKEN_PREFIX + phoneNumber;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public String findByRefreshToken(String refresh) {
        String key = INVERSE_INDEX_PREFIX + refresh;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public Boolean existsByRefreshToken(String refresh) {
        String key = INVERSE_INDEX_PREFIX + refresh;
        return redisTemplate.hasKey(key);
    }

    public Cookie createCookie(String key, String value, Long maxAge) {
        Cookie cookie = new Cookie(key, value);
        int maxAgeInt = maxAge.intValue();
        cookie.setMaxAge(maxAgeInt);
//        cookie.setSecure(true);
//        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}

