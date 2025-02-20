package xin.eason.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.BiConsumer;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import xin.eason.common.constant.MQMsgConstant;
import xin.eason.common.constant.OrderStatusEnum;
import xin.eason.common.properties.AlipayProperties;
import xin.eason.dao.PayMapper;
import xin.eason.domain.po.PayOrder;
import xin.eason.domain.req.ShopCartReq;
import xin.eason.domain.res.PayOrderRes;
import xin.eason.domain.vo.ProductVO;
import xin.eason.service.PayService;
import xin.eason.service.rpc.ProductRpc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl extends ServiceImpl<PayMapper, PayOrder> implements PayService {

    private final ProductRpc productRpc;

    private final AlipayClient alipayClient;

    private final AlipayProperties alipayProperties;

    private final RabbitTemplate rabbitTemplate;

    /**
     * 创建订单 (流水单)
     *
     * @param shopCartReq 购物车请求对象，包含了创建订单所需的 用户ID <b>userId</b> 和商品ID <b>productId</b>
     * @return 封装了 用户ID, 生成的订单ID, 支付的Url, 订单状态 的对象
     */
    @Override
    public PayOrderRes createOrder(ShopCartReq shopCartReq) {
        // 查询 该用户 对于这个 商品ID 是否有未支付订单
        PayOrder unPayOrder = lambdaQuery()
                .eq(PayOrder::getUserId, shopCartReq.getUserId())
                .eq(PayOrder::getProductId, shopCartReq.getProductId())
                .one();
        log.info("正在查询是否有未支付订单...");
        if (unPayOrder != null && unPayOrder.getStatus().equals(OrderStatusEnum.PAY_WAITE)) {
            // 存在未支付订单, 直接返回未支付订单信息
            log.info("存在未支付订单, 订单 ID: {}, 用户 ID: {}", unPayOrder.getOrderId(), shopCartReq.getUserId());
            return PayOrderRes.builder()
                    .userId(shopCartReq.getUserId())
                    .orderId(unPayOrder.getOrderId())
                    .payUrl(unPayOrder.getPayUrl())
                    .orderStatus(OrderStatusEnum.PAY_WAITE)
                    .build();
        } else if (unPayOrder != null && unPayOrder.getStatus().equals(OrderStatusEnum.CREATE)) {
            // 发生掉单, 流水单存在但是没有支付单
            log.info("发生掉单");
            // 对接支付宝 SDK
            PayOrder payOrder = doPrePay(
                    shopCartReq.getProductId(),
                    unPayOrder.getProductName(),
                    unPayOrder.getOrderId(),
                    unPayOrder.getTotalAmount()
            );
            return PayOrderRes.builder()
                    .userId(shopCartReq.getUserId())
                    .orderId(payOrder.getOrderId())
                    .payUrl(payOrder.getPayUrl())
                    .orderStatus(payOrder.getStatus())
                    .build();
        }

        // 不存在未支付订单, 创建流水单和支付单

        // 根据 productId 查询具体商品信息
        log.info("正在创建流水单和支付单...");
        ProductVO productVO = productRpc.queryProductInfoById(shopCartReq.getProductId());
        LocalDateTime nowTime = LocalDateTime.now();
        log.info("正在使用 RPC 通过 productId 获取商品信息...");

        PayOrder payOrder = PayOrder.builder()
                .userId(shopCartReq.getUserId())
                .productId(shopCartReq.getProductId())
                .productName(productVO.getProductName())
                .orderId(String.valueOf(System.currentTimeMillis()).substring(0, 10) + String.valueOf(RandomUtil.randomInt(1000)))
                .orderTime(nowTime)
                .totalAmount(productVO.getPrice())
                .status(OrderStatusEnum.CREATE)
                .payUrl("暂无 Url")
                .payTime(nowTime)
                .createTime(nowTime)
                .updateTime(nowTime)
                .build();

        // 存入数据库
        save(payOrder);
        log.info("创建订单成功: {}", payOrder);

        // 调用支付宝 SDK 创建支付单
        PayOrder waitingPayOrder = doPrePay(shopCartReq.getProductId(), payOrder.getProductName(), payOrder.getOrderId(), payOrder.getTotalAmount());

        return PayOrderRes.builder()
                .userId(shopCartReq.getUserId())
                .orderId(payOrder.getOrderId())
                .payUrl(waitingPayOrder.getPayUrl())
                .orderStatus(waitingPayOrder.getStatus())
                .build();
    }

    /**
     * 与支付宝平台对接, 创建支付单
     *
     * @param productId   商品 ID
     * @param orderId     唯一的订单ID
     * @param totalAmount 订单总价格
     * @return PayOrder 订单对象
     */
    private PayOrder doPrePay(String productId, String productName, String orderId, BigDecimal totalAmount) {
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setNotifyUrl(alipayProperties.getNotifyUrl());   // 设置支付宝平台异步回调请求的 Url
        payRequest.setReturnUrl(alipayProperties.getReturnUrl());   // 设置支付完成后跳转的页面的 Url

        /*JSONObject jsonObject = new JSONObject();*/
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", orderId);
        jsonObject.put("subject", productName);
        jsonObject.put("total_amount", totalAmount.toString());
        jsonObject.put("product_code", "FAST_INSTANT_TRADE_PAY");

        payRequest.setBizContent(jsonObject.toJSONString());     // 设置具体的请求体
        String payUrl = null;

        try {
            AlipayTradePagePayResponse payResponse = alipayClient.pageExecute(payRequest);
            if (payResponse != null)
                payUrl = payResponse.getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }

        if (payUrl == null || payUrl.isBlank()) {
            throw new RuntimeException("payUrl 获取失败, payUrl 为 null");
        }

        PayOrder payOrder = PayOrder.builder()
                .payUrl(payUrl)
                .orderId(orderId)
                .status(OrderStatusEnum.PAY_WAITE)
                .build();
        log.info("创建支付订单成功, payOrder: {}", payOrder);

        lambdaUpdate().eq(PayOrder::getOrderId, payOrder.getOrderId()).update(payOrder);
        log.info("支付 URL: \n{}", payUrl);
        return payOrder;
    }

    /**
     * 支付宝验签流程, 验证请求是否是支付宝发送的, 并确认支付状态, 然后更新到数据库
     *
     * @param request 请求对象
     * @return <b>success</b> 或 <b>false</b> 支付状态
     */
    public String paySuccess(HttpServletRequest request) throws AlipayApiException {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            String tradeNo = params.get("out_trade_no");
            String gmtPayment = params.get("gmt_payment");
            String alipayTradeNo = params.get("trade_no");

            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayProperties.getAlipayPublicKey(), "UTF-8"); // 验证签名
            // 支付宝验签
            if (checkSignature) {
                // 验签通过
                log.info("支付回调，交易名称: {}", params.get("subject"));
                log.info("支付回调，交易状态: {}", params.get("trade_status"));
                log.info("支付回调，支付宝交易凭证号: {}", params.get("trade_no"));
                log.info("支付回调，商户订单号: {}", params.get("out_trade_no"));
                log.info("支付回调，交易金额: {}", params.get("total_amount"));
                log.info("支付回调，买家在支付宝唯一id: {}", params.get("buyer_id"));
                log.info("支付回调，买家付款时间: {}", params.get("gmt_payment"));
                log.info("支付回调，买家付款金额: {}", params.get("buyer_pay_amount"));
                log.info("支付回调，支付回调，更新订单 {}", tradeNo);
                // 更新数据库订单支付状态

                lambdaUpdate().eq(PayOrder::getOrderId, tradeNo).update(PayOrder.builder().status(OrderStatusEnum.PAY_SUCCESS).build());
                // 向支付成功消息队列发送消息
                CorrelationData cd = initCD();
                rabbitTemplate.convertAndSend(MQMsgConstant.PAY_SUCCESS_EXCHANGE, MQMsgConstant.PAY_SUCCESS_ROUTING_KEY, tradeNo, cd);

                return "success";
            }
        }
        return "false";
    }

    /**
     * 为 CorrelationData 对象添加 Callback 方法, 实现发送者确认
     * @return 配置好的 CorrelationData 对象
     */
    public CorrelationData initCD() {
        CorrelationData cd = new CorrelationData();
        cd.getFuture().whenComplete((BiConsumer<CorrelationData.Confirm, Throwable>) (confirm, throwable) -> {
            if (throwable != null) {
                log.error("消息发送过程出现错误: \n", throwable);
                return;
            }
            if (confirm.isAck()) {
                log.info("消息发送成功! {}", confirm);
            } else {
                log.info("消息发送失败, 原因: {}", confirm.getReason());
            }
        });
        return cd;
    }

}
