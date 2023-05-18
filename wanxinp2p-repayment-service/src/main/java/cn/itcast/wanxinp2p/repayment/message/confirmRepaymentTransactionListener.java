package cn.itcast.wanxinp2p.repayment.message;

import cn.itcast.wanxinp2p.api.repayment.model.RepaymentRequest;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;
import cn.itcast.wanxinp2p.repayment.mapper.RepaymentPlanMapper;
import cn.itcast.wanxinp2p.repayment.service.RepaymentService;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

//事务消息监听类
@Component
@RocketMQTransactionListener(txProducerGroup = "TP_CONFIRM_REPAYMENT")
public class confirmRepaymentTransactionListener implements RocketMQLocalTransactionListener {

    @Resource
    private RepaymentService repaymentService;

    private RepaymentPlanMapper repaymentPlanMapper;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        //1.解析消息
        JSONObject jsonObject = JSONObject.parseObject(new String((byte[])message.getPayload()));

        RepaymentPlan repaymentPlan = jsonObject.parseObject("repaymentPlan", RepaymentPlan.class);
        RepaymentRequest repaymentRequest = jsonObject.parseObject("repaymentRequest", RepaymentRequest.class);

        //2.执行本地事务
        Boolean result = repaymentService.confirmRepayment(repaymentPlan, repaymentRequest);
        //3.返回事务状态

        if (result){
            //3.返回事务状态
            return RocketMQLocalTransactionState.COMMIT;
        }else {
            //3.返回事务状态
            return RocketMQLocalTransactionState.ROLLBACK;
        }


    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        //1.解析消息
        JSONObject jsonObject = JSONObject.parseObject(new String((byte[])message.getPayload()));
        RepaymentPlan repaymentPlan = jsonObject.parseObject("repaymentPlan", RepaymentPlan.class);

        //2.检查本地事务
        RepaymentPlan repaymentPlanNew = repaymentPlanMapper.selectById(repaymentPlan.getId()) ;

        //3.返回事务状态
        if (repaymentPlanNew.getRepaymentStatus().equals("1")){
            //3.返回事务状态
            return RocketMQLocalTransactionState.COMMIT;
        }else {
            //3.返回事务状态
            return RocketMQLocalTransactionState.ROLLBACK;
        }



    }
}
