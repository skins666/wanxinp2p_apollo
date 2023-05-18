package cn.itcast.wanxinp2p.repayment.service;

import cn.itcast.wanxinp2p.api.depository.DepositoryAgentApi;
import cn.itcast.wanxinp2p.api.depository.model.UserAutoPreTransactionRequest;
import cn.itcast.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import cn.itcast.wanxinp2p.api.repayment.model.RepaymentRequest;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.TenderDTO;
import cn.itcast.wanxinp2p.common.domain.*;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.common.util.DateUtil;
import cn.itcast.wanxinp2p.repayment.entity.ReceivableDetail;
import cn.itcast.wanxinp2p.repayment.entity.ReceivablePlan;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentDetail;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;
import cn.itcast.wanxinp2p.repayment.mapper.PlanMapper;
import cn.itcast.wanxinp2p.repayment.mapper.ReceivableDetailMapper;
import cn.itcast.wanxinp2p.repayment.mapper.ReceivablePlanMapper;
import cn.itcast.wanxinp2p.repayment.mapper.RepaymentDetailMapper;
import cn.itcast.wanxinp2p.repayment.model.EqualInterestRepayment;
import cn.itcast.wanxinp2p.repayment.util.RepaymentUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RepaymentServiceImpl implements RepaymentService {

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private ReceivablePlanMapper receivablePlanMapper;
    @Resource
    private RepaymentPlanService repaymentPlanService;


    @Resource
    private RepaymentDetailMapper repaymentDetailMapper;

    //注入feign代理
    @Resource
    private DepositoryAgentApi depositoryAgentApi;

    private ReceivableDetailMapper receivableDetailMapper;

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public String startRepayment(ProjectWithTendersDTO projectWithTendersDTO) {

        //1.生成借款人还款计划
        //1.1 获取标的信息
        ProjectDTO projectDTO = projectWithTendersDTO.getProject();

        //1.2 获取投标信息
        List<TenderDTO> tenders = projectWithTendersDTO.getTenders();

        //1.3 计算还款的月数
        Double ceil = Math.ceil(projectDTO.getPeriod() / 30.0);
        Integer month = ceil.intValue();

        //1.4 还款方式，只针对等额本息
        String repaymentWay = projectDTO.getRepaymentWay();

        if (repaymentWay.equals(RepaymentWayCode.FIXED_REPAYMENT.getCode())) {
            //1.5 生成还款计划
            EqualInterestRepayment fixedRepayment = RepaymentUtil.fixedRepayment(projectDTO.getAmount(), projectDTO.getBorrowerAnnualRate(), month, projectDTO.getCommissionAnnualRate());

            //1.6 保存还款计划
            List<RepaymentPlan> planList = saveRepaymentPlan(projectDTO, fixedRepayment);

            //2.生成投资人应收明细
            //2.1 根据投标信息生成应收明细
            tenders.forEach(tenderDTO -> {
                EqualInterestRepayment receipt = RepaymentUtil.fixedRepayment(tenderDTO.getAmount(), tenderDTO.getProjectAnnualRate(), month, projectWithTendersDTO.getCommissionInvestorAnnualRate());

                //2.2 保存应收明细到数据库
                planList.forEach(plan -> {
                    saveRreceivablePlan(plan, tenderDTO, receipt);
                });

            });

        } else {
            return "-1";
        }

        return DepositoryReturnCode.RETURN_CODE_00000.getCode();
    }

    /**
     * 查询到期还款计划
     *
     * @param date
     * @return
     */

    @Override
    public List<RepaymentPlan> SelectDueRepayment(String date) {
        //将String的data转换成LocalDateTime类型
        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
// 将日期向前推三天


        QueryWrapper<RepaymentPlan> repaymentPlanQueryWrapper = new QueryWrapper<>();
        repaymentPlanQueryWrapper.lambda().eq(RepaymentPlan::getShouldRepaymentDate, localDateTime)
                .eq(RepaymentPlan::getRepaymentStatus, 0);

        return repaymentPlanService.list(repaymentPlanQueryWrapper);

    }

    @Override
    public RepaymentDetail saveRepaymentDetail(RepaymentPlan repaymentPlan) {
        //1.先进行查询
        QueryWrapper<RepaymentDetail> repaymentDetailQueryWrapper = new QueryWrapper<>();
        repaymentDetailQueryWrapper.lambda().eq(RepaymentDetail::getRepaymentPlanId, repaymentPlan.getId());

        RepaymentDetail repaymentDetail = repaymentDetailMapper.selectOne(repaymentDetailQueryWrapper);
        if (repaymentDetail == null) {


            //2.查不到数据才进行保存
            repaymentDetail = new RepaymentDetail();
            repaymentDetail.setRepaymentPlanId(repaymentPlan.getId());
            repaymentDetail.setAmount(repaymentPlan.getAmount());
            repaymentDetail.setRepaymentDate(LocalDateTime.now());
            repaymentDetail.setAmount(repaymentPlan.getAmount());
            repaymentDetail.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
            repaymentDetail.setStatus(StatusCode.STATUS_OUT.getCode());

            repaymentDetailMapper.insert(repaymentDetail);

        }
        return repaymentDetail;
    }

    @Override
    public UserAutoPreTransactionRequest generateUserAutoTranactionRequest(RepaymentPlan repaymentPlan, String requestNo) {
        //1.构造请求数据
        UserAutoPreTransactionRequest request = new UserAutoPreTransactionRequest();
        request.setBizType(PreprocessBusinessTypeCode.REPAYMENT.getCode());
        request.setAmount(repaymentPlan.getAmount());
        request.setProjectNo(repaymentPlan.getProjectNo());
        request.setUserNo(repaymentPlan.getUserNo());
        request.setRemark("还款预处理");
        request.setRequestNo(requestNo);
        request.setId(repaymentPlan.getId());


        return request;
    }

    @Override
    public void executeRepayment(String date) {
        //1.查询到期还款计划
        List<RepaymentPlan> repaymentPlans = SelectDueRepayment(date);

        //2.遍历还款计划
        repaymentPlans.forEach(repaymentPlan -> {
            RepaymentDetail repaymentDetail = saveRepaymentDetail(repaymentPlan);
            //3.调用银行接口进行还款
            Boolean preRepaymentResult = preRepayment(repaymentPlan, repaymentDetail.getRequestNo());
            if (preRepaymentResult) {
                log.info("还款预处理成功");
                // TODO: 2023/5/18 0018 接口四
            }


        });


    }

    /**
     * @param repaymentPlan 还款计划
     * @param preRequestNo  预处理请求流水号
     * @return
     */
    @Override
    public Boolean preRepayment(RepaymentPlan repaymentPlan, String preRequestNo) {
        //构造请求数据
        UserAutoPreTransactionRequest userAutoPreTransactionRequest = generateUserAutoTranactionRequest(repaymentPlan, preRequestNo);
        //请求存管代理服务
        RestResponse<String> stringRestResponse = depositoryAgentApi.userAutoPreTransaction(userAutoPreTransactionRequest);
        //判断是否成功
        return stringRestResponse.getResult().equals(DepositoryReturnCode.RETURN_CODE_00000.getCode());


    }

    @Transactional
    @Override
    public Boolean confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest) {
        //1.更新还款明细，已同步

        UpdateWrapper<RepaymentDetail> repaymentDetailUpdateWrapper = new UpdateWrapper<>();
        repaymentDetailUpdateWrapper.lambda().eq(RepaymentDetail::getRequestNo, repaymentRequest.getRequestNo())

                .set(RepaymentDetail::getStatus, StatusCode.STATUS_IN.getCode());
         repaymentDetailMapper.update(null,repaymentDetailUpdateWrapper);

         //通过还款计划流水号查询还款计划

      Long  repaymentId = repaymentPlan.getId();

        //2.更新应收计划，已收款（根据投标id和期数）


        //保存应收明细到表中
        //根据rePaymentId查询应收计划
        QueryWrapper<ReceivablePlan> receivablePlanQueryWrapper = new QueryWrapper<>();
        receivablePlanQueryWrapper.lambda().eq(ReceivablePlan::getRepaymentId, repaymentId);
        List<ReceivablePlan> receivablePlanList = receivablePlanMapper.selectList(receivablePlanQueryWrapper);
        //遍历应收计划
        receivablePlanList.forEach(receivablePlan -> {
            receivablePlan.setReceivableStatus(1);
            //更新应收计划
            receivablePlanMapper.updateById(receivablePlan);
            ReceivableDetail receivableDetail = new ReceivableDetail();
            receivableDetail.setReceivableId(receivablePlan.getId());
            receivableDetail.setAmount(receivablePlan.getAmount());
            receivableDetail.setReceivableDate(LocalDateTime.now());

            receivableDetailMapper.insert(receivableDetail);
        });





        //3.更新还款计划，已还款
        UpdateWrapper<RepaymentPlan> repaymentPlanUpdateWrapper = new UpdateWrapper<>();
        repaymentPlanUpdateWrapper.lambda().eq(RepaymentPlan::getId, repaymentId)
                .set(RepaymentPlan::getRepaymentStatus, "1");
        boolean update = repaymentPlanService.update(null, repaymentPlanUpdateWrapper);
        // TODO: 2023/5/18 本地事务
        return update;
    }

    //保存还款计划到数据库
    public List<RepaymentPlan> saveRepaymentPlan(ProjectDTO projectDTO,
                                                 EqualInterestRepayment
                                                         fixedRepayment) {
        List<RepaymentPlan> repaymentPlanList = new ArrayList<>();
        // 获取每期利息
        final Map<Integer, BigDecimal> interestMap =
                fixedRepayment.getInterestMap();
        // 平台收取利息
        final Map<Integer, BigDecimal> commissionMap =
                fixedRepayment.getCommissionMap();
        // 获取每期本金
        fixedRepayment.getPrincipalMap().forEach((k, v) -> {
            // 还款计划封装数据
            final RepaymentPlan repaymentPlan = new RepaymentPlan();
            // 标的id
            repaymentPlan.setProjectId(projectDTO.getId());
            // 发标人用户标识
            repaymentPlan.setConsumerId(projectDTO.getConsumerId());
            // 发标人用户编码
            repaymentPlan.setUserNo(projectDTO.getUserNo());
            // 标的编码
            repaymentPlan.setProjectNo(projectDTO.getProjectNo());
            // 期数
            repaymentPlan.setNumberOfPeriods(k);
            // 当期还款利息
            repaymentPlan.setInterest(interestMap.get(k));
            // 还款本金
            repaymentPlan.setPrincipal(v);
            // 本息 = 本金 + 利息
            repaymentPlan.setAmount(repaymentPlan.getPrincipal()
                    .add(repaymentPlan.getInterest()));
            // 应还时间 = 当前时间 + 期数( 单位月 )
            repaymentPlan.setShouldRepaymentDate(DateUtil.localDateTimeAddMonth(DateUtil.now(), k));
            // 应还状态, 当前业务为待还
            repaymentPlan.setRepaymentStatus("0");
            // 计划创建时间
            repaymentPlan.setCreateDate(DateUtil.now());
            // 设置平台佣金( 借款人让利 ) 注意这个地方是 具体佣金
            repaymentPlan.setCommission(commissionMap.get(k));
            // 保存到数据库
            planMapper.insert(repaymentPlan);
            repaymentPlanList.add(repaymentPlan);
        });
        return repaymentPlanList;
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
        final Map<Integer, BigDecimal> commissionMap = receipt.getCommissionMap();
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
        // 设置投资人让利, 注意这个地方是具体佣金
        receivablePlan.setCommission(commissionMap
                .get(repaymentPlan.getNumberOfPeriods()));
        // 保存到数据库
        receivablePlanMapper.insert(receivablePlan);
    }
}
