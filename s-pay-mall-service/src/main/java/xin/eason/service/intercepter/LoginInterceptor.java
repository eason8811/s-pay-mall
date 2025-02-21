package xin.eason.service.intercepter;

import com.google.common.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import xin.eason.common.util.JwtUtil;

@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    private final Cache<String, String> cache;

    private String ticket;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String token = request.getHeader("Auth-Key");
            String userInfo = jwtUtil.parse(token);
            String ticket = userInfo.split("@")[0];
            String openId = userInfo.split("@")[1];
            this.ticket = ticket;
            cache.put(ticket, openId);
            return true;
        } catch (Exception e) {
            log.error("登录校验失败！");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        cache.invalidate(ticket);
    }

}
