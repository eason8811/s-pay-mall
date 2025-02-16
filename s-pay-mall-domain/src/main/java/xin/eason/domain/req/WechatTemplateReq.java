package xin.eason.domain.req;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class WechatTemplateReq {
    private String touser;

    private String templateId;

    private String url;

    private MiniProgram miniprogram;

    private String clientMsgId;

    private Map<String, Map<String, String>> data;

    @Data
    public static class MiniProgram{
        private String appid;

        private String pagepath;
    }

    public void put(String key, String value) {
        if (data == null)
            data = new HashMap<>();
        put(data, key, value);
    }

    public static void put(Map<String, Map<String, String>> data, String key, String value) {
        HashMap<String, String> map = new HashMap<>();
        map.put("value", value);
        data.put(key, map);
    }
}
