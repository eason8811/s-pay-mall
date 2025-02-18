package xin.eason.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xin.eason.domain.req.ShopCartReq;
import xin.eason.domain.res.PayOrderRes;
import xin.eason.domain.res.Result;
import xin.eason.service.PayService;

@Slf4j
@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    @PostMapping("/pay")
    public Result<PayOrderRes> pay(@RequestBody ShopCartReq shopCartReq){
        PayOrderRes order = payService.createOrder(shopCartReq);
        return Result.success(order);
    }
}
