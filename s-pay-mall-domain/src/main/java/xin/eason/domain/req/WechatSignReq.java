package xin.eason.domain.req;

import lombok.Data;

@Data
public class WechatSignReq {
    private String signature;
    private String timestamp;
    private String nonce;
    private String echostr;
}
