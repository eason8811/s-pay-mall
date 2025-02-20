package xin.eason.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xin.eason.common.constant.OrderStatusEnum;
import xin.eason.domain.po.PayOrder;

@Mapper
public interface PayMapper extends BaseMapper<PayOrder> {
    void doneOrder(String orderId, OrderStatusEnum status);
}
