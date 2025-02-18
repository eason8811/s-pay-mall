package xin.eason.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductVO {
    private String productId;
    private String productName;
    private String productDesc;
    private BigDecimal price;
}
