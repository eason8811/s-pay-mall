package xin.eason.controller;

import com.google.common.cache.Cache;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import xin.eason.common.cons.WechatConstant;
import xin.eason.common.properties.WechatProperties;
import xin.eason.common.util.MessageTextEntity;
import xin.eason.common.util.QrCodeScanEventEntity;
import xin.eason.common.util.SignatureUtil;
import xin.eason.common.util.XmlUtil;

@Data
@Slf4j
@RestController
@RequestMapping("/api/v1/weixin/portal")
@RequiredArgsConstructor
public class WechatPortalController {

    private final WechatProperties wechatProperties;

    private final Cache<String, String> cache;

    @GetMapping(value = "receive", produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            log.info("微信公众号验签信息开始 [{}, {}, {}, {}]", signature, timestamp, nonce, echostr);
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }
            boolean check = SignatureUtil.check(wechatProperties.getToken(), signature, timestamp, nonce);
            log.info("微信公众号验签信息完成 check：{}", check);
            if (!check) {
                return null;
            }
            return echostr;
        } catch (Exception e) {
            log.error("微信公众号验签信息失败 [{}, {}, {}, {}]", signature, timestamp, nonce, echostr, e);
            return null;
        }
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
        try {
            log.info("接收微信公众号信息请求 openId: {}, 请求体:\n{}", openid, requestBody);
            // 消息转换
            QrCodeScanEventEntity scanEvent = XmlUtil.xmlToBean(requestBody, QrCodeScanEventEntity.class);
            String ticket = scanEvent.getTicket();
            String eventName = scanEvent.getEvent();

            String returnMessage;
            if (WechatConstant.NO_SUBSCRIBE_SCAN.equals(scanEvent.getEvent())) {
                // 未关注用户扫描二维码事件
                log.info("未关注用户正在扫描公众号二维码, 用户 openId: {}, Ticket: {}", openid, ticket);
                returnMessage = "";
            } else if (WechatConstant.SUBSCRIBED_SCAN.equals(scanEvent.getEvent())) {
                // 已关注用户扫描二维码事件
                log.info("已关注用户正在扫描公众号二维码, 用户 openId: {}, Ticket: {}", openid, ticket);

                if (openid.equals(cache.getIfPresent(ticket))) {
                    // 能够在缓存中找到该 TICKET 的openId
                    returnMessage = buildMessageTextEntity(openid, "您已经登录！");
                } else {
                    returnMessage = buildMessageTextEntity(openid, "登陆成功");
                    // 将 Ticket 和用户的 openId 存入缓存 ( Ticket -> openId )
                    cache.put(ticket, openid);
                }
            } else {
                // 未知消息推送
                log.info("未知消息推送, Event: {}, 用户 openId: {}, Ticket: {}", eventName, openid, ticket);
                returnMessage = "";
            }

            return returnMessage;
        } catch (Exception e) {
            log.error("接收微信公众号信息请求{}失败 {}", openid, requestBody, e);
            return "";
        }
    }

    private String buildMessageTextEntity(String openid, String content) {
        MessageTextEntity res = new MessageTextEntity();
        // 公众号分配的ID
        res.setFromUserName(wechatProperties.getOriginalid());
        res.setToUserName(openid);
        res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
        res.setMsgType("text");
        res.setContent(content);
        return XmlUtil.beanToXml(res);
    }
}
