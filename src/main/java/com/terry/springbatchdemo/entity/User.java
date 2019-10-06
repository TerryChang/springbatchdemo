package com.terry.springbatchdemo.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@SequenceGenerator(
        name = "userIdxSequenceGenerator"
        , sequenceName = "USER_IDX_SEQUENCE"
        , initialValue = 1
        , allocationSize = 1
)
@Table(name="USER")
@NoArgsConstructor
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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<ShoppingCart> shoppingCartList = new ArrayList<>();

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

    public void update(String name, String loginId) {
        this.name = name;
        this.loginId = loginId;
    }


}
