package cn.itcast.wanxinp2p.transaction.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectQueryDTO;
import cn.itcast.wanxinp2p.common.domain.PageVO;
import cn.itcast.wanxinp2p.transaction.entity.Project;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestBody;


public interface ProjectService extends IService<Project> {


    /**
     * 创建标的
     * @param projectDTO
     * @return
     */
    ProjectDTO createProject(ProjectDTO projectDTO);

    /**
     * 根据身份证号查询性别
     */
    String getGenderByIdCard(String idCard);

    /**
     * 根据consumerId查询借款次数
     */
    Integer getProjectCountByConsumerId(Long consumerId);


    /**
     * 根据借款次数加性别合成标的名称
     */
    String getProjectName(String sax, Integer count, ConsumerDTO consumer);



    PageVO<ProjectDTO> queryProjects(@RequestBody ProjectQueryDTO projectQueryDTO, String order, Integer pageNo, Integer pageSize, String sortBy) ;
}
