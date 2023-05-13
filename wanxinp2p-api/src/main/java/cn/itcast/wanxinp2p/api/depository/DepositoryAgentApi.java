package cn.itcast.wanxinp2p.api.depository;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;

public interface DepositoryAgentApi {


    /**
     * 开通存管账户
     * @param consumerRequest
     * @return
     */
    RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest);

    /**
     * 向银行存管发送标的信息
     */
    RestResponse<String> createProject(ProjectDTO projectDTO);
}
