package xin.eason.service.conf;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xin.eason.common.properties.AlipayProperties;

@Configuration
public class AlipayConfiguration {

    /**
     * 将 alipayClient 实例化为 Bean 对象
     * @param alipayProperties alipay 配置对象
     * @return alipayClient 实例化的 Bean 对象
     */
    @Bean
    @ConditionalOnProperty(value = "alipay.enabled", havingValue = "true")
    public AlipayClient alipayClient(AlipayProperties alipayProperties) {
        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getMerchantPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType()
        );
    }
}
