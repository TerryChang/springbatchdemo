package com.terry.springbatchdemo.config;

import com.terry.springbatchdemo.entity.Product;
import com.terry.springbatchdemo.entity.ShoppingCart;
import com.terry.springbatchdemo.entity.ShoppingItem;
import com.terry.springbatchdemo.entity.User;
import com.terry.springbatchdemo.repository.ProductRepository;
import com.terry.springbatchdemo.repository.UserRepository;
import com.terry.springbatchdemo.vo.ProductVO;
import com.terry.springbatchdemo.vo.ShoppingCartVO;
import com.terry.springbatchdemo.vo.ShoppingItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomItemProcessor implements ItemProcessor<ShoppingCartVO, ShoppingCart> {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public ShoppingCart process(ShoppingCartVO item) throws Exception {
        Optional<User> optionalUser = userRepository.findByLoginId(item.getLoginId());
        if(!optionalUser.isPresent()) {
            // 사용자가 없다는 예외 던지는 부분
            throw new BatchException(item, "ShoppingItem - Not Exists User : " + item.getLoginId());
        }
        User userEntity = optionalUser.get();
        ShoppingCart shoppingCartEntity = ShoppingCart.builder()
                                                .user(userEntity)
                                                .lineNumber(item.getLineNumber())
                                                .lineContent(item.getLineContent())
                                                .lineContentForJson(item.getLineContentForJson())
                                            .build();

        List<ShoppingItemVO> shoppingItemVOList = item.getShoppingItemList();
        int subIdentifier = 0;
        for(ShoppingItemVO shoppingItem : shoppingItemVOList) {
            ProductVO productVO = shoppingItem.getProduct();
            long productPrice = productVO.getProductPrice();
            int cnt = shoppingItem.getCnt();
            long totalPriceByProduct = shoppingItem.getTotalPriceByProduct();

            if(productPrice * cnt != totalPriceByProduct) {
                // 상품단가 * 갯수 != 상품별 총 가격일 경우 맞지 않다고 예외를 던지는 부분
                throw new BatchException(item, "ShoppingItem - Mismatch product price * cnt and totalPrice");
            }

            Optional<Product> optionalProduct = productRepository.findById(productVO.getIdx());
            Product productEntity = null;
            if(optionalProduct.isPresent()) {
                productEntity = optionalProduct.get();
                if(productEntity.getProductPrice() != productPrice) {
                    // 상품 단가가 등록되어 있는 상품단가와 틀리다는 예외를 던지는 부분
                    throw new BatchException(item, "ShoppingItem - Mismatch already database product price and log product price");
                }
                if(productEntity.getProductPrice() * cnt != totalPriceByProduct) {
                    // 등록된 상품 단가 * 갯수와 등록하고자 하는 총액이 틀리다는 예외를 던지는 부분
                    throw new BatchException(item, "ShoppingItem - Mismatch database product price * cnt and log total price");
                }
                // ShoppingItem shoppingItemEntity = new ShoppingItem(productEntity, cnt);
                ShoppingItem shoppingItemEntity = ShoppingItem.builder().product(productEntity).cnt(cnt).subIdentifier(subIdentifier++).build();
                shoppingItemEntity.setShoppingCart(shoppingCartEntity); // setShoppingCart 메소드에서 내부적으로 ShoppingCart 엔티티 객체에 ShoppingItem 객체를 추가해주고 있다
            } else {
                // 상품이 없다는 예외 던지는 부분
                throw new BatchException(item, "Product - Product idx " + productVO.getIdx() + " is not exists");
            }
        }
        return shoppingCartEntity;
    }
}
