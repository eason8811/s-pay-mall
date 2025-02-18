package xin.eason.domain.po;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("xml")
public class LocationEventEntity {
    @XStreamAlias("ToUserName")
    private String toUserName;

    @XStreamAlias("FromUserName")
    private String fromUserName;

    @XStreamAlias("CreateTime")
    private String createTime;

    @XStreamAlias("MsgType")
    private String msgType;

    @XStreamAlias("Event")
    private String event;

    @XStreamAlias("Latitude")
    private String latitude;    // 纬度

    @XStreamAlias("Longitude")
    private String longitude;   // 经度

    @XStreamAlias("Precision")
    private String precision;   // 地理位置精度
}
