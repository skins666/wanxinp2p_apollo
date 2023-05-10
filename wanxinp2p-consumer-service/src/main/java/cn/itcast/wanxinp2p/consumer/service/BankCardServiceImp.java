package cn.itcast.wanxinp2p.consumer.service;

import cn.itcast.wanxinp2p.consumer.entity.BankCard;
import cn.itcast.wanxinp2p.consumer.mapper.BankCardMapper;
import cn.itcast.wanxinp2p.consumer.model.BankCardDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class BankCardServiceImp extends ServiceImpl<BankCardMapper,BankCard> implements BandCardService{

    /**
     * 根据用户id查询银行卡信息
     * @param consumerId
     * @return
     */
    @Override
    public BankCardDTO getByConsumerId(Long consumerId) {
        BankCard one = getOne(new QueryWrapper<BankCard>().lambda().eq(BankCard::getConsumerId, consumerId), false);
        if (one == null) {
            return null;
        }
        BankCardDTO bankCardDTO=new BankCardDTO();

        BeanUtils.copyProperties(one,bankCardDTO);
        return bankCardDTO;
    }

    /**
     * 根据卡号查询银行卡信息
     *
     * @param cardNumber
     * @return
     */
    @Override
    public BankCardDTO getByCardNumber(String cardNumber) {

        BankCard one = getOne(new QueryWrapper<BankCard>().lambda().eq(BankCard::getCardNumber, cardNumber));
        if (one == null) {
            return null;
        }
        BankCardDTO bankCardDTO=new BankCardDTO();
        BeanUtils.copyProperties(one, bankCardDTO);


        return bankCardDTO;
    }
}
