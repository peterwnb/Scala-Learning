package com.today.mysql.member.dto

/**
* 对应实体member.member_coupon
**/
case class Coupon(

 /**
   *
   * *
   * 会员id
   *
   **/

 memberId: Long,

 /**
   *
   * *
   * 会员名称
   *
   **/

 memberName: String,

 /**
   *
   * *
   * 主键id
   *
   **/

 id: Long,

 /**
   *
   * *
   * 优惠券id
   *
   **/

 couponId: Long,

 /**
   *
   * *
   * 优惠券名字
   *
   **/

 couponName: String,

 /**
   *
   * *
   * 优惠券开始时间
   *
   **/

 startTime: java.sql.Timestamp,

 /**
   *
   * *
   * 优惠券结束时间
   *
   **/

 endTime: java.sql.Timestamp,

 /**
   *
   * *
   * 优惠券使用说明
   *
   **/

 couponRemark: String,

 /**
   *
   * *
   * 优惠券领取时间
   *
   **/

 receiveTime: java.sql.Timestamp,

 /**
   *
   * *
   * 来源标识{1：游戏过关赠送2：排行榜赠送3：金币兑换}
   *
   **/

 sourceFlag: Int,

 /**
   *
   * *
   * 来源id
   *
   **/

 sourceId: Option[Long],

 /**
   *
   * *
   * 是否使用
   *
   **/

 isUsed: Int,

 /**
   *
   * *
   * 优惠券使用时间
   *
   **/

 usedTime: Option[java.sql.Timestamp],

 /**
   *
   * *
   * 外码
   *
   **/

 fkId: String,

 /**
   *
   * *
   * 二维码标示
   *
   **/

 scanCodeId: Option[String],

 /**
   *
   * *
   * 创建时间
   *
   **/

 createdAt: java.sql.Timestamp,

 /**
   *
   * *
   * 特指后台创建人(公司员工 id)
   *
   **/

 createdBy: Int,

 /**
   *
   * *
   * 更新时间
   *
   **/

 updatedAt: java.sql.Timestamp,

 /**
   *
   * *
   * 特指后台更新人(公司员工 id)
   *
   **/

 updatedBy: Int,

 /**
   *
   * *
   * 备注
   *
   **/

 remark: String,

 couponKey: String,

 storeId: Option[Int],

 storeName: String,

 orderId: Option[Int],

 oldCouponId: Option[String],

 couponPrice: Option[Double],

 couponType: Option[Int]
)
