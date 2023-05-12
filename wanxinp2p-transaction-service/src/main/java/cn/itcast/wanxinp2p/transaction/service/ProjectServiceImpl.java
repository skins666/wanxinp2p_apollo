package cn.itcast.wanxinp2p.transaction.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.common.domain.*;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.transaction.agent.ConsumerApiAgent;
import cn.itcast.wanxinp2p.transaction.common.utils.SecurityUtil;
import cn.itcast.wanxinp2p.transaction.entity.Project;
import cn.itcast.wanxinp2p.transaction.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import groovy.util.logging.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;


@Service
@Slf4j
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {


    @Resource
    private ConsumerApiAgent  consumerApiAgent;

    @Resource
    private ConfigService configService;

    @Override
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        //获取当前用户信息  经典错误问题 不能直接进行远程调用获取用户，因为远程调用没令牌，无法得到信息，需要这边直接security得到传给远程调用
        RestResponse<ConsumerDTO> response = consumerApiAgent.getByMobile(SecurityUtil.getUser().getMobile());
        ConsumerDTO consumerDTO = response.getResult();

        projectDTO.setConsumerId(consumerDTO.getId());
        projectDTO.setUserNo(consumerDTO.getUserNo());
        //生成标的编码
        projectDTO.setProjectNo(CodeNoUtil.getNo(CodePrefixCode.CODE_PROJECT_PREFIX));
        //标的状态修改
        projectDTO.setProjectStatus(ProjectCode.COLLECTING.getCode());
        //标的可用状态修改
        projectDTO.setStatus(StatusCode.STATUS_OUT.getCode());
        //设置创建时间
        projectDTO.setCreateDate(LocalDateTime.now());
        //设置还款方式
        projectDTO.setRepaymentWay(RepaymentWayCode.FIXED_REPAYMENT.getCode());
        //设置标的类型
        projectDTO.setType("NEW");
        //将DTO转换为实体类
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        //其他信息配置在阿波罗，我也不打算弄
        //设置年化利率
        project.setAnnualRate(configService.getCommissionAnnualRate());
        //设置标的名称
        project.setName(
                getProjectName(getGenderByIdCard(consumerDTO.getIdNumber()),
                        getProjectCountByConsumerId(consumerDTO.getId()),
                        consumerDTO));

        //保存标的信息
        save(project);

        //将ProjectDTO设置的信息更全
        projectDTO.setName(project.getName());
        projectDTO.setId(project.getId());

        return projectDTO;
    }

    @Override
    public String getGenderByIdCard(String idCard) {
        int idCardLength = idCard.length();
        if (idCardLength != 18) {
            // 身份证号码长度不合法
            throw new IllegalArgumentException("Invalid id card number length.");
        }
        char checkCode = idCard.charAt(17);
        if (!Character.isDigit(checkCode) && checkCode != 'X') {
            // 校验位不合法
            throw new IllegalArgumentException("Invalid id card number check code.");
        }
        int genderCode = Integer.parseInt(idCard.substring(16, 17));
        if (genderCode % 2 == 1) {
            return "男士";
        } else {
            return "女士";
        }
    }

    /**
     * 根据consumerId查询借款次数
     *
     * @param consumerId
     * @return
     */
    @Override
    public Integer getProjectCountByConsumerId(Long consumerId) {

        QueryWrapper<Project> wrapper = new QueryWrapper<>();
        wrapper.select("count(*) as projectCount")
                .eq("consumerId", consumerId);
        Map<String, Object> map = getMap(wrapper);
        if (map != null) {
            Object projectCount = map.get("projectCount");
            if (projectCount != null) {
                try {
                    return Integer.parseInt(projectCount.toString());
                } catch (NumberFormatException e) {
                    // 计数值转换异常
                    throw new IllegalStateException("Invalid project count value.");
                }
            }
        }

        return null;
    }

    @Override
    public String getProjectName(String sax, Integer count, ConsumerDTO consumer) {
        String projectName = String.format("%s%s的第%d次借款", consumer.getUsername(), sax, count);
        return projectName;
    }


}
