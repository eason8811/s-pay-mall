package xin.eason.job;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xin.eason.common.constant.OrderStatusEnum;
import xin.eason.domain.po.PayOrder;
import xin.eason.service.PayService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckNoNotifyOrder {

    private final PayService payService;

    private final AlipayClient alipayClient;

    private final EventBus eventBus;

    /**
     * 每秒检查状态为等待支付且未到期的订单, 向支付宝确认该订单的支付状态
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void exec(){
        try {
            LocalDateTime now = LocalDateTime.now();
            List<PayOrder> unPayOrderList = payService.lambdaQuery()
                    .eq(PayOrder::getStatus, OrderStatusEnum.PAY_WAITE)
                    .gt(PayOrder::getOrderTime, now.plusMinutes(-15))
                    .list();

            if (unPayOrderList == null || unPayOrderList.isEmpty())
                return;

            for (PayOrder order : unPayOrderList) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("out_trade_no", order.getOrderId());
                request.setBizContent(jsonObject.toJSONString());
                AlipayTradeQueryResponse response = alipayClient.execute(request);
                if ("10000".equals(response.getCode())){
                    payService.lambdaUpdate()
                            .eq(PayOrder::getOrderId, order.getOrderId())
                            .set(PayOrder::getStatus, OrderStatusEnum.PAY_SUCCESS)
                            .update();
                    log.info("查询到未更新的未支付订单, 订单ID: {}, 已经更改为支付成功!", order.getOrderId());
                    eventBus.post(order.getOrderId());
                }
            }
        } catch (AlipayApiException e) {
            log.error("检查未超时的等待支付状态订单失败, 错误信息: \n", e);
        }

    }
}
