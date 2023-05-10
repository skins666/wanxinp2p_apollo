package cn.itcast.wanxinp2p.consumer.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户绑定银行卡信息;
 * @author : http://www.chiner.pro
 * @date : 2023-5-10
 */
@ApiModel(value = "用户绑定银行卡信息",description = "")
@TableName("bank_card")
@Data
public class BankCard implements Serializable,Cloneable{
    /** 主键 */
    @ApiModelProperty(name = "主键",notes = "")
    @TableId
    private  Long  id ;
    /** 用户标识 */
    @ApiModelProperty(name = "用户标识",notes = "")
    private Long consumerId ;
    /** 银行编码 */
    @ApiModelProperty(name = "银行编码",notes = "")
    private String bankCode ;
    /** 银行名称 */
    @ApiModelProperty(name = "银行名称",notes = "")
    private String bankName ;
    /** 银行卡号 */
    @ApiModelProperty(name = "银行卡号",notes = "")
    private String cardNumber ;
    /** 银行预留手机号 */
    @ApiModelProperty(name = "银行预留手机号",notes = "")
    private String mobile ;
    /** 可用状态 */
    @ApiModelProperty(name = "可用状态",notes = "")
    private Integer status ;


}
