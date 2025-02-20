package xin.eason.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import xin.eason.common.constant.MQMsgConstant;
import xin.eason.common.constant.OrderStatusEnum;
import xin.eason.dao.PayMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSuccessListener {

    private final PayMapper payMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(MQMsgConstant.PAY_SUCCESS_QUEUE),
            exchange = @Exchange(MQMsgConstant.PAY_SUCCESS_EXCHANGE),
            key = MQMsgConstant.PAY_SUCCESS_ROUTING_KEY
    ))
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
