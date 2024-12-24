package aistudio.data.domain.service.common;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private final long TIMEOUT_SECONDS = 30;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public String getResultByKey(String key) {
        ListOperations<String, String> listOps = stringRedisTemplate.opsForList();
        // BLPOP 명령으로 값 가져오기
        String result = listOps.leftPop(key, TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (result == null) {
            // 타임아웃 발생 시 로그 추가
            throw new RuntimeException("Timeout while waiting for result from Redis");
        }

        if (!result.isEmpty()) {
            return result; // 결과 반환
        }

        // 값이 비었을 경우 처리
        throw new RuntimeException("Empty result for key: " + key);
    }

    public void saveRefreshToken(String memberId, String refreshToken, long expirationTime) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + memberId, refreshToken, expirationTime, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String memberId) {
        return (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + memberId);
    }

    public void deleteRefreshToken(String memberId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + memberId);
    }

}
