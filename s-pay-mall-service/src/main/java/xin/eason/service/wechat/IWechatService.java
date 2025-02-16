package xin.eason.service.wechat;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import xin.eason.domain.req.WechatQrCodeReq;
import xin.eason.domain.req.WechatTemplateReq;
import xin.eason.domain.res.WechatQrCodeRes;
import xin.eason.domain.res.WechatRes;
import xin.eason.domain.res.WechatTemplateRes;

/**
 * 用于向微信公众平台发送请求获取 <b>Access Token</b> 和 带 <b>Ticket</b> 的二维码
 */
public interface IWechatService {

    /**
     * 发送 <b>GET</b> 请求获取 <b>Access Token</b>
     * @param request 向公众平台发送的请求参数的封装
     * @return 返回带 <b>Access Token</b> 和过期时间的对象
     */
    @GET("/cgi-bin/token")
    Call<WechatRes> getAccessToken(
            @Query("grant_type") String grantType,
            @Query("appid") String appid,
            @Query("secret") String secret
    );

    /**
     * 发送 <b>POST</b> 请求获取 <b>ticket</b>
     * @param request 向公众平台发送获取二维码的请求参数的封装
     * @return 返回带 <b>ticket</b> 的信息
     */
    @POST("/cgi-bin/qrcode/create")
    Call<WechatQrCodeRes> getQrCode(@Body WechatQrCodeReq request, @Query("access_token") String accessToken);

    /**
     * 发送 <b>POST</b> 请求以向指定用户发送模板信息
     * @param wechatTemplateReq 向公众平台发送的模板信息数据
     * @return 返回发送模板信息状态的 JSON 数据体
     */
    @POST("/cgi-bin/message/template/send")
    Call<WechatTemplateRes> sendTemplate(@Body WechatTemplateReq wechatTemplateReq, @Query("access_token") String accessToken);
}
