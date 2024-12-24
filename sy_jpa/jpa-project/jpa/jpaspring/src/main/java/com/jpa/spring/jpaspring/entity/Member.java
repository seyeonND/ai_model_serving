package com.jpa.spring.jpaspring.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
public class Member {

    @Id
    private String memberId;
    private String memberName;
    private String memberPassword;
    private String memberEmail;
    @Builder.Default
    private String adminYn = "N";

    // @CreatedDate
    private LocalDateTime registerDateTime;
    // @CreatedBy
    private String registerId;

    // @LastModifiedDate
    private LocalDateTime modifyDateTime;
    // @CreatedBy
    private String modifyId;

    protected Member(){}

}
