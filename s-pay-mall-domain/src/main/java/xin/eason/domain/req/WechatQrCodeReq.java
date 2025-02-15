package xin.eason.domain.req;

import lombok.Builder;
import lombok.Data;

/**
 * 封装要向微信公众平台发送的获取永久二维码的信息
 */
@Data
@Builder
public class WechatQrCodeReq {
    private String actionName;

    private ActionInfo actionInfo;

    private Long expireSeconds;

    /**
     * example: "action_info": {"scene": {"scene_id": 123}}
     */
    @Data
    @Builder
    public static class ActionInfo{
        private Scene scene;
    }

    /**
     * example: "scene": {"scene_id": 123}
     */
    @Data
    @Builder
    public static class Scene{
        private String scene_str;
    }
}
