package cn.itcast.wanxinp2p.consumer.service;

import cn.itcast.wanxinp2p.consumer.entity.BankCard;
import cn.itcast.wanxinp2p.consumer.model.BankCardDTO;
import com.baomidou.mybatisplus.extension.service.IService;


public interface BandCardService extends IService<BankCard> {


      BankCardDTO getByConsumerId(Long consumerId);



      BankCardDTO getByCardNumber(String cardNumber);


}
