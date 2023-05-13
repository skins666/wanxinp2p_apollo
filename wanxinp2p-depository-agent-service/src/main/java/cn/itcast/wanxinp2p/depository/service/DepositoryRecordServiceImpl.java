package cn.itcast.wanxinp2p.depository.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.DepositoryBaseResponse;
import cn.itcast.wanxinp2p.api.depository.model.DepositoryResponseDTO;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.api.depository.model.ProjectRequestDataDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.StatusCode;
import cn.itcast.wanxinp2p.common.util.EncryptUtil;
import cn.itcast.wanxinp2p.common.util.RSAUtil;
import cn.itcast.wanxinp2p.depository.common.constant.DepositoryErrorCode;
import cn.itcast.wanxinp2p.depository.common.constant.DepositoryRequestTypeCode;
import cn.itcast.wanxinp2p.depository.entity.DepositoryRecord;
import cn.itcast.wanxinp2p.depository.mapper.DepositoryRecordMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class DepositoryRecordServiceImpl extends ServiceImpl<DepositoryRecordMapper, DepositoryRecord> implements DepositoryRecordService {


    @Resource
    private OkHttpService okHttpService;
    @Resource
    private ConfigService configService;

    @Override
    public GatewayRequest createConsumer(ConsumerRequest consumerRequest) {
        //保存交易记录
        saveDepositoryRecord(consumerRequest);

        String reqData = JSON.toJSONString(consumerRequest);
        //签名
        String sign = RSAUtil.sign(reqData, RSAUtil.p2p_privateKey, "utf-8");

        GatewayRequest gatewayRequest = new GatewayRequest();
        gatewayRequest.setServiceName("PERSONAL_REGISTER");
        gatewayRequest.setPlatformNo(configService.getP2pCode());
        gatewayRequest.setReqData(EncryptUtil.encodeURL(EncryptUtil
                .encodeUTF8StringBase64(reqData)));
        gatewayRequest.setSignature(EncryptUtil.encodeURL(sign));

        gatewayRequest.setDepositoryUrl(configService.getDepositoryUrl() +
                "/gateway");
        return gatewayRequest;
    }

    @Override
    public Boolean modifyRequestStatus(String requestNum, Integer status) {
        if (requestNum == null || status == null) {
            return false;
        }
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        //requestNum转换成Long类型
        Long requestNumLong = Long.valueOf(requestNum);

        depositoryRecord.setId(requestNumLong);
        depositoryRecord.setRequestStatus(status);
        depositoryRecord.setConfirmDate(LocalDateTime.now());
        boolean b = updateById(depositoryRecord);


        return b;
    }

    /**
     * @param projectDTO
     * @return
     */
    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> createProject(ProjectDTO projectDTO) {
        //1.保存交易记录
        DepositoryRecord depositoryRecord = saveDepositoryRecord(projectDTO.getRequestNo(), DepositoryRequestTypeCode.CREATE.getCode(), "project", projectDTO.getId());


        //2.签名数据
        String jsonString = JSON.toJSONString(depositoryRecord);
        //转换base64
        String reqData = EncryptUtil.encodeUTF8StringBase64(jsonString);
        //3.向银行发送数据
        //url地址，发送哪些数据
        String url = configService.getDepositoryUrl();
        url = url + "/service";

        //4.根据结果修改状态并返回结果
        return sendHttpGet("CREATE_PROJECT", url, reqData, depositoryRecord);
    }


    private DepositoryResponseDTO<DepositoryBaseResponse> sendHttpGet(
            String serviceName, String url, String reqData,
            DepositoryRecord depositoryRecord) {
// 银行存管系统接收的4大参数: serviceName, platformNo, reqData, signature
// signature会在okHttp拦截器(SignatureInterceptor)中处理
// 平台编号
        String platformNo = configService.getP2pCode();
// redData签名
// 发送请求, 获取结果, 如果检验签名失败, 拦截器会在结果中放入: "signature", "false"
        String responseBody = okHttpService
                .doSyncGet(url + "?serviceName=" + serviceName + "&platformNo=" +
                        platformNo + "&reqData=" + reqData);
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = JSON
                .parseObject(responseBody,
                        new TypeReference<DepositoryResponseDTO<DepositoryBaseResponse>>() {
                        });
// 响应后, 根据结果更新数据库( 进行签名判断 )
// 判断签名(signature)是为 false, 如果是说明验签失败!
        if (!"false".equals(depositoryResponse.getSignature())) {
// 成功 - 设置数据同步状态
            depositoryRecord.setRequestStatus(StatusCode.STATUS_IN.getCode());
// 设置消息确认时间
            depositoryRecord.setConfirmDate(LocalDateTime.now());
// 更新数据库
            updateById(depositoryRecord);
        } else {
// 失败 - 设置数据同步状态
            depositoryRecord.setRequestStatus(StatusCode.STATUS_FAIL.getCode());
// 设置消息确认时间
            depositoryRecord.setConfirmDate(LocalDateTime.now());
// 更新数据库
            updateById(depositoryRecord);
// 抛业务异常
            throw new BusinessException(DepositoryErrorCode.E_160101);
        }
        return depositoryResponse;
    }


    @Override
    public DepositoryRecord saveDepositoryRecord(String requestNo, String requestType, String objectType, Long objectId) {
        //设置请求流水号
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        depositoryRecord.setRequestNo(requestNo);
        depositoryRecord.setRequestType(requestType);
        depositoryRecord.setObjectType(objectType);
        depositoryRecord.setObjectId(objectId);
        depositoryRecord.setCreateDate(LocalDateTime.now());
        depositoryRecord.setRequestStatus(StatusCode.STATUS_OUT.getCode());
        save(depositoryRecord);

        return depositoryRecord;
    }

    @Override
    public ProjectRequestDataDTO converProjectDTOToProjectRequestDataDTO(ProjectDTO projectDTO, String requestNo) {

        if (Objects.isNull(projectDTO) || Objects.isNull(requestNo)) {
            return null;
        }
        ProjectRequestDataDTO projectRequestDataDTO = new ProjectRequestDataDTO();
        BeanUtils.copyProperties(projectDTO, projectRequestDataDTO);
        projectRequestDataDTO.setRequestNo(requestNo);
        return projectRequestDataDTO;
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
