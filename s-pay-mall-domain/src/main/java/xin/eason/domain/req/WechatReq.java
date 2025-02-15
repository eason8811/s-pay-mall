package xin.eason.domain.req;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 封装要向微信公众平台发送的信息
 */
@Data
@Component
@ConfigurationProperties(value = "wechat")
public class WechatReq {
    private String grantType;

    private String appid;

    private String secret;
}
