package com.example.Jisi_Server.redis;

import com.example.Jisi_Server.global.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String phoneNumber = "1234567890";
    private final String refresh = "dummyRefreshToken";
    private final Long expiredMs = 60000L; // 1 minute

    @BeforeEach
    public void setUp() {
        // Clean up Redis before each test
        redisTemplate.delete("refresh_token:" + phoneNumber);
        redisTemplate.delete("refresh_to_phoneNumber:" + refresh);
    }

    @Test
    public void testAddRefreshEntity() {
        // Act
        refreshTokenService.addRefreshEntity(phoneNumber, refresh, expiredMs);

        // Assert
        String storedRefreshToken = (String) redisTemplate.opsForValue().get("refresh_token:" + phoneNumber);
        String storedPhoneNumber = (String) redisTemplate.opsForValue().get("refresh_to_phoneNumber:" + refresh);

        assertNotNull(storedRefreshToken, "Refresh token should be stored in Redis");
        assertEquals(refresh, storedRefreshToken, "Stored refresh token should match");
        assertNotNull(storedPhoneNumber, "Phone number should be stored in Redis");
        assertEquals(phoneNumber, storedPhoneNumber, "Stored phone number should match");

        // Check TTL
        Long ttlForToken = redisTemplate.getExpire("refresh_token:" + phoneNumber, TimeUnit.MILLISECONDS);
        Long ttlForPhoneNumber = redisTemplate.getExpire("refresh_to_phoneNumber:" + refresh, TimeUnit.MILLISECONDS);

        assertTrue(ttlForToken > 0, "TTL for refresh token should be set");
        assertTrue(ttlForPhoneNumber > 0, "TTL for phone number should be set");

        System.out.println("TTL for token: " + ttlForToken);
        System.out.println("TTL for phone number: " + ttlForPhoneNumber);
    }

    @Test
    public void testTokenExpiration() throws InterruptedException {
        // Arrange
        refreshTokenService.addRefreshEntity(phoneNumber, refresh, expiredMs);

        // Wait for token to expire
        Thread.sleep(expiredMs + 1000); // wait a little longer than the TTL

        // Assert
        String storedRefreshToken = (String) redisTemplate.opsForValue().get("refresh_token:" + phoneNumber);
        String storedPhoneNumber = (String) redisTemplate.opsForValue().get("refresh_to_phoneNumber:" + refresh);

        assertNull(storedRefreshToken, "Refresh token should be expired and removed from Redis");
        assertNull(storedPhoneNumber, "Phone number should be expired and removed from Redis");
    }
}

