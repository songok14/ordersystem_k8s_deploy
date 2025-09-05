package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.service.SseAlarmService;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;

    public Long create(List<OrderCreateDto> orderCreateDtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("member is not found"));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        for (OrderCreateDto dto : orderCreateDtoList){
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product is not found"));
            if(product.getStockQuantity() < dto.getProductCount() ){
//                예외를 강제 발생시킴으로서, 모두 임시저장사항들을 rollback처리
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
//            1.동시에 접근하는 상황에서 update값의 정합성이 깨지고 갱신이상이 발생
//            2.spring버전이나 mysql버전에 따라 jpa에서 강제에러(deadlock)를 유발시켜 대부분의 요청실패 발생
            product.updateStockQuantity(dto.getProductCount());

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(dto.getProductCount())
                    .ordering(ordering)
                    .build();
//            orderDetailRepository.save(orderDetail);
            ordering.getOrderDetailList().add(orderDetail);
        }
        orderingRepository.save(ordering);

//        주문성공시 admin 유저에게 알림메시지 전송
        sseAlarmService.publishMessage("admin@naver.com", email, ordering.getId());

        return ordering.getId();
    }

    public List<OrderListResDto> findAll(){
        return orderingRepository.findAll().stream().map(o->OrderListResDto.fromEntity(o)).collect(Collectors.toList());
    }


    public List<OrderListResDto> myorders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("member is not found"));
        return  orderingRepository.findAllByMember(member).stream().map(o->OrderListResDto.fromEntity(o)).collect(Collectors.toList());
    }
    
    public Ordering cancel(Long id){
//        Ordering DB에 상태값변경 CANCELED
        Ordering ordering = orderingRepository.findById(id).orElseThrow(()->new EntityNotFoundException("ordering is not found"));
        ordering.cancelStatus();
        for (OrderDetail orderDetail : ordering.getOrderDetailList()){
//        rdb재고 업데이트
            orderDetail.getProduct().cancelOrder(orderDetail.getQuantity());

        }
        return ordering;
    }
}
