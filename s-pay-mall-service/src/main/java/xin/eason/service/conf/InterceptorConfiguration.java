package xin.eason.service.conf;

import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xin.eason.common.util.JwtUtil;
import xin.eason.service.intercepter.LoginInterceptor;

@Component
@RequiredArgsConstructor
public class InterceptorConfiguration implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;

    private final Cache<String, String> cache;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LoginInterceptor interceptor = new LoginInterceptor(jwtUtil, cache);
        InterceptorRegistration registration = registry.addInterceptor(interceptor);
        // 排除 login 路径的拦截
        registration.excludePathPatterns("/api/v1/login/**");
        registration.excludePathPatterns("/api/v1/weixin/portal/**");
//        registration.excludePathPatterns("/api/v1/alipay/**");
    }
}
