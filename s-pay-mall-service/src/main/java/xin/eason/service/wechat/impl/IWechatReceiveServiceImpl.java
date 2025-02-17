package xin.eason.service.wechat.impl;

import com.google.common.cache.Cache;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import xin.eason.common.cons.WechatConstant;
import xin.eason.common.properties.WechatProperties;
import xin.eason.domain.po.MessageTextEntity;
import xin.eason.domain.po.QrCodeScanEventEntity;
import xin.eason.common.util.SignatureUtil;
import xin.eason.common.util.XmlUtil;
import xin.eason.domain.po.SubscribeEventEntity;
import xin.eason.domain.po.TemplateSendEventEntity;
import xin.eason.domain.req.WechatReceiveReq;
import xin.eason.domain.req.WechatSignReq;
import xin.eason.domain.req.WechatTemplateReq;
import xin.eason.domain.res.WechatTemplateRes;
import xin.eason.service.wechat.IWechatReceiveService;
import xin.eason.service.wechat.IWechatService;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class IWechatReceiveServiceImpl implements IWechatReceiveService {

    private final WechatProperties wechatProperties;

    private final Cache<String, String> cache;

    private final IWechatService iWechatService;

    private String templateId = "EZXZgdMPpQUabzSAU5tj8gc05zoQ9e-BToUdU02WhiY";

    private String turnToUrl = "https://www.baidu.com";

    /**
     * 验证消息来自微信公众平台接口
     *
     * @param wechatSignReq 微信公众平台发来的 <b>验签</b> 请求体对象
     * @return 验签成功则返回 echostr
     */
    @Override
    public String validate(WechatSignReq wechatSignReq) {
        String signature = wechatSignReq.getSignature();
        String timestamp = wechatSignReq.getTimestamp();
        String nonce = wechatSignReq.getNonce();
        String echostr = wechatSignReq.getEchostr();
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
     * 接收微信公众平台推送的消息
     *
     * @param wechatReceiveReq 微信公众平台发来的 <b>消息推送</b>
     * @return 返回要推送给特定 <b>openId</b> 用户的信息内容
     */
    @Override
    public String receiveScanEvent(WechatReceiveReq wechatReceiveReq) {
        String requestBody = wechatReceiveReq.getRequestBody();
        String signature = wechatReceiveReq.getSignature();
        String timestamp = wechatReceiveReq.getTimestamp();
        String nonce = wechatReceiveReq.getNonce();
        String openid = wechatReceiveReq.getOpenid();
        String encType = wechatReceiveReq.getEncType();
        String msgSignature = wechatReceiveReq.getMsgSignature();

        String returnMsg = "";

        log.info("接收微信公众号信息请求 openId: {}, 请求体:\n{}", openid, requestBody);
        // 尝试解析为 SCAN 事件
        try {
            returnMsg = scanEventHandle(openid, requestBody);
            return returnMsg;
        } catch (AbstractReflectionConverter.UnknownFieldException e) {
            log.error("当前推送的消息不是二维码扫描的 SCAN 消息");
        } catch (Exception e) {
            log.error("接收微信公众号信息请求 openId: {}, 请求体:\n{}", openid, requestBody, e);
            return "";
        }

        // 尝试解析为 SUBSCRIBE 事件
        try {
            returnMsg = subscribeEventHandle(openid, requestBody);
            return returnMsg;
        } catch (AbstractReflectionConverter.UnknownFieldException e) {
            log.error("当前推送的消息不是关注公众号的 SUBSCRIBE 消息");
        } catch (Exception e) {
            log.error("接收微信公众号信息请求 openId: {}, 请求体:\n{}", openid, requestBody, e);
            return "";
        }

        // 尝试解析为 TEMPLATESENDJOBFINISH (模板消息发送成功) 事件
        try {
            returnMsg = templateSendEventHandle(openid, requestBody);
            return returnMsg;
        } catch (AbstractReflectionConverter.UnknownFieldException e) {
            log.error("当前推送的消息不是模板消息发送成功的 TEMPLATESENDJOBFINISH 消息");
        } catch (Exception e) {
            log.error("接收微信公众号信息请求 openId: {}, 请求体:\n{}", openid, requestBody, e);
            return "";
        }
        return returnMsg;
    }

    /**
     * 处理微信公众平台推送的 <b>SCAN</b> 消息
     *
     * @param openid      扫码用户的 <b>openId</b>
     * @param requestBody XML形式的请求体
     * @return 回复给公众平台的信息
     */
    private String scanEventHandle(String openid, String requestBody) {
        // 消息转换
        QrCodeScanEventEntity scanEvent = XmlUtil.xmlToBean(requestBody, QrCodeScanEventEntity.class);
        String ticket = scanEvent.getTicket();
        String eventName = scanEvent.getEvent();

        String returnMessage;
        if (WechatConstant.SCAN.equals(scanEvent.getEvent()) && openid.equals(cache.getIfPresent(ticket))) {
            // 已关注用户扫描二维码事件
            log.info("已关注用户正在扫描公众号二维码, 用户 openId: {}, Ticket: {}", openid, ticket);

            // 能够在缓存中找到该 TICKET 的openId
            log.info("用户: {} 已经登录!", openid);
            returnMessage = buildMessageTextEntity(openid, "您已经登录！");

            // 发送模板信息
            sendLoginTemplate(templateId, openid, turnToUrl, cache.getIfPresent("access_token"));
            return returnMessage;
        }

        if (WechatConstant.SCAN.equals(scanEvent.getEvent()) && !openid.equals(cache.getIfPresent(ticket))) {
            // 已关注用户扫描二维码事件
            log.info("已关注用户正在扫描公众号二维码, 用户 openId: {}, Ticket: {}", openid, ticket);
            log.info("用户: {} 登陆成功!", openid);
            returnMessage = buildMessageTextEntity(openid, "登陆成功");
            // 发送模板信息
            sendLoginTemplate(templateId, openid, turnToUrl, cache.getIfPresent("access_token"));

            // 将 Ticket 和用户的 openId 存入缓存 ( Ticket -> openId )
            cache.put(ticket, openid);
            return returnMessage;
        } else if (WechatConstant.SUBSCRIBE.equals(scanEvent.getEvent())) {
            // 未关注的用户, 通过扫描二维码关注公众号的事件
            log.info("未关注用户正在扫描公众号二维码, 用户 openId: {}, Ticket: {}", openid, ticket);
            log.info("未关注用户: {} 登陆成功! 并且已关注!", openid);
            returnMessage = buildMessageTextEntity(openid, "登陆成功");
            // 发送模板信息
            sendLoginTemplate(templateId, openid, turnToUrl, cache.getIfPresent("access_token"));

            // 将 Ticket 和用户的 openId 存入缓存 ( Ticket -> openId )
            cache.put(ticket, openid);
            return returnMessage;
        }

        // 未知消息推送
        log.info("未知消息推送, Event: {}, 用户 openId: {}, Ticket: {}", eventName, openid, ticket);
        returnMessage = "";

        return returnMessage;

    }

    /**
     * 处理微信公众平台推送的 <b>subscribe</b> 消息
     *
     * @param openid      扫码用户的 <b>openId</b>
     * @param requestBody XML形式的请求体
     * @return 回复给公众平台的信息
     */
    private String subscribeEventHandle(String openid, String requestBody) {
        // 消息转换
        SubscribeEventEntity subscribeScanEvent = XmlUtil.xmlToBean(requestBody, SubscribeEventEntity.class);
        String eventName = subscribeScanEvent.getEvent();

        String returnMessage;
        if (WechatConstant.SUBSCRIBE.equals(eventName)) {
            // 当前为关注消息推送
            log.info("用户已关注公众号 openId: {}", openid);
            returnMessage = buildMessageTextEntity(openid, "感谢你的关注！");
            return returnMessage;
        }
        // 未知消息推送
        log.info("未知消息推送, Event: {}, 用户 openId: {}", eventName, openid);
        returnMessage = "";
        return returnMessage;
    }

    /**
     * 处理微信公众平台推送的 <b>TEMPLATESENDJOBFINISH</b> 信息
     *
     * @param openid      扫码用户的 <b>openId</b>
     * @param requestBody XML形式的请求体
     * @return 回复给公众平台的信息
     */
    public String templateSendEventHandle(String openid, String requestBody) {
        // 消息转换
        TemplateSendEventEntity templateSendEvent = XmlUtil.xmlToBean(requestBody, TemplateSendEventEntity.class);
        String eventName = templateSendEvent.getEvent();

        String returnMessage = "";
        if (WechatConstant.TEMPLATE_SEND_JOB_FINISH.equals(eventName)) {
            // 当前为模板信息发送完成消息推送
            log.info("模板信息{} msgId: {}, openId: {}",
                    "success".equals(templateSendEvent.getStatus()) ? "成功发送" : "发送失败",
                    templateSendEvent.getMsgId(),
                    openid);
            return returnMessage;
        }
        // 未知消息推送
        log.info("未知消息推送, Event: {}, 用户 openId: {}", eventName, openid);
        return returnMessage;
    }

    /**
     * 构建回复给用户的信息实体
     *
     * @param openid  需要发向的用户的 <b>openId</b>
     * @param content 需要发送的消息内容
     * @return 构建好的 <b>XML</b> 消息实体
     */
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

    /**
     * 向指定用户发送模板信息
     *
     * @param templateId  需要发送的模板信息的 <b>模板 ID</b>
     * @param openid      需要发送的用户的 <b>openId</b>
     * @param turnToUrl   发送给用户的模板信息点击后跳转的 <b>URL</b>
     * @param accessToken 用于向微信公众平台调用 API 的 <b>Access Token</b>
     * @return 如果发送成功则返回模板信息的 <b>ID</b>
     */
    private Long sendLoginTemplate(String templateId, String openid, String turnToUrl, String accessToken) {
        // 发送模板信息
        WechatTemplateReq request = new WechatTemplateReq();
        request.setTemplateId(templateId);
        request.setTouser(openid);
        request.setUrl(turnToUrl);
        request.put("openId", openid);
        // 向公众平台发送 http 请求
        try {
            Response<WechatTemplateRes> response = iWechatService.sendTemplate(request, accessToken).execute();
            String errmsg = response.body().getErrmsg();
            if (!"ok".equals(errmsg)) {
                log.error("发送模板信息失败! 错误代码: {}, 错误信息: {}", response.body().getErrcode(), errmsg);
                throw new RuntimeException(errmsg);
            }
            Long msgid = response.body().getMsgid();
            log.info("发送模板信息成功, msgId: {}", msgid);
            return msgid;
        } catch (IOException e) {
            log.error("发送模板信息请求过程出错!");
            throw new RuntimeException(e);
        }
    }
}
