package xin.eason.service.rpc;

import org.springframework.stereotype.Component;
import xin.eason.domain.vo.ProductVO;

import java.math.BigDecimal;

@Component
public class ProductRpc {
    /**
     * 根据商品ID <b>productId</b> 查询商品信息 (模拟 RPC 请求)
     * @param productId 商品 ID
     * @return 商品的视图对象 <b>ProductVO</b>
     */
    public ProductVO queryProductInfoById(String productId) {
        return ProductVO.builder()
                .productId(productId)
                .productName("测试商品")
                .productDesc("测试商品的描述")
                .price(new BigDecimal("666.66"))
                .build();
    }
}
