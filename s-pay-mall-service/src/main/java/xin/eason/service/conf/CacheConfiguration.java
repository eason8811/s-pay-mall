package xin.eason.service.conf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfiguration {

    /**
     * 配置本地缓存
     * @return 本地缓存实例化对象
     */
    @Bean
    public Cache<String, String> cache(){
        return CacheBuilder.newBuilder()
                .expireAfterWrite(7200, TimeUnit.SECONDS)
                .build();
    }
}
