package xin.eason.domain.po;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("xml")
public class QrCodeScanEventEntity {
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

    @XStreamAlias("EventKey")
    private String eventKey;

    @XStreamAlias("Ticket")
    private String ticket;

}
