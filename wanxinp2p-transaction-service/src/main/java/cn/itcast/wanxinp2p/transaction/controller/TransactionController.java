package cn.itcast.wanxinp2p.transaction.controller;

import cn.itcast.wanxinp2p.api.transaction.TransactionApi;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.transaction.service.ProjectService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;

public class TransactionController implements TransactionApi {

    @Resource
    private ProjectService projectService;

    @ApiOperation("创建标的")
    @ApiImplicitParam(name = "project", value = "标的信息", required = true,
            dataType = "ProjectDTO", paramType = "body")
    @PostMapping(value = "/my/projects")
    @Override
    public RestResponse<ProjectDTO> createProject(ProjectDTO project) {
        return RestResponse.success(projectService.createProject(project));
    }
}
