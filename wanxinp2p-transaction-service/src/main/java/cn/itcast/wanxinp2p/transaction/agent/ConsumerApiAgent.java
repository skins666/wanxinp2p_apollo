package cn.itcast.wanxinp2p.transaction.agent;


import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "consumer-service")
public interface ConsumerApiAgent {


    /**
     * 获取当前登录用户信息
     * @return
     */
    @GetMapping("/l/currConsumer/{mobile}")
    RestResponse<ConsumerDTO> getByMobile(@PathVariable String mobile);

}
