package xin.eason.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {
    private String grantType;

    private String appid;

    private String secret;

    private String originalid;

    private String token;
}
