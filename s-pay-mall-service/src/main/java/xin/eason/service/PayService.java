package xin.eason.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xin.eason.domain.po.PayOrder;
import xin.eason.domain.req.ShopCartReq;
import xin.eason.domain.res.PayOrderRes;

public interface PayService extends IService<PayOrder> {

    /**
     * 创建订单 (流水单)
     * @param shopCartReq 购物车请求对象，包含了创建订单所需的 用户ID <b>userId</b> 和商品ID <b>productId</b>
     * @return 封装了 用户ID, 生成的订单ID, 支付的Url, 订单状态 的对象
     */
    PayOrderRes createOrder(ShopCartReq shopCartReq);
}
