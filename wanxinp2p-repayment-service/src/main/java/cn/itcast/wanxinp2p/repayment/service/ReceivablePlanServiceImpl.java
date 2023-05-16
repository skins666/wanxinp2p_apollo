package cn.itcast.wanxinp2p.repayment.service;

import cn.itcast.wanxinp2p.repayment.entity.ReceivablePlan;
import cn.itcast.wanxinp2p.repayment.mapper.ReceivablePlanMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class ReceivablePlanServiceImpl extends ServiceImpl<ReceivablePlanMapper, ReceivablePlan> implements ReceivablePlanService{
}
