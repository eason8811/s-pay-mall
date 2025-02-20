package xin.eason.service.listener;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xin.eason.common.constant.OrderStatusEnum;
import xin.eason.dao.PayMapper;
import xin.eason.domain.po.PayOrder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSuccessListener {

    private final PayMapper payMapper;

    @Subscribe
    public void listener(String paySuccessOrderId) {
        try {
            log.info("已收到支付成功回调 MQ 消息: {}", paySuccessOrderId);
            payMapper.doneOrder(paySuccessOrderId, OrderStatusEnum.DEAL_DONE);
            log.info("订单ID: {} 发货成功!", paySuccessOrderId);
        } catch (Exception e) {
            log.error("更新支付成功订单信息出错! 订单ID: {}", paySuccessOrderId);
        }
    }
}
