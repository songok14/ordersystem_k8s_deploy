package com.beyond.ordersystem.member.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.LoginReqDto;
import com.beyond.ordersystem.member.dto.MemberCreateDto;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long save(MemberCreateDto memberCreateDto){
        if(memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(memberCreateDto.getPassword());
        Member member = memberRepository.save(memberCreateDto.toEntity(encodedPassword));
        return member.getId();
    }

    public Member login(LoginReqDto loginReqDto){
        Optional<Member> optionalMember = memberRepository.findByEmail(loginReqDto.getEmail());
        boolean check = true;
        if(!optionalMember.isPresent()){
            check = false;
        }else {
            if(!passwordEncoder.matches(loginReqDto.getPassword(), optionalMember.get().getPassword())){
                check=false;
            }
        }
        if(!check){
            throw new IllegalArgumentException("email 또는 비밀번호가 일치하지 않습니다.");
        }
        return optionalMember.get();
    }

    public List<MemberResDto> findAll(){
        return memberRepository.findAll().stream().map(m->MemberResDto.fromEntity(m)).collect(Collectors.toList());
    }
    
    public MemberResDto myinfo(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return MemberResDto.fromEntity(member);
    }


    public MemberResDto findById(Long id){
        Member member = memberRepository.findById(id).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return MemberResDto.fromEntity(member);
    }

    public void delete(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        member.deleteMember("Y");
    }
}
