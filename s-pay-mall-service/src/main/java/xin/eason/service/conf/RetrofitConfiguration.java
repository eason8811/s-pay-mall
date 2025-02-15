package xin.eason.service.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import xin.eason.service.wechat.IWechatService;

@Configuration
public class RetrofitConfiguration {
    private static final String BASE_URL = "https://api.weixin.qq.com";

    /**
     * 实例化 <b>Retrofit</b> 对象, 设置 <b>base URL</b> 和序列化工厂
     * @return <b>Retrofit</b> 实例化对象, 并生成 Bean 交给IOC容器管理
     */
    @Bean
    public Retrofit retrofit(){
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create(
                        new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                ))
                .build();
    }

    /**
     * 创建 <b>IWechatService</b> 接口实例
     * @param retrofit 实例化的 <b>Retrofit</b> 对象
     * @return <b>IWechatService</b> 接口实例
     */
    @Bean
    public IWechatService iWechatService(Retrofit retrofit){
        return retrofit.create(IWechatService.class);
    }
}
