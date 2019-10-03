package com.terry.springbatchdemo.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@SequenceGenerator(
        name = "userIdxSequenceGenerator"
        , sequenceName = "USER_IDX_SEQUENCE"
        , initialValue = 1
        , allocationSize = 1
)
@Table(name="USER")
@Getter
@EqualsAndHashCode
@ToString
@Access(AccessType.FIELD)
public class User {

    private Long idx;

    @Column(name="NAME", nullable = false)
    private String name;

    @Column(name="LOGIN_ID", nullable = false)
    private String loginId;

    @OneToMany
    private List<ShoppingCart> shoppingCartList;

    public User() {

    }

    @Builder
    public User(String loginId, String name) {
        this.loginId = loginId;
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userIdxSequenceGenerator")
    @Column(name="IDX")
    @Access(AccessType.PROPERTY)
    public Long getIdx() {
        return idx;
    }

    private void setIdx(Long idx) {
        this.idx = idx;
    }
}
