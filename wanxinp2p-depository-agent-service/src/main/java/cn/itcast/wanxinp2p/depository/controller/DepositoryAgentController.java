package cn.itcast.wanxinp2p.depository.controller;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.DepositoryAgentApi;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.depository.service.DepositoryRecordService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
@RestController
public class DepositoryAgentController implements DepositoryAgentApi {


    /**
     * 存管代理服务
     * @param consumerRequest
     * @return
     */

    @Resource
    private DepositoryRecordService depositoryRecordService;

    @ApiOperation("开通存管账户")
    @ApiImplicitParam(name = "consumerRequest", value = "开户信息", required = true, dataType = "ConsumerRequest", paramType = "body")
    @Override
    @PostMapping(value = "/l/consumers")
    public RestResponse<GatewayRequest> createConsumer(@RequestBody ConsumerRequest consumerRequest) {
        return RestResponse.success(depositoryRecordService.createConsumer(consumerRequest));
    }
}
