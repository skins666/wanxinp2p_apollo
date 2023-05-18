package cn.itcast.wanxinp2p.repayment.agent;

import cn.itcast.wanxinp2p.api.depository.model.UserAutoPreTransactionRequest;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "depository-agent-service")
public interface DepositoryOpenFeignAgent {

    /**
     * 用户自动还款预处理
     * @param request
     * @return
     */
    @PostMapping(value = "/depository-agent/l/auto-pre-transaction")
    RestResponse<String> userAutoPreTransaction(UserAutoPreTransactionRequest request);
}
