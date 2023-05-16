package cn.itcast.wanxinp2p.repayment.service;

import cn.itcast.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.TenderDTO;
import cn.itcast.wanxinp2p.common.domain.DepositoryReturnCode;
import cn.itcast.wanxinp2p.common.domain.RepaymentWayCode;
import cn.itcast.wanxinp2p.common.util.DateUtil;
import cn.itcast.wanxinp2p.repayment.entity.ReceivablePlan;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;
import cn.itcast.wanxinp2p.repayment.mapper.RepaymentPlanMapper;
import cn.itcast.wanxinp2p.repayment.model.EqualInterestRepayment;
import cn.itcast.wanxinp2p.repayment.util.RepaymentUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RepaymentPlanServiceImpl extends ServiceImpl<RepaymentPlanMapper, RepaymentPlan> implements RepaymentPlanService {

    @Resource
    private ReceivablePlanService receivablePlanService;


    @Override
    public String startRepayment(ProjectWithTendersDTO projectWithTendersDTO) {
        //生成借款人还款计划
        //1.1 获取标的信息
        ProjectDTO project = projectWithTendersDTO.getProject();

        //1.2 获取投标信息
        List<TenderDTO> tenders = projectWithTendersDTO.getTenders();

        //1.3 计算还款月数
        Double ceil = Math.ceil(project.getPeriod() / 30);
        int i = ceil.intValue();
        //1.4 还款方式=》只针对等额本息
        String repaymentWay = project.getRepaymentWay();
        if (repaymentWay.equals(RepaymentWayCode.FIXED_CAPITAL.getCode())) {
            //1.5 生成还款计划

            EqualInterestRepayment equalInterestRepayment = RepaymentUtil.fixedRepayment(project.getAmount(), project.getBorrowerAnnualRate(), i, projectWithTendersDTO.getCommissionBorrowerAnnualRate());
            //保存还款计划

            List<RepaymentPlan> repaymentPlans = saveRepaymentPlan(project, equalInterestRepayment);
//生成投资人应收明细
            //2.1 生成投资人应收明细
            tenders.forEach(tender -> {
                //同样调用RepaymentUtil.fixedRepayment
                EqualInterestRepayment equalInterestRepayment1 = RepaymentUtil.fixedRepayment(tender.getAmount(), tender.getProjectAnnualRate(), i, projectWithTendersDTO.getCommissionBorrowerAnnualRate());
/* 由于投标人的收款明细需要还款信息,所有遍历还款计划,
把还款期数与投资人应收期数对应上*/
                repaymentPlans.forEach(plan -> {
// 2.2 保存应收明细到数据库
                    saveRreceivablePlan(plan, tender, equalInterestRepayment1);
                });

            });
            //保存到数据库
        } else {
            return "-1";
        }


        return DepositoryReturnCode.RETURN_CODE_00000.getCode();
    }

    @Override
    public List<RepaymentPlan> saveRepaymentPlan(ProjectDTO projectDTO, EqualInterestRepayment equalInterestRepayment) {
        ArrayList<RepaymentPlan> repaymentPlans = new ArrayList<>(10);
        //获取每期的利息
        final Map<Integer, BigDecimal> interestMap = equalInterestRepayment.getInterestMap();
        //平台收取的利息
        final Map<Integer, BigDecimal> commissionMap = equalInterestRepayment.getCommissionMap();
        //每期还款本金
        final Map<Integer, BigDecimal> principalMap = equalInterestRepayment.getPrincipalMap();
        principalMap.forEach((k, v) -> {
            final RepaymentPlan repaymentPlan = new RepaymentPlan();
            repaymentPlan.setProjectId(projectDTO.getId());
            repaymentPlan.setProjectNo(projectDTO.getProjectNo());
            repaymentPlan.setConsumerId(projectDTO.getConsumerId());
            repaymentPlan.setUserNo(projectDTO.getUserNo());
            repaymentPlan.setNumberOfPeriods(k);
            repaymentPlan.setInterest(commissionMap.get(k));
            repaymentPlan.setPrincipal(v);
            repaymentPlan.setAmount(repaymentPlan.getPrincipal().add(repaymentPlan.getInterest()));
            repaymentPlan.setShouldRepaymentDate(DateUtil.localDateTimeAddMonth(DateUtil.now(), k));
            repaymentPlan.setRepaymentStatus("0");
            repaymentPlan.setCreateDate(LocalDateTime.now());

            repaymentPlans.add(repaymentPlan);
        });
        this.saveBatch(repaymentPlans);


        return repaymentPlans;
    }


    //保存应收明细到数据库
    private void saveRreceivablePlan(RepaymentPlan repaymentPlan,
                                     TenderDTO tender,
                                     EqualInterestRepayment receipt) {
// 应收本金
        final Map<Integer, BigDecimal> principalMap = receipt.getPrincipalMap();
// 应收利息
        final Map<Integer, BigDecimal> interestMap = receipt.getInterestMap();
// 平台收取利息
        final Map<Integer, BigDecimal> commissionMap =
                receipt.getCommissionMap();
// 封装投资人应收明细
        ReceivablePlan receivablePlan = new ReceivablePlan();
// 投标信息标识
        receivablePlan.setTenderId(tender.getId());
// 设置期数
        receivablePlan.setNumberOfPeriods(repaymentPlan.getNumberOfPeriods());
// 投标人用户标识
        receivablePlan.setConsumerId(tender.getConsumerId());
// 投标人用户编码
        receivablePlan.setUserNo(tender.getUserNo());
// 还款计划项标识
        receivablePlan.setRepaymentId(repaymentPlan.getId());
// 应收利息
        receivablePlan.setInterest(interestMap.get(repaymentPlan
                .getNumberOfPeriods()));
// 应收本金
        receivablePlan.setPrincipal(principalMap.get(repaymentPlan
                .getNumberOfPeriods()));
// 应收本息 = 应收本金 + 应收利息
        receivablePlan.setAmount(receivablePlan.getInterest()
                .add(receivablePlan.getPrincipal()));
// 应收时间
        receivablePlan.setShouldReceivableDate(repaymentPlan
                .getShouldRepaymentDate());
// 应收状态, 当前业务为未收
        receivablePlan.setReceivableStatus(0);
// 创建时间
        receivablePlan.setCreateDate(DateUtil.now());
// 设置投资人让利, 注意这个地方是具体: 佣金
        receivablePlan.setCommission(commissionMap
                .get(repaymentPlan.getNumberOfPeriods()));
// 保存到数据库
        receivablePlanService.save(receivablePlan);
    }
}

