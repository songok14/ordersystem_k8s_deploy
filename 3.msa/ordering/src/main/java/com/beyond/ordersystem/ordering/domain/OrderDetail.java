package com.beyond.ordersystem.ordering.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private int quantity;
    @JoinColumn(name = "ordering_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Ordering ordering;
    private Long productId;
    // 조회의 빈도에 따라 msa 도메인 설계에서 적절한 반정규화를 통한 성능 향상 가능
    private String productName;
}
