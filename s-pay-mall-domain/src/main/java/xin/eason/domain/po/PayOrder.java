package xin.eason.domain.po;

import lombok.Builder;
import lombok.Data;
import xin.eason.common.constant.OrderStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PayOrder {
    private Long id;
    private String userId;
    private String productId;
    private String productName;
    private String orderId;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private OrderStatusEnum status;
    private String payUrl;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
