package xin.eason.domain.res;

import lombok.Data;

@Data
public class WechatTemplateRes {
    private String errmsg;
    private Integer errcode;
    private Long msgid;
}
