package com.jpa.spring.jpaspring.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jpa.spring.jpaspring.entity.Member;
import com.jpa.spring.jpaspring.repository.MemberRepository;
import com.jpa.spring.jpaspring.vo.MemberVo.MemberGetVo;
import com.jpa.spring.jpaspring.vo.MemberVo.MemberSaveVo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public List<MemberGetVo> getListMember() {
        return memberRepository.findAll().stream()
                .map(member -> MemberGetVo.builder()
                        .memberId(member.getMemberId())
                        .memberName(member.getMemberName())
                        .memberEmail(member.getMemberEmail())
                        .adminYn(member.getAdminYn())
                        .build())
                .collect(Collectors.toList());
    }

    public MemberGetVo getMember(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        return MemberGetVo.builder()
                .memberId(member.getMemberId())
                .memberName(member.getMemberName())
                .memberEmail(member.getMemberEmail())
                .adminYn(member.getAdminYn())
                .build();
    }

    public Member insertMember(MemberSaveVo memberVo) {
        // 입력값 검증
        if (memberVo.getMemberId() == null || memberVo.getMemberId().isEmpty()) {
            throw new IllegalArgumentException("Member ID is required");
        }

        // 현재 사용자 ID 가져오기 (Spring Security 사용 시)
        // String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Member 엔티티 생성
        Member member = Member.builder()
                .memberId(memberVo.getMemberId())
                .memberName(memberVo.getMemberName())
                .memberPassword(memberVo.getMemberPassword())
                .memberEmail(memberVo.getMemberEmail())
                .adminYn(memberVo.getAdminYn() != null ? memberVo.getAdminYn() : "N")
                .registerDateTime(LocalDateTime.now())
                .registerId("psy") 
                .build();

        // Member 저장
        return memberRepository.save(member);
    }

    @Transactional
    public void updateMember(String memberId, MemberSaveVo memberVo) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));
    
        // 필요한 필드 업데이트
        member.setMemberName(memberVo.getMemberName() != null ? memberVo.getMemberName() : member.getMemberName());
        member.setMemberPassword(memberVo.getMemberPassword() != null ? memberVo.getMemberPassword() : member.getMemberPassword());
        member.setMemberEmail(memberVo.getMemberEmail() != null ? memberVo.getMemberEmail() : member.getMemberEmail());
        member.setAdminYn(memberVo.getAdminYn() != null ? memberVo.getAdminYn() : member.getAdminYn());
    
        member.setModifyDateTime(LocalDateTime.now());
        member.setRegisterId("psy");
    }

    @Transactional
    public void deleteMember(String memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        memberRepository.delete(member);
    }    

}
