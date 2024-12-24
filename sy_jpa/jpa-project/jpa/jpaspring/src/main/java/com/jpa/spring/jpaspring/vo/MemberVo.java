package com.jpa.spring.jpaspring.vo;

import lombok.Builder;
import lombok.Getter;

public class MemberVo {

    @Builder
    @Getter
    public static class MemberGetVo{
        private String memberId;
        private String memberName;
        private String memberEmail;
        private String adminYn;
    }

    @Getter
    public static class MemberSaveVo{
        private String memberId;
        private String memberName;
        private String memberPassword;
        private String memberEmail;
        private String adminYn;
    }
}
