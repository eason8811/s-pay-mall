package xin.eason.common.util;


import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;
import java.util.Map;


@Slf4j
@Component
public class JwtUtil {

    private final JWTSigner jwtSigner;

    public JwtUtil(KeyPair keyPair) {
        jwtSigner = JWTSignerUtil.createSigner("rs256", keyPair);
    }

    public String create(String userInfo, Duration ttl) {
        return JWT.create()
                .setSigner(jwtSigner)
                .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis()))
                .setPayload("userInfo", userInfo)
                .sign();
    }

    public String create(Map<String, String> userInfoMap, Duration ttl) {
        if (userInfoMap != null && !userInfoMap.isEmpty()) {
            String userInfo = userInfoMap.entrySet().stream()
                    .map(entry -> entry.getKey() + "@" + entry.getValue())
                    .findFirst()
                    .orElse("");
            return create(userInfo, ttl);
        }
        log.error("userInfoMap 不能为空!");
        throw new RuntimeException();
    }

    public String parse(String token) {
        if (token == null) {
            log.error("未登录!");
            throw new RuntimeException();
        }

        JWT jwt;
        String userInfo;
        try {
            jwt = JWT.of(token);
            jwt.setSigner(jwtSigner);
        } catch (Exception e) {
            log.error("token 不合法!");
            throw new RuntimeException(e);
        }

        if (!jwt.verify()) {
            log.error("token 不合法!");
            throw new RuntimeException();
        }

        if (!jwt.validate(0)) {
            log.error("token 已过期!");
            throw new RuntimeException();
        }

        try {
            userInfo = (String) jwt.getPayload("userInfo");
        } catch (Exception e) {
            log.error("token 不合法!");
            throw new RuntimeException(e);
        }

        if (userInfo == null || userInfo.isBlank()) {
            log.error("token 数据格式不合法!");
            throw new RuntimeException();
        }

        return userInfo;
    }


}
