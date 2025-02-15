package xin.eason.domain.res;

import lombok.Data;

/**
 * 用于封装请求微信公众平台接口后返回的 <b>Access Token</b> 信息
 */
@Data
public class WechatRes {
    private String accessToken;

    private Integer expiresIn;

    private Integer errcode;

    private String errmsg;
}
