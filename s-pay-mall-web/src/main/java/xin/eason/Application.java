package xin.eason;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import xin.eason.common.properties.AlipayProperties;
import xin.eason.common.properties.WechatProperties;

@SpringBootApplication
@EnableConfigurationProperties({WechatProperties.class, AlipayProperties.class})
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
