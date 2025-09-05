package com.beyond.ordersystem.member.dto;


import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.domain.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberCreateDto {
    @NotEmpty(message = "이름은 필수 입력 항목입니다.")
    private String name;
    @NotEmpty(message = "email은 필수 입력 항목입니다.")
    private String email;
    @NotEmpty(message = "password는 필수 입력 항목입니다.")
    @Size(min = 8, message = "패스워드의 길이가 너무 짧습니다.")
    private String password;
    public Member toEntity(String encodedPassword){
        return Member.builder()
                .email(this.email)
                .name(this.name)
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }
}
