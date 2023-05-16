package cn.itcast.wanxinp2p.transaction.common.utils;

import java.math.BigDecimal;

/**
 * <P>
 * 投资人预期收益计算工具
 * </p>
 *
 * @author wuzhao@itcast.cn
 * @since 2019/6/11
 */
public class IncomeCalcUtil {

    /**
     * 等额本息 投资人收益计算
     *
     * @param invest    投资金额
     * @param yearRate 年利率
     * @param month     月数
     * @return 总收益
     */
/**
 * 计算固定收益投资的总收益
 * @param invest 投资金额
 * @param yearRate 年利率
 * @param month 投资期限，以月为单位
 * @return 投资期限内的总收益
 */
public static BigDecimal getIncomeTotalInterest(BigDecimal invest, BigDecimal yearRate, int month) {
    // 计算月利率
    double monthRate = yearRate.doubleValue() / 12;
    BigDecimal totalInterest = new BigDecimal(0);
    // 计算每个月的利息并累加到总收益中
    for (int i = 1; i < month + 1; i++) {
        // 计算每个月的利息收益
        BigDecimal multiply = invest.multiply(new BigDecimal(monthRate));
        //计算从当前月开始到投资期限结束的时间内的利息收益增量：
        BigDecimal sub = new BigDecimal(Math.pow(1 + monthRate, month)).subtract(new BigDecimal(Math.pow(1 + monthRate, i - 1)));
        //根据以上参数计算出当前月份的利息收益：
        BigDecimal monthInterest = multiply.multiply(sub).divide(new BigDecimal(Math.pow(1 + monthRate, month) - 1), 6, BigDecimal.ROUND_DOWN);
        // 将计算出来的利息收益累加到总收益中
        monthInterest = monthInterest.setScale(2, BigDecimal.ROUND_DOWN);
        totalInterest = totalInterest.add(monthInterest);
    }
    return totalInterest;
}



    public static void main(String[] args) {
        // 投资金额
        BigDecimal invest = new BigDecimal(10000);
        // 时间
        int month = 12;
        // 年利率
        BigDecimal yearRate = new BigDecimal(0.1);

        System.out.println(getIncomeTotalInterest(invest, yearRate, month));
    }
}
