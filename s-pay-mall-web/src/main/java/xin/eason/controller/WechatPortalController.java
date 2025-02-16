package xin.eason.controller;

import com.google.common.cache.Cache;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xin.eason.common.properties.WechatProperties;
import xin.eason.domain.req.WechatReceiveReq;
import xin.eason.domain.req.WechatSignReq;
import xin.eason.service.wechat.IWechatReceiveService;
import xin.eason.service.wechat.IWechatService;

@Data
@Slf4j
@RestController
@RequestMapping("/api/v1/weixin/portal")
@RequiredArgsConstructor
public class WechatPortalController {

    private final IWechatReceiveService iWechatReceiveService;

    /**
     * 验证消息来自微信公众平台接口
     * @param wechatSignReq 微信公众平台发来的 <b>验签</b> 请求体对象
     * @return 验签成功则返回 echostr
     */
    @GetMapping(value = "/receive", produces = "text/plain;charset=utf-8")
    public String validate(WechatSignReq wechatSignReq) {
        return iWechatReceiveService.validate(wechatSignReq);
    }

    /**
     * 接收微信公众号平台的回调请求
     *
     * @param requestBody  XML 格式的请求体
     * @param signature    签名
     * @param timestamp    时间戳
     * @param nonce        随机数
     * @param openid       发起这个推送动作的微信用户的 <b>openId</b>
     * @param encType      加密方式
     * @param msgSignature 消息签名
     * @return 回复给公众平台的消息
     */
    @PostMapping(value = "receive", produces = "application/xml; charset=UTF-8")
    public String eventReceive(
            @RequestBody String requestBody,
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("openid") String openid,
            @RequestParam(name = "encrypt_type", required = false) String encType,
            @RequestParam(name = "msg_signature", required = false) String msgSignature
    ) {
        WechatReceiveReq wechatReceiveReq = WechatReceiveReq.builder()
                .requestBody(requestBody)
                .signature(signature)
                .timestamp(timestamp)
                .nonce(nonce)
                .openid(openid)
                .encType(encType)
                .msgSignature(msgSignature)
                .build();
        return iWechatReceiveService.receiveScanEvent(wechatReceiveReq);
    }


}
