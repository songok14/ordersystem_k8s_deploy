package com.beyond.ordersystem.member.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@ToString
@Entity
@Builder
//jpql을 제외하고 모든 조회쿼리에 where del_yn = "N" 붙이는 효과
@Where(clause = "del_yn = 'N'")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 50, unique = true, nullable = false)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    @Builder.Default
    private String delYn = "N";
    public void deleteMember(String delYn){
        this.delYn = delYn;
    }
}
