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
import java.util.ArrayList;
import java.util.List;

public class ShoppingCartVODeserializer extends StdDeserializer<ShoppingCartVO> {
    private ObjectMapper objectMapper;

    private ShoppingCartVODeserializer() {
        this(ShoppingCartVO.class);
    }
    private ShoppingCartVODeserializer(Class<?> vc) {
        super(vc);
    }

    public ShoppingCartVODeserializer(ObjectMapper objectMapper) {
        this();
        this.objectMapper = objectMapper;
    }

    // {"loginId":"pivotal.com","shoppingItemList":[{"product":{"productId":"5","productPrice":900},"cnt":1,"totalPriceByProduct":900},{"product":{"productId":"6","productPrice":1500},"cnt":2,"totalPriceByProduct":3000},{"product":{"productId":"3","productPrice":1000},"cnt":5,"totalPriceByProduct":5000},{"product":{"productId":"3","productPrice":1000},"cnt":5,"totalPriceByProduct":5000},{"product":{"productId":"1","productPrice":500},"cnt":4,"totalPriceByProduct":2000}],"totalPrice":15900}
    @Override
    public ShoppingCartVO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode jsonNode = p.getCodec().readTree(p);
        ShoppingCartVO shoppingCartVO = null;
        String loginId = jsonNode.get("loginId").textValue();
        long totalPrice = jsonNode.get("totalPrice").longValue();
        JsonNode shoppingItemListJsonNode = jsonNode.get("shoppingItemList");
        if(shoppingItemListJsonNode.isArray()) {
            List<ShoppingItemVO> shoppingItemList = new ArrayList<>();
            for(JsonNode item : shoppingItemListJsonNode) {
                ShoppingItemVO shoppingItemVO = objectMapper.convertValue(item, ShoppingItemVO.class);
                shoppingItemList.add(shoppingItemVO);
            }
            shoppingCartVO = new ShoppingCartVO(loginId, shoppingItemList, totalPrice);
        } else {
            // shoppingItemList 항목이 배열이 아니기 때문에 예외처리
        }
        return shoppingCartVO;
    }
}
