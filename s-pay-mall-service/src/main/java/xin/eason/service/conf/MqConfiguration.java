package xin.eason.service.conf;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MqConfiguration {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 初始化 rabbitTemplate 对象, 为其添加 CallBack 函数
     */
    @PostConstruct
    public void init(){
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                log.debug("已收到 RabbitMQ 的 CallBack 信息");
                log.debug("信息内容: {}", returnedMessage.getMessage());
                log.debug("信息交换机: {}", returnedMessage.getExchange());
                log.debug("信息 RoutingKey： {}", returnedMessage.getRoutingKey());
                log.debug("信息代码: {}", returnedMessage.getReplyCode());
                log.debug("信息回复内容: {}", returnedMessage.getReplyText());
            }
        });
    }
}
