package xin.eason.domain.req;

import lombok.Data;
import org.springframework.stereotype.Component;
import xin.eason.common.properties.WechatProperties;

/**
 * 封装要向微信公众平台发送的信息
 */
@Data
@Component
public class WechatReq {

    private final WechatProperties wechatProperties;

    private final String grantType;

    private final String appid;

    private final String secret;

    public WechatReq(WechatProperties wechatProperties){
        this.wechatProperties = wechatProperties;
        this.grantType = wechatProperties.getGrantType();
        this.appid = wechatProperties.getAppid();
        this.secret = wechatProperties.getSecret();
    }
}
