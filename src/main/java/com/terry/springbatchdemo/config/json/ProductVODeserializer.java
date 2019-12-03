package com.terry.springbatchdemo.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.terry.springbatchdemo.vo.ProductVO;
import com.terry.springbatchdemo.vo.ShoppingCartVO;

import java.io.IOException;

public class ProductVODeserializer extends StdDeserializer<ProductVO> {

    private ObjectMapper objectMapper;

    private ProductVODeserializer() {
        this(ShoppingCartVO.class);
    }
    private ProductVODeserializer(Class<?> vc) {
        super(vc);
    }

    public ProductVODeserializer(ObjectMapper objectMapper) {
        this();
        this.objectMapper = objectMapper;
    }

    // {"productId":"5","productPrice":900}
    @Override
    public ProductVO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode jsonNode = p.getCodec().readTree(p);
        Long idx = jsonNode.get("idx").longValue();
        int productPrice = jsonNode.get("productPrice").intValue();
        return ProductVO.builder().idx(idx).productPrice(productPrice).build();
    }
}
