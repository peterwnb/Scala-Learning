package com.today.mysql.member.dto

/**
  * @author dapeng-tool
  */case class MemberCardAccount (
   /**
   * 
   */
id : Long,

   /**
   * 会员id
   */
memberId : Long,

   /**
   * 会员名称
   */
memberName : String,

   /**
   * 账户余额
   */
cardBalance : BigDecimal,

   /**
   * 账户状态,1:正常(normal);2:冻结(frezz)
   */
cardAccountStatus : Int,

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
)
