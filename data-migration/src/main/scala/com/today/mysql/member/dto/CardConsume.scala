package com.today.mysql.member.dto

/**
  * @author dapeng-tool
  */case class CardConsume (
   /**
   * 主键id
   */
id : Long,

   /**
   * 卡编号id
   */
cardId : Long,

   /**
   * 订单编号，关联card_order_base，为空表示退卡
   */
orderId : Long,

   /**
   * 消费金额，单位：元（负额表示退卡的退款）
   */
consumeAmount : BigDecimal,

   /**
   * '变动前余额'
   */
lastBalance : BigDecimal,

   /**
   * 操作人编号，关联system_user_base.UserId
   */
createdBy : Int,

   /**
   * 操作时间
   */
createdAt : java.sql.Timestamp,

   /**
   * 会员编号，关联member_member_base.MemberId
   */
memberId : Long,

   /**
   * 卡号
   */
cardNo : String,

   /**
     * 会员姓名
     */
   memberName : Option[String] = None,

   oldConsumeId : Option[String] = None,
                           )
