package com.neelesh.paymentgateway.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neelesh.paymentgateway.dto.PaymentResponse;
import com.neelesh.paymentgateway.enums.PaymentStatus;
import com.neelesh.paymentgateway.model.IdempotencyKey;
import com.neelesh.paymentgateway.repository.IdempotencyRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class IdempotencyService {
    private final IdempotencyRepository idempotencyRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;


    Optional<PaymentResponse> getExistingResponse(String key){
        Object cachedResponse = redisTemplate.opsForValue().get(key);
        if(cachedResponse != null) {
            return Optional.of((PaymentResponse) cachedResponse);
        }
        Optional<IdempotencyKey> idempotencyKey = idempotencyRepository.findById(key);
        if(idempotencyKey.isPresent()){
            String jsonResponse = idempotencyKey.get().getResponse();
            try {
                PaymentResponse response = objectMapper.readValue(jsonResponse, PaymentResponse.class);
                redisTemplate.opsForValue().set(key, response, 24, TimeUnit.HOURS);
                return Optional.of(response);
            } catch (JsonProcessingException e){
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    boolean tryAcquireLock(String key){
        String lockKey = "lock:" +key;

        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 30, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    void storeResponse(String key, PaymentResponse paymentResponse, PaymentStatus paymentStatus){
        redisTemplate.opsForValue().set(key, paymentResponse, 24, TimeUnit.HOURS);
        try {
            String jsonResponse = objectMapper.writeValueAsString(paymentResponse);
            IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                    .status(paymentStatus)
                    .response(jsonResponse)
                    .key(key).build();
            idempotencyRepository.save(idempotencyKey);
        }catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payment response", e);
        }
    }

    void releaseLock(String key){
        String lockKey = "lock:" + key;
        redisTemplate.delete(lockKey);
    }
}
