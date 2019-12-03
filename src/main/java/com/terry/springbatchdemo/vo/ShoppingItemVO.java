package com.terry.springbatchdemo.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = ShoppingItemVO.ShoppingItemVOBuilder.class)
public class ShoppingItemVO {

    private ProductVO product;
    private int cnt;
    private int totalPriceByProduct;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ShoppingItemVOBuilder {

    }
}
