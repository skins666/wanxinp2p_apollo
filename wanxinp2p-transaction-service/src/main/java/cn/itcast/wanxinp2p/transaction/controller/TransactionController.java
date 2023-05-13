package cn.itcast.wanxinp2p.transaction.controller;

import cn.itcast.wanxinp2p.api.transaction.TransactionApi;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectQueryDTO;
import cn.itcast.wanxinp2p.common.domain.PageVO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.transaction.service.ProjectService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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



    @ApiOperation("查询标的列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectQueryDTO", value = "标的信息", required = true,
                    dataType = "ProjectQueryDTO", paramType = "body"),
            @ApiImplicitParam(name = "order", value = "排序方式", required = true,
                    dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true,
                    dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页显示条数", required = true,
                    dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "sortBy", value = "排序字段", required = true,
                    dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/projects/q")
    @Override
    public RestResponse<PageVO<ProjectDTO>> queryProjects(@RequestBody ProjectQueryDTO projectQueryDTO, @RequestParam String order, @RequestParam Integer pageNo, @RequestParam Integer pageSize,@RequestParam String sortBy) {

             //执行查询
        PageVO<ProjectDTO> pageVO = projectService.queryProjects(projectQueryDTO, order, pageNo, pageSize, sortBy);

        return RestResponse.success(pageVO);
    }


    /**
     * 审核标的信息
     * @param id
     * @param approvalStatus
     * @return
     */
    @ApiOperation("审核标的信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "标的id", required = true,
                    dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "approvalStatus", value = "审核状态", required = true,
                    dataType = "String", paramType = "path")
    })
    @PostMapping(value = "/projects/{id}/audit/{approvalStatus}")
    @Override
    public RestResponse<String> confirmProject(@PathVariable Long id, @PathVariable String approvalStatus) {

        return null;
    }
}
