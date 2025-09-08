package com.beyond.ordersystem.product.service;

import com.beyond.ordersystem.product.dto.ProductUpdateStockDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockKafkaListener {
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stock-update-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message) {
        try {
            productService.updateStock(objectMapper.readValue(message, ProductUpdateStockDto.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
