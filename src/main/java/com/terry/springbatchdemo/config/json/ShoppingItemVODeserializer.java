package com.terry.springbatchdemo.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.terry.springbatchdemo.vo.ProductVO;
import com.terry.springbatchdemo.vo.ShoppingCartVO;
import com.terry.springbatchdemo.vo.ShoppingItemVO;

import java.io.IOException;

public class ShoppingItemVODeserializer extends StdDeserializer<ShoppingItemVO> {
    private ObjectMapper objectMapper;

    private ShoppingItemVODeserializer() {
        this(ShoppingItemVO.class);
    }
    private ShoppingItemVODeserializer(Class<?> vc) {
        super(vc);
    }

    public ShoppingItemVODeserializer(ObjectMapper objectMapper) {
        this();
        this.objectMapper = objectMapper;
    }

    // {"product":{"productId":"5","productPrice":900},"cnt":1,"totalPriceByProduct":900}
    @Override
    public ShoppingItemVO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode jsonNode = p.getCodec().readTree(p);
        JsonNode productJsonNode = jsonNode.get("product");
        ProductVO productVO = objectMapper.convertValue(productJsonNode, ProductVO.class);
        int productCnt = productJsonNode.get("cnt").intValue();
        int productTotalPrice = productJsonNode.get("totalPriceByProduct").intValue();
        ShoppingItemVO shoppingItemVO = new ShoppingItemVO(productVO, productCnt, productTotalPrice);

        return null;
    }
}
