package xin.eason.service;

import xin.eason.domain.vo.UserInfoVO;

public interface LoginService {

    /**
     * 获取 <b>Ticket</b> 业务流程
     *
     * @return 返回获取到的 <b>Ticket</b>
     */
    String getTicket();

    /**
     * 检查登录状态
     * @param ticket 用于生成二维码的 <b>Ticket</b>
     * @return 用户信息的视图对象
     */
    UserInfoVO checkLogin(String ticket);
}
