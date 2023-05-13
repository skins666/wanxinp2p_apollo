package cn.itcast.wanxinp2p.depository.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.DepositoryBaseResponse;
import cn.itcast.wanxinp2p.api.depository.model.DepositoryResponseDTO;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.api.depository.model.ProjectRequestDataDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.depository.entity.DepositoryRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface DepositoryRecordService extends IService<DepositoryRecord> {

      GatewayRequest createConsumer(ConsumerRequest consumerRequest);


      Boolean modifyRequestStatus(String requestNum,Integer status);

      /**
       * 保存标的
       * @param projectDTO
       * @return
       */
      DepositoryResponseDTO<DepositoryBaseResponse> createProject(ProjectDTO projectDTO);

     DepositoryRecord saveDepositoryRecord(String requestNo, String requestType, String objectType, Long objectNo);


     ProjectRequestDataDTO converProjectDTOToProjectRequestDataDTO(ProjectDTO projectDTO, String requestNo);
}
