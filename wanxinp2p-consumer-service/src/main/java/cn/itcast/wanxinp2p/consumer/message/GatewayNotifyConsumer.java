package cn.itcast.wanxinp2p.consumer.message;


import cn.itcast.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import cn.itcast.wanxinp2p.consumer.service.ConsumerService;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class GatewayNotifyConsumer {


    private ConsumerService consumerService;

    /**
     * Gateway通知消费者方法
     * 构造方法中实现消费者的初始化  构造方法中属性值直接放进，因为这个类与参数是在启动的时候就要加载的
     *
     */

    public GatewayNotifyConsumer(@Value("${rocketmq.consumer.group}" )String consumerGroup) throws MQClientException {
        // 创建一个消费者实例
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
        // 设置NameServer的地址
        defaultMQPushConsumer.setNamesrvAddr("127.0.0.1:9876");
        // 设置消费者的消费起始位置为最后一条消息的偏移量
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        // 订阅Topic为TP_GATEWAY_NOTIFY_AGENT，过滤消息标签为任意值的消息
        defaultMQPushConsumer.subscribe("TP_GATEWAY_NOTIFY_AGENT", "*");

        // 注册消息监听器
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //在这里实现功能
                try {
                    Message message = messages.get(0);
                    String topic = message.getTopic();
                    String tags = message.getTags();
                    String body = new String(message.getBody(), StandardCharsets.UTF_8);
                    //因为咱们接收到的是*  也就是说所有的标签消息都要进行接收，所以要进行判断
                    if(tags.equals("PERSONAL_REGISTER")){
                        //如果是个人开户的话，就进行个人开户的业务处理
                        //将消息体转换为DepositoryConsumerResponse对象
                        DepositoryConsumerResponse depositoryConsumerResponse = JSON.parseObject(body, DepositoryConsumerResponse.class);
                        //调用ConsumerService的modifyRequestStatus方法
                        consumerService.modifyRequestStatus(depositoryConsumerResponse);
                    }

                } catch (Exception e) {
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        // 启动消费者实例
        defaultMQPushConsumer.start();
    }
}
