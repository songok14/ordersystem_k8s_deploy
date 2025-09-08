package com.beyond.ordersystem.common.config;

import com.beyond.ordersystem.common.service.SseAlarmService;
import com.fasterxml.jackson.databind.JsonSerializable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    // Redis Pub/Sub을 위한 연결 객체 생성
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory sseFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);

        // redis pub/sub 기능은 db에 값을 저장하는 기능이 아니므로, 특정 DB에 의존적이지 않음

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> sseRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // redis 리스너 객체
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(redisConnectionFactory);
        // 아래 채널에서 오는 메시지를 해당 객체로 낚아채겠다?
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
        // 만약에 여러 채널을 구독해야 하는 경우, 여러 개의 PatternTopic을 add하거나, 별도의 Bean 객체 생성.

        return container;
    }

    // redis의 채널에서 수신된 메시지를 처리하는 빈객체 (위에서 수신한걸 여기서 처리한다.)
    @Bean
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService) {
        // 채널로부터 수신되는 message처리를 SseAlarmService의 onMessage메서드로 설정
        // 즉, 메시지가 수신되면 onMessage메서드가 호출된다.
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }

}
