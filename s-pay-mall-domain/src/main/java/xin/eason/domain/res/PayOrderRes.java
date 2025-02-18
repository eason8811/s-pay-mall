package xin.eason.domain.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xin.eason.common.constant.OrderStatusEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderRes {
    private String userId;
    private String orderId;
    private String payUrl;
    private OrderStatusEnum orderStatus;

}
