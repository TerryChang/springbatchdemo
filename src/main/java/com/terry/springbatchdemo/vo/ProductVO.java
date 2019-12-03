package com.terry.springbatchdemo.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = ProductVO.ProductVOBuilder.class)
public class ProductVO {

    private Long idx;
    private String productName;
    private int productPrice;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ProductVOBuilder {

    }
}