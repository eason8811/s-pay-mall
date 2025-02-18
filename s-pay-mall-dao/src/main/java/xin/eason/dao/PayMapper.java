package xin.eason.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import xin.eason.domain.po.PayOrder;

@Mapper
public interface PayMapper extends BaseMapper<PayOrder> {
}
