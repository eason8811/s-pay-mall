package xin.eason.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xin.eason.common.constant.OrderStatusEnum;
import xin.eason.domain.po.PayOrder;
import xin.eason.service.PayService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloseTimeOutOrder {

    private final PayService payService;

    /**
     * 每三秒检测一次数据库, 如果有到目前时间超过 15 分钟的订单未支付 (PAY_WAIT) 就关单 将其状态转为 (CLOSE)
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void exec() {
        LocalDateTime now = LocalDateTime.now();
        List<PayOrder> unPayOrderList = payService.lambdaQuery()
                .lt(PayOrder::getOrderTime, now.plusMinutes(-15))
                .eq(PayOrder::getStatus, OrderStatusEnum.PAY_WAITE)
                .list();
        if (unPayOrderList == null || unPayOrderList.isEmpty())
            return;

        List<String> orderIdList = new ArrayList<>();
        unPayOrderList.forEach(order -> {
            orderIdList.add(order.getOrderId());
            order.setStatus(OrderStatusEnum.CLOSE);
        });

        payService.lambdaUpdate()
                .in(PayOrder::getOrderId, orderIdList)
                .set(PayOrder::getStatus, OrderStatusEnum.CLOSE)
                .update();
        log.info("超时订单已关单: {}", unPayOrderList);
    }
}
