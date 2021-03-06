package com.today.mysql.member.dto.freemud

case class OrderOrderBase(
  OrderId: String,
  OrderNo: String,
  PartnerId: String,
  OrgId: String,
  OrgCode: String,
  TypeFlag: Int,
  StatusFlag: Int,
  MemberId: String,
  CardNos: String,
  ShouldPay: Double,
  ActualPay: Double,
  CardPay: Double,
  CashPay: Double,
  CodePay: Double,
  Remark: String,
  OperatorId: String,
  OperateTime: java.sql.Timestamp,
  OutTradeNo: String,
  RefundAmount: Double,
  TransId: String,
  ScorePay: Double,
  PosCode: String,
  DiscountAmount: Double,
  PayChannelCode: String,
  BusinessDate: String,
  AliOpenId: String,
  UpdateTime: java.sql.Timestamp
)
