package xin.eason.service.wechat;

import xin.eason.domain.req.WechatReceiveReq;
import xin.eason.domain.req.WechatSignReq;

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
}
