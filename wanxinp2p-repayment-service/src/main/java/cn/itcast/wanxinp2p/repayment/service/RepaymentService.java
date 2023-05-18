package cn.itcast.wanxinp2p.repayment.service;

import cn.itcast.wanxinp2p.api.depository.model.UserAutoPreTransactionRequest;
import cn.itcast.wanxinp2p.api.repayment.model.ProjectWithTendersDTO;
import cn.itcast.wanxinp2p.api.repayment.model.RepaymentRequest;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentDetail;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;

import java.util.List;

public interface RepaymentService {

    /**
     * 启动还款
     * @param projectWithTendersDTO
     * @return
     */
    String startRepayment(ProjectWithTendersDTO projectWithTendersDTO);


    /**
     * 查询到期还款计划
     * @param date
     * @return
     */
    List<RepaymentPlan>  SelectDueRepayment(String date);


    /**
     * 根据保存计划保存还款明细
     */
    RepaymentDetail saveRepaymentDetail(RepaymentPlan repaymentPlan);


    UserAutoPreTransactionRequest generateUserAutoTranactionRequest(RepaymentPlan repaymentDetail, String requestNo);


    /**
     * 执行还款
     */
    void executeRepayment(String date);

    /**
     * 预处理还款
     * @param repaymentPlan 还款计划
     * @param preRequestNo 预处理请求流水号
     * @return
     */

    Boolean preRepayment(RepaymentPlan repaymentPlan, String preRequestNo);


    /**
     * .处理本地事务做更新
     * @param repaymentPlan
     * @param repaymentRequest
     * @return
     */
    Boolean confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest);
}
