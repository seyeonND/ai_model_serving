package com.jpa.spring.jpaspring.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jpa.spring.jpaspring.entity.Member;
import com.jpa.spring.jpaspring.service.MemberService;
import com.jpa.spring.jpaspring.vo.MemberVo.MemberGetVo;
import com.jpa.spring.jpaspring.vo.MemberVo.MemberSaveVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 모든 멤버 조회 (GET /api/members)
    @GetMapping
    public List<MemberGetVo> getAllMembers() {
        return memberService.getListMember();
    }

    @GetMapping("/{memberId}")
    public MemberGetVo getMember(@PathVariable String memberId) {
        return memberService.getMember(memberId);
    }

    @PostMapping
    public ResponseEntity<MemberGetVo> createMember(@RequestBody MemberSaveVo memberVo) {
        Member member = memberService.insertMember(memberVo);
        MemberGetVo memberGetVo = MemberGetVo.builder()
            .memberId(member.getMemberId()) 
            .memberName(member.getMemberName())
            .memberEmail(member.getMemberEmail())
            .adminYn(member.getAdminYn())
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(memberGetVo);
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<MemberGetVo> updateMember(
            @PathVariable String memberId,
            @RequestBody MemberSaveVo memberVo) {
        memberService.updateMember(memberId, memberVo);
        MemberGetVo memberGetVo = memberService.getMember(memberId);
        return ResponseEntity.ok(memberGetVo);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable String memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
