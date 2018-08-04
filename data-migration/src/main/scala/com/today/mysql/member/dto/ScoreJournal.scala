package com.today.mysql.member.dto

/**
  * @author dapeng-tool
  */case class ScoreJournal (
   /**
   * 利用主键策略生成的唯一键
   */
id : Long,

   /**
   * 会员id
   */
memberId : Long,

   /**
   * 变动积分
   */
scorePrice : Int,

   /**
   * 订单id
   */
orderId : Long,

   /**
   * 积分来源类型,1:消费(consume);2:积分兑换(exchange);3:消费赠送(give);4:首充赠送(recharge);5:完善资料赠送(perfect);6:指定商品赠送(sku_give);7:会员日赠送(member_day);8:无现金日赠送(nocash_day);9:退款(refund);10:多倍商品赠送(mutlple);11:退单(back);12:会员生日赠送(birthday);13:组合套餐赠送(package);14:储值送(stored);15:手动赠送(give);16:注册赠送(register);17:系统扣除(system)
   */
resouceType : Int,

   /**
   * 变动之前积分
   */
scoreLast : Long,

   /**
   * 创建时间
   */
createdAt : java.sql.Timestamp,

   /**
   * 特指后台创建人(公司员工 id)
   */
createdBy : Int,

   /**
   * 更新时间
   */
updatedAt : java.sql.Timestamp,

   /**
   * 特指后台更新人(公司员工 id)
   */
updatedBy : Int,

   /**
   * 备注
   */

remark : String,

oldScoreId: Option[String] = None,
)
