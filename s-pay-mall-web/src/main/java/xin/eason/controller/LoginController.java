package xin.eason.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.eason.domain.res.Result;
import xin.eason.domain.res.WechatQrCodeRes;
import xin.eason.service.LoginService;

@Slf4j
@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * 用户获取 <b>Ticket</b> 的接口
     * @return <b>Ticket</b>
     */
    @GetMapping("/get_ticket")
    public Result<String> getTicket(){
        String ticket = loginService.getTicket();
        return Result.success(ticket);
    }

    /**
     * 前端轮训登录状态接口
     * @param ticket 之前获取到的用于生成二维码的 <b>Ticket</b>
     * @return 登录状态
     */
    @GetMapping("/check_login")
    public Result<String> checkLogin(String ticket){
        String loginStatus = loginService.checkLogin(ticket);
        return Result.success(loginStatus);
    }
}
