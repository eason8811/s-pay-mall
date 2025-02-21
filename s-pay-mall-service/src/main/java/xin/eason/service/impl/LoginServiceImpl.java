package xin.eason.service.impl;

import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import xin.eason.common.constant.MessageConstant;
import xin.eason.common.constant.WechatConstant;
import xin.eason.common.properties.JwtProperties;
import xin.eason.common.util.JwtUtil;
import xin.eason.domain.req.WechatQrCodeReq;
import xin.eason.domain.req.WechatReq;
import xin.eason.domain.res.WechatQrCodeRes;
import xin.eason.domain.res.WechatRes;
import xin.eason.domain.vo.UserInfoVO;
import xin.eason.service.wechat.IWechatService;
import xin.eason.service.LoginService;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final IWechatService wechatService;

    private final Cache<String, String> cache;

    private final WechatReq wechatReq;

    private final JwtUtil jwtUtil;

    private final JwtProperties jwtProperties;

    /**
     * 获取 <b>Ticket</b> 业务流程
     *
     * @return 返回获取到的 <b>Ticket</b>
     */
    @Override
    public String getTicket() {
        String accessToken = getAccessToken();
        log.info("获取 Access Token 成功: {}", accessToken);
        log.info("正在获取 Ticket");
        WechatQrCodeReq wechatQrCodeReq = WechatQrCodeReq.builder()
                .actionName(WechatConstant.ACTION_NAME)
                .expireSeconds(WechatConstant.EXPIRE_SECONDS)
                .actionInfo(
                        WechatQrCodeReq.ActionInfo.builder()
                        .scene(WechatQrCodeReq.Scene.builder().scene_str(String.valueOf(System.currentTimeMillis())).build())
                        .build()
                )
                .build();

        try {
            Response<WechatQrCodeRes> response = wechatService.getQrCode(wechatQrCodeReq, accessToken).execute();
            WechatQrCodeRes qrCodeRes = response.body();
            String ticket = qrCodeRes.getTicket();

            assert ticket != null && !ticket.isBlank();

            log.info("获取 Ticket 成功: {}", ticket);

            return ticket;
        } catch (IOException e) {
            log.error("获取 Ticket 失败");
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查登录状态
     * @param ticket 用于生成二维码的 <b>Ticket</b>
     * @return 用户信息的视图对象
     */
    @Override
    public UserInfoVO checkLogin(String ticket) {
        String openId = cache.getIfPresent(ticket);
        String loginStatus = MessageConstant.NO_LOGIN;
        log.info("正在检查登录状态...");

        if (openId == null || openId.isBlank()) {
            log.info("用户: {} 的登录状态: {}", openId, loginStatus);
            return UserInfoVO.builder()
                    .token("")
                    .userOpenId("")
                    .ticket(ticket)
                    .loginStatus(loginStatus)
                    .build();
        }
        loginStatus = MessageConstant.LOGIN;
        String userInfo = ticket + "@" + openId;
        String token = jwtUtil.create(userInfo, jwtProperties.getTokenTTL());
        log.info("用户: {} 的登录状态: {}", openId, loginStatus);
        return UserInfoVO.builder()
                .token(token)
                .userOpenId(openId)
                .ticket(ticket)
                .loginStatus(loginStatus)
                .build();

    }

    /**
     * 获取 <b>Access Token</b> 流程
     * @return <b>Access Token</b>
     */
    private String getAccessToken() {
        // 判断缓存中是否存在 Access Token
        String accessToken = cache.getIfPresent("access_token");
        try {
            log.info("正在获取 Access Token");
            if (accessToken == null || accessToken.isBlank()) {
                log.info("缓存无 Access Token 向微信公众平台获取");
                Response<WechatRes> response = wechatService.getAccessToken(
                        wechatReq.getGrantType(),
                        wechatReq.getAppid(),
                        wechatReq.getSecret()
                ).execute();
                accessToken = response.body().getAccessToken();
                assert accessToken != null && !accessToken.isBlank();
            }
        } catch (IOException e) {
            log.error("获取 Access Token 失败");
            throw new RuntimeException(e);
        }
        cache.put("access_token", accessToken);
        return accessToken;
    }


}
