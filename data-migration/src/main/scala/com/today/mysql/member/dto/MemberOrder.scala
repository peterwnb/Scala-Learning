package com.today.mysql.member.dto

case class MemberOrder(

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
  * 订单号
  *
  **/

orderNo: String,

/**
  *
  * *
  * 订单类型{1：门店订单2：充值订单3：快捷购订单4：自助购订单5：套卡订单}
  *
  **/

orderType: Int,

/**
  *
  * *
  * 订单状态{1：待支付2：已支付3：已结算4：已撤销}
  *
  **/

orderStatus: Int,

/**
  *
  * *
  * 应付金额，单位：元
  *
  **/

shouldPay: Double,

/**
  *
  * *
  * 实际付款金额
  *
  **/

actualPay: Double,

/**
  *
  * *
  * 卡支付金额
  *
  **/

cardPay: Double,

/**
  *
  * *
  * 现金支付
  *
  **/

cashPay: Double,

/**
  *
  * *
  * 扫码支付金额
  *
  **/

codePay: Double,

/**
  *
  * *
  * 备注
  *
  **/

remark: String,

/**
  *
  * *
  * 操作人
  *
  **/

operatorId: String,

/**
  *
  * *
  * 退款金额
  *
  **/

refundAmount: Double,

/**
  *
  * *
  * 商户交易流水号
  *
  **/

transId: String,

/**
  *
  * *
  * 积分支付金额
  *
  **/

scorePay: Double,

/**
  *
  * *
  * Pos机编号
  *
  **/

posCode: String,

/**
  *
  * *
  * 优惠金额，单位：元
  *
  **/

discountAmount: Double,

/**
  *
  * *
  * 第三方支付渠道
  *
  **/

payChannelCode: String,

/**
  *
  * *
  * 营业日
  *
  **/

businessDate: String,

/**
  *
  * *
  * 修改时间
  *
  **/

updateTime: java.sql.Timestamp,

/**
  *
  **
  * 商品信息
  *
  **/

commodity: String,

/**
  *
  **
  * 优惠劵
  *
  **/

coupon: String,

integral: Int,

oldOrderId: Option[String] = None,
                      )
