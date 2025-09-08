package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.dto.CommonSuccessDto;
import com.beyond.ordersystem.common.service.SseAlarmService;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderDetaiReslDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.ProductDto;
import com.beyond.ordersystem.ordering.feignclient.ProductFeignClient;
import com.beyond.ordersystem.ordering.repository.OrderingDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderingDetailRepository orderingDetailRepository;
    private final SseAlarmService sseAlarmService;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Long createOrdering(List<OrderCreateDto> dtos, String email) {
        Ordering ordering = Ordering.builder()
                .memberEmail(email).build();
        orderingRepository.save(ordering);

        for (OrderCreateDto dto : dtos) {
            // 상품 조회
            String productDetailUrl = "http://product-service/product/detail/" + dto.getProductId().toString();
            HttpHeaders headers = new HttpHeaders();
            // HttpEntity: HttpBody + HttpHeader를 세팅하기 위한 객체
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<CommonSuccessDto> responseEntity = restTemplate.
                    exchange(productDetailUrl, HttpMethod.GET, httpEntity, CommonSuccessDto.class);
            CommonSuccessDto commonSuccessDto = responseEntity.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            // readValue:String -> 클래스 변환, convertValue : Object 클래스 -> 클래스 변환
            ProductDto product = objectMapper.convertValue(commonSuccessDto.getResult(), ProductDto.class);
            if (product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            // 주문 발생
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();

            ordering.getOrderDetailList().add(orderDetail);

            // 동기적 재고감소 요청
            String productIpdateStockUrl = "http://product-service/product/updatestock";
            HttpHeaders stockHeaders = new HttpHeaders();
            stockHeaders.setContentType(MediaType.APPLICATION_JSON);
            // HttpEntity: HttpBody + HttpHeader를 세팅하기 위한 객체
            HttpEntity<OrderCreateDto> updateStockEntity = new HttpEntity<>(dto, stockHeaders);
            restTemplate.exchange(productIpdateStockUrl, HttpMethod.PUT, updateStockEntity, Void.class);
        }

        // 주문성공 시 admin 유저에게 알림메시지 전송
        sseAlarmService.publishMessage("admin@naver.com", email, ordering.getId());

        return ordering.getId();
    }

    // fallback 메서드는 원본 메서드의 매개변수와 정확히 일치해야 함.
    // t에 에러 메세지 주입
    public void fallbackProductServiceCircuit(List<OrderCreateDto> dtos, String email, Throwable t){
        throw new RuntimeException("서버 응답 지연,\n 나중에 다시 시도해 주세요.");
    }

    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "fallbackProductServiceCircuit")
    public Long createFeignKafka(List<OrderCreateDto> dtos, String email) {
        Ordering ordering = Ordering.builder()
                .memberEmail(email).build();
        orderingRepository.save(ordering);

        for (OrderCreateDto dto : dtos) {
            // feign 클라이언트를 사용한 동기적 상품조회
            CommonSuccessDto commonSuccessDto = productFeignClient.getProductById(dto.getProductId());
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto product = objectMapper.convertValue(commonSuccessDto.getResult(), ProductDto.class);

            // 상품 조회
            // HttpEntity: HttpBody + HttpHeader를 세팅하기 위한 객체
            if (product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }

            // 주문 발생
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetailList().add(orderDetail);

//            // feign을 통한 동기적 재고감소 요청
//            productFeignClient.updateProductStockQuantity(dto);

            // kafka를 활용한 비동기적 재고감소 요청
            // kafkaTemplate.send(토픽명, 메시지)
            kafkaTemplate.send("stock-update-topic", dto);
        }

        // 주문성공 시 admin 유저에게 알림메시지 전송
        sseAlarmService.publishMessage("admin@naver.com", email, ordering.getId());

        return ordering.getId();
    }

    // 주문 목록 조회
    public List<OrderListResDto> getOrderingList() {
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        List<Ordering> orderingList = orderingRepository.findAll();

        // Ordering (id, orderStatus), Member(memberEmail)
        // OrderDetail (detailId, productName, productCount)
        for (Ordering ordering : orderingList) {
            List<OrderDetaiReslDto> orderDetailDtoList = new ArrayList<>();

            List<OrderDetail> orderDetailList = orderingDetailRepository.findByOrdering(ordering);
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetailDtoList.add(OrderDetaiReslDto.fromEntity(orderDetail));
            }
            OrderListResDto orderListResDto = OrderListResDto.fromEntity(ordering, orderDetailDtoList);
            orderListResDtoList.add(orderListResDto);
        }

        return orderListResDtoList;
    }

    // 나의 주문 목록 조회
    public List<Ordering> getMyOrderingList(String email) {

        return orderingRepository.findByMemberEmail(email);
    }
}
