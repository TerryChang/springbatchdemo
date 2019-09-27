package com.terry.springbatchdemo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name="USER")
@Getter
@EqualsAndHashCode
@Access(AccessType.FIELD)
public class User {

    private Long idx;

    @Column(name="LOGIN_ID")
    private String loginId;

    @Column(name="NAME")
    private String name;

    public User() {

    }

    public User(Long idx, String loginId, String name) {
        this.idx = idx;
        this.loginId = loginId;
        this.name = name;
    }

    @Id
    @Column(name="idx")
    @Access(AccessType.PROPERTY)
    public Long getIdx() {
        return idx;
    }
}
