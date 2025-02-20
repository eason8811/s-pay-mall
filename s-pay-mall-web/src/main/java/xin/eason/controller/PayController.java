package xin.eason.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.eason.common.properties.AlipayProperties;
import xin.eason.domain.req.ShopCartReq;
import xin.eason.domain.res.PayOrderRes;
import xin.eason.domain.res.Result;
import xin.eason.service.PayService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/alipay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    private final AlipayProperties alipayProperties;

    /**
     * 创建流水订单和支付订单接口
     * @param shopCartReq 购物车请求对象, 包含了 <b>用户ID</b> 和 <b>商品ID</b>
     * @return 带有 用户ID, 订单ID, 订单状态 和 支付URL 的对象
     */
    @PostMapping("/create_pay_order")
    public Result<PayOrderRes> pay(@RequestBody ShopCartReq shopCartReq){
        try {
            log.info("正在创建订单...");
            PayOrderRes order = payService.createOrder(shopCartReq);
            log.info("创建订单成功!");
            return Result.success(order);
        } catch (Exception e) {
            log.error("创建订单错误! 错误信息: ", e);
            return Result.error("创建订单错误");
        }
    }

    @PostMapping("/alipay_notify_url")
    public String alipayNotify(HttpServletRequest request){
        try {
            log.info("支付回调，消息接收 {}", request.getParameter("trade_status"));
            return payService.paySuccess(request);
        } catch (RuntimeException | AlipayApiException e) {
            log.error("支付宝回调处理失败! 错误信息: \n", e);
            return "false";
        }
    }


}
