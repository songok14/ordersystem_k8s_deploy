package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResDto {
    private Long orderingId;
    private OrderStatus orderStatus;
    private String memberEmail;
    @Builder.Default
    private List<OrderDetaiReslDto> orderDetails = new ArrayList<>();

    public static OrderListResDto fromEntity(Ordering ordering, List<OrderDetaiReslDto> orderDetailDtoList) {
        return OrderListResDto.builder()
                .orderingId(ordering.getId())
                .orderStatus(ordering.getOrderStatus())
                .memberEmail(ordering.getMemberEmail())
                .orderDetails(orderDetailDtoList)
                .build();
    }
}
