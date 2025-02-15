package xin.eason;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import xin.eason.domain.req.WechatReq;

@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,        // 禁用数据源自动配置
                HibernateJpaAutoConfiguration.class,      // 禁用 JPA 自动配置
                DataSourceTransactionManagerAutoConfiguration.class  // 禁用事务管理
        }
)
@EnableConfigurationProperties(WechatReq.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
