package xin.eason.service.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.cache.Cache;
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
import xin.eason.domain.po.PayOrder;
import xin.eason.service.wechat.IWechatReceiveService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSuccessListener {

    private final PayMapper payMapper;

    private final IWechatReceiveService wechatReceiveService;

    private final Cache<String, String> cache;

    private final String sendProductTemplateId = "BNGoy95cPSy1mjsFleykVN811OtXBO2rrsMUPuFMpKY";

    private final String turnToUrl = "https://www.baidu.com";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(MQMsgConstant.PAY_SUCCESS_QUEUE),
            exchange = @Exchange(MQMsgConstant.PAY_SUCCESS_EXCHANGE),
            key = MQMsgConstant.PAY_SUCCESS_ROUTING_KEY
    ))
    public void listener(String paySuccessOrderId) {
        try {
            log.info("已收到支付成功回调 MQ 消息: {}", paySuccessOrderId);
            // 创建 updateWrapper 表示 where 条件
            LambdaUpdateWrapper<PayOrder> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PayOrder::getOrderId, paySuccessOrderId);

            payMapper.update(PayOrder.builder().status(OrderStatusEnum.DEAL_DONE).build(), updateWrapper);
            log.info("订单ID: {} 发货成功!", paySuccessOrderId);

            // 创建 queryWrapper 表示 where 条件
            LambdaQueryWrapper<PayOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PayOrder::getOrderId, paySuccessOrderId);
            PayOrder payOrder = payMapper.selectOne(queryWrapper);

            // 装配数据
            HashMap<String, String> dataMap = new HashMap<>();
            dataMap.put("orderId", payOrder.getOrderId());
            dataMap.put("productName", payOrder.getProductName());
            dataMap.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            wechatReceiveService.sendTemplate(sendProductTemplateId, payOrder.getUserId(), turnToUrl, cache.getIfPresent("access_token"), dataMap);
        } catch (Exception e) {
            log.error("更新支付成功订单信息出错! 订单ID: {}", paySuccessOrderId);
        }
    }
}
