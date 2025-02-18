package xin.eason.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;



@Getter
@RequiredArgsConstructor
public enum OrderStatusEnum {
    CREATE("CREATE", "建完成－如果调单了，也会从创建记录重新发起创建支付"),
    PAY_WAITE("PAY_WAITE", "等待支付－订单创建完成后，创建支付单"),
    PAY_SUCCESS("PAY_SUCCESS", "付成功－接收到支付回调消息"),
    DEAL_DONE("DEAL_DONE", "交易完成－商品发货完成"),
    CLOSE("CLOSE", "超时关单－超市未支付");

    @EnumValue
    private final String code;
    @JsonValue
    private final String desc;
}
