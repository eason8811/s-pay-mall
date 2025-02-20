package xin.eason.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xin.eason.common.constant.OrderStatusEnum;
import xin.eason.domain.po.PayOrder;
import xin.eason.service.PayService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendProductJob {

    private final PayService payService;

    /**
     * 查询已支付完成但未发货订单, 实现自动发货
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void exec(){
        List<PayOrder> paySuccessOrderList = payService.lambdaQuery()
                .eq(PayOrder::getStatus, OrderStatusEnum.PAY_SUCCESS)
                .list();
        paySuccessOrderList.forEach(order -> {
            log.info("订单ID: {} 已发货", order.getOrderId());
            order.setStatus(OrderStatusEnum.DEAL_DONE);
        });

        payService.lambdaUpdate()
                .in(PayOrder::getOrderId, paySuccessOrderList.stream().map(PayOrder::getOrderId).toList())
                .set(PayOrder::getStatus, OrderStatusEnum.DEAL_DONE)
                .update();
    }
}
