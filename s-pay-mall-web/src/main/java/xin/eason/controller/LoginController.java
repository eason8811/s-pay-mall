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

    @GetMapping("/get_ticket")
    public Result<String> getTicket(){
        String ticket = loginService.getTicket();
        return Result.success(ticket);
    }
}
