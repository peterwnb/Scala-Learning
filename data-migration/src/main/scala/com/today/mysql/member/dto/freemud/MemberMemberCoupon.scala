package com.today.mysql.member.dto.freemud

case class MemberMemberCoupon(
  Id: String,
  PartnerId: String,
  MemberId: String,
  TypeCode: String,
  CouponName: String,
  StartTime: java.sql.Timestamp,
  EndTime: java.sql.Timestamp,
  Remark: String,
  CreateTime: java.sql.Timestamp,
  SourceFlag: Int,
  IsUsed: Int,
  UseTime: java.sql.Timestamp,
  FkId: String,
  SourceId: String,
  CouponId: String,
  StrategyId: String,
  RegisterChannelCode: String,
  RegisterStoreCode: String,
  ScanCodeId: String,
)