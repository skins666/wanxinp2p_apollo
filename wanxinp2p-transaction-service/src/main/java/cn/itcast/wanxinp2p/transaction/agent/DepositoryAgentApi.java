package cn.itcast.wanxinp2p.transaction.agent;


import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "depository-agent-service")
public interface DepositoryAgentApi {


    @PostMapping(value = "/depository-agent/l/creatProject")
    public RestResponse<String> createProject(ProjectDTO projectDTO);
}
