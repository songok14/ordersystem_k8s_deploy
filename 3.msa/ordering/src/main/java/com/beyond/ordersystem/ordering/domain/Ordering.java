package com.beyond.ordersystem.ordering.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ordering extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private OrderStatus orderStatus = OrderStatus.ORDERED;
    private String memberEmail;
    @OneToMany(mappedBy = "ordering", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> orderDetailList = new ArrayList<>();
}
