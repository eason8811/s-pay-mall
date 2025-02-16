package xin.eason.domain.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WechatReceiveReq {
    private String requestBody;

    private String signature;

    private String timestamp;

    private String nonce;

    private String openid;

    private String encType;

    private String msgSignature;
}
