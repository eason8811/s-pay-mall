package xin.eason.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl extends ServiceImpl<PayMapper, PayOrder> implements PayService {

    private final ProductRpc productRpc;

    private final AlipayClient alipayClient;

    private final AlipayProperties alipayProperties;

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
            // TODO: 对接支付宝 SDK
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
                .orderId(String.valueOf(System.currentTimeMillis()).substring(0,10) + String.valueOf(RandomUtil.randomInt(1000)))
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

        // TODO: 调用支付宝 SDK 创建支付单
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
     * @param productId 商品 ID
     * @param orderId 唯一的订单ID
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

}
