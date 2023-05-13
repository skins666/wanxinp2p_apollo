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

    /**
     * 查询标的列表
     * @param projectQueryDTO
     * @param order
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @return
     */
    RestResponse<PageVO<ProjectDTO>> queryProjects(ProjectQueryDTO projectQueryDTO, String order, Integer pageNo, Integer pageSize, String sortBy);

    /**
     * 审核标的信息
     */
    RestResponse<String> confirmProject(Long id, String approvalStatus);
}
