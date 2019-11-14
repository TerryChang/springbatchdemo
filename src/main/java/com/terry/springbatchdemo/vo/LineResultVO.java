package com.terry.springbatchdemo.vo;

import com.terry.springbatchdemo.entity.ShoppingCart;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class LineResultVO {
    private String line;
    private int lineNumber;
    private ShoppingCartVO shoppingCartVO;

    @Setter
    private ShoppingCart shoppingCart;

}
