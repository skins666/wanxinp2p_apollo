package cn.itcast.wanxinp2p.repayment.service;

import cn.itcast.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;
import cn.itcast.wanxinp2p.repayment.model.EqualInterestRepayment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RepaymentPlanService extends IService<RepaymentPlan> {




    /**
     * 启动还款
     * @param projectWithTendersDTO
     * @return
     */

    String startRepayment(ProjectWithTendersDTO projectWithTendersDTO);



    public List<RepaymentPlan> saveRepaymentPlan(ProjectDTO projectDTO, EqualInterestRepayment equalInterestRepayment);
}
