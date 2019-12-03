package com.terry.springbatchdemo.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.terry.springbatchdemo.config.LineInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Builder(builderClassName = "ShoppingCartVOBuilder", toBuilder = true)
@JsonDeserialize(builder = ShoppingCartVO.ShoppingCartVOBuilder.class)
public class ShoppingCartVO extends LineInfo {
    private String loginId;
    List<ShoppingItemVO> shoppingItemList;
    private long totalPrice;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ShoppingCartVOBuilder {

    }

}
