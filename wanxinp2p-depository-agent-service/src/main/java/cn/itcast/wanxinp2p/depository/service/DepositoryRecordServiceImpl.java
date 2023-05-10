package cn.itcast.wanxinp2p.depository.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.common.domain.StatusCode;
import cn.itcast.wanxinp2p.common.util.EncryptUtil;
import cn.itcast.wanxinp2p.common.util.RSAUtil;
import cn.itcast.wanxinp2p.depository.common.constant.DepositoryRequestTypeCode;
import cn.itcast.wanxinp2p.depository.entity.DepositoryRecord;
import cn.itcast.wanxinp2p.depository.mapper.DepositoryRecordMapper;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class DepositoryRecordServiceImpl extends ServiceImpl<DepositoryRecordMapper, DepositoryRecord> implements DepositoryRecordService{


    @Resource
    private ConfigService configService;
    @Override
    public GatewayRequest  createConsumer(ConsumerRequest consumerRequest) {
        //保存交易记录
        saveDepositoryRecord(consumerRequest);

        String reqData = JSON.toJSONString(consumerRequest);
        //签名
        String sign = RSAUtil.sign(reqData, RSAUtil.p2p_privateKey, "utf-8");

        GatewayRequest gatewayRequest=new GatewayRequest();
        gatewayRequest.setServiceName("PERSONAL_REGISTER");
        gatewayRequest.setPlatformNo(configService.getP2pCode());
        gatewayRequest.setReqData(EncryptUtil.encodeURL(EncryptUtil
                .encodeUTF8StringBase64(reqData)));
        gatewayRequest.setSignature(EncryptUtil.encodeURL(sign));

        gatewayRequest.setDepositoryUrl(configService.getDepositoryUrl() +
                "/gateway");
        return gatewayRequest;
    }

    private void saveDepositoryRecord(ConsumerRequest consumerRequest) {
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        depositoryRecord.setRequestNo(consumerRequest.getRequestNo());
        depositoryRecord.setRequestType(DepositoryRequestTypeCode.CONSUMER_CREATE.getCode());
        depositoryRecord.setObjectType("consumer");
        depositoryRecord.setObjectId(consumerRequest.getId());
        depositoryRecord.setCreateDate(LocalDateTime.now());
        depositoryRecord.setRequestStatus(StatusCode.STATUS_OUT.getCode());

        save(depositoryRecord);

    }
}
