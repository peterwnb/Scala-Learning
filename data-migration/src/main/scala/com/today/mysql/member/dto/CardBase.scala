package com.today.mysql.member.dto

/**
  * @author dapeng-tool
  */case class CardBase (
   /**
   * 礼品卡主键id
   */
id : Long,

   /**
   * 卡号
   */
cardNo : String,

   /**
   * 总面值，单位：元
   */
faceValue : BigDecimal,

   /**
   * 卡成本，单位：元
   */
cardCost : BigDecimal,

   /**
   * 状态标识,1:待领用(to_be_used);2:待发售(wait_for_sale);3:使用中(in_use);4:使用完(use_finished);5:黑卡(black_card);6:已兑换(has_be_convert);7:待领取
   */
status : Int,

   /**
   * 操作人ID
   */
createdBy : Int,

   /**
   * 操作时间
   */
createdAt : java.sql.Timestamp,

   /**
   * 更新人
   */
updatedBy : Int,

   /**
   * 更新时间
   */
updatedAt : java.sql.Timestamp,

   /**
   * 关联会员id
   */
memberId : Long,

   /**
   * 剩余金额，单位：元
   */
cardBalance : BigDecimal,

   /**
   * 卡片生效日期
   */
startDate : java.sql.Timestamp,

   /**
   * 结束日期
   */
endDate : java.sql.Timestamp,

   /**
   * 售价，单位：元
   */
saleValue : BigDecimal,

   /**
   * 密码
   */
password : String,

   /**
     * 转赠时间
     */
donationAt : Option[java.sql.Timestamp],

   /**
     * 接受时间
     */
receiveAt : Option[java.sql.Timestamp],

   /**
     * 卡类型：1-普通卡；2-增设虚拟卡
     */
cardType : Option[Int],

oldCardId: Option[String]= None,
)
