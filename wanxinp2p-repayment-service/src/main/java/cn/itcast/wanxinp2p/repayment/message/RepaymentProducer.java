package cn.itcast.wanxinp2p.repayment.message;

import cn.itcast.wanxinp2p.api.repayment.model.RepaymentRequest;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RepaymentProducer {

        @Resource
        private RocketMQTemplate rocketMQTemplate;
        public void confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("repaymentPlan", repaymentPlan);
                jsonObject.put("repaymentRequest", repaymentRequest);
                String s = jsonObject.toJSONString();
                //封装Message对象
                Message<String> build = MessageBuilder.withPayload(s).build();
                //发送消息
                rocketMQTemplate.sendMessageInTransaction("TP_CONFIRM_REPAYMENT", "CID_CONFIRM_REPAYMENT", build, null);


        }
}
