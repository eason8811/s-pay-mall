package xin.eason.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xin.eason.common.constant.OrderStatusEnum;
import xin.eason.dao.PayMapper;
import xin.eason.domain.po.PayOrder;
import xin.eason.domain.req.ShopCartReq;
import xin.eason.domain.res.PayOrderRes;
import xin.eason.domain.vo.ProductVO;
import xin.eason.service.PayService;
import xin.eason.service.rpc.ProductRpc;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl extends ServiceImpl<PayMapper, PayOrder> implements PayService {

//    private final PayMapper payMapper;

    private final ProductRpc productRpc;

    /**
     * 创建订单 (流水单)
     *
     * @param shopCartReq 购物车请求对象，包含了创建订单所需的 用户ID <b>userId</b> 和商品ID <b>productId</b>
     * @return 封装了 用户ID, 生成的订单ID, 支付的Url, 订单状态 的对象
     */
    @Override
    public PayOrderRes createOrder(ShopCartReq shopCartReq) {
        // 查询 该用户 对于这个 商品ID 是否有未支付订单
        PayOrder payOrder = lambdaQuery()
                .eq(PayOrder::getUserId, shopCartReq.getUserId())
                .eq(PayOrder::getProductId, shopCartReq.getProductId())
                .one();
        log.info("正在查询是否有未支付订单...");
        if (payOrder != null && payOrder.getStatus().equals(OrderStatusEnum.PAY_WAITE)) {
            // 存在未支付订单, 直接返回未支付订单信息
            log.info("存在未支付订单, 订单 ID: {}, 用户 ID: {}", payOrder.getOrderId(), shopCartReq.getUserId());
            return PayOrderRes.builder()
                    .userId(shopCartReq.getUserId())
                    .orderId(payOrder.getOrderId())
                    .payUrl(payOrder.getPayUrl())
                    .orderStatus(OrderStatusEnum.PAY_WAITE)
                    .build();
        } else if (payOrder != null && payOrder.getStatus().equals(OrderStatusEnum.CREATE)) {
            // 发生掉单, 流水单存在但是没有支付单
            log.info("发生掉单");
            // TODO: 对接支付宝 SDK
        }

        // 不存在未支付订单, 创建流水单和支付单

        // 根据 productId 查询具体商品信息
        log.info("正在创建流水单和支付单...");
        ProductVO productVO = productRpc.queryProductInfoById(shopCartReq.getProductId());
        LocalDateTime nowTime = LocalDateTime.now();
        log.info("正在使用 RPC 通过 productId 获取商品信息...");

        payOrder = PayOrder.builder()
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
        PayOrderRes payOrderRes = new PayOrderRes();
        BeanUtil.copyProperties(payOrder, payOrderRes);
        payOrderRes.setOrderStatus(payOrder.getStatus());
        save(payOrder);
        log.info("创建订单成功: {}", payOrder);

        // TODO: 调用支付宝 SDK 创建支付单


        return payOrderRes;
    }

}
