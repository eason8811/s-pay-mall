package xin.eason.service.wechat;

import xin.eason.domain.req.WechatReceiveReq;
import xin.eason.domain.req.WechatSignReq;

import java.util.Map;

public interface IWechatReceiveService {

    /**
     * 验证消息来自微信公众平台接口
     * @param wechatSignReq 微信公众平台发来的 <b>验签</b> 请求体对象
     * @return 验签成功则返回 echostr
     */
    String validate(WechatSignReq wechatSignReq);

    /**
     * 接收微信公众平台推送的消息
     * @param wechatReceiveReq 微信公众平台发来的 <b>消息推送</b>
     * @return 返回要推送给特定 <b>openId</b> 用户的信息内容
     */
    String receiveScanEvent(WechatReceiveReq wechatReceiveReq);

    /**
     * 向指定用户发送模板信息
     *
     * @param templateId  需要发送的模板信息的 <b>模板 ID</b>
     * @param openid      需要发送的用户的 <b>openId</b>
     * @param turnToUrl   发送给用户的模板信息点击后跳转的 <b>URL</b>
     * @param accessToken 用于向微信公众平台调用 API 的 <b>Access Token</b>
     * @param dataMap 用于存储数据的键值对
     * @return 如果发送成功则返回模板信息的 <b>ID</b>
     */
    Long sendTemplate(String templateId, String openid, String turnToUrl, String accessToken, Map<String, String> dataMap);
}
