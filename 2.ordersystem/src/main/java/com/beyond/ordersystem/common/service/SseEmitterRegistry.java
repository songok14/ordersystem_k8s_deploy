package com.beyond.ordersystem.common.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
//    SseEmitter는 연결된 사용자정보(ip, macaddress정보 등...)를 의미
//    ConcurrentHashMap은 Thread-Safe한 map(동시성 이슈 발생X)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    public void addSseEmitter(String email, SseEmitter sseEmitter){
        System.out.println(emitterMap);
        emitterMap.put(email, sseEmitter);
    }
    public void removeEmitter(String email){
        emitterMap.remove(email);
        System.out.println(emitterMap);
    }
    public SseEmitter getEmitter(String email){
        return emitterMap.get(email);
    }
}
