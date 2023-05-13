package cn.itcast.wanxinp2p.api.transaction;

import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectQueryDTO;
import cn.itcast.wanxinp2p.common.domain.PageVO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;

public interface TransactionApi {


    /**
     * 创建标的(借款人发标)
     * @param project
     * @return
     */
    RestResponse<ProjectDTO> createProject(ProjectDTO project) ;


    RestResponse<PageVO<ProjectDTO>> queryProjects(ProjectQueryDTO projectQueryDTO, String order, Integer pageNo, Integer pageSize, String sortBy);
}
