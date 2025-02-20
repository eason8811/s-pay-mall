package xin.eason.service;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.BiConsumer;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import xin.eason.domain.po.PayOrder;
import xin.eason.domain.req.ShopCartReq;
import xin.eason.domain.res.PayOrderRes;

public interface PayService extends IService<PayOrder> {

    /**
     * 创建订单 (流水单)
     *
     * @param shopCartReq 购物车请求对象，包含了创建订单所需的 用户ID <b>userId</b> 和商品ID <b>productId</b>
     * @return 封装了 用户ID, 生成的订单ID, 支付的Url, 订单状态 的对象
     */
    PayOrderRes createOrder(ShopCartReq shopCartReq);

    /**
     * 支付宝验签流程, 验证请求是否是支付宝发送的, 并确认支付状态, 然后更新到数据库
     *
     * @param request 请求对象
     * @return <b>success</b> 或 <b>false</b> 支付状态
     */
    String paySuccess(HttpServletRequest request) throws AlipayApiException;

    /**
     * 为 CorrelationData 对象添加 Callback 方法, 实现发送者确认
     *
     * @return 配置好的 CorrelationData 对象
     */
    CorrelationData initCD();
}
