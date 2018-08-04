package com.today.mysql.member.dto.freemud

case class MemberConsumeLog(
  ConsumeId: String,
  PartnerId: String,
  MemberId: String,
  OrgId: String,
  OrgCode: String,
  OrderId: String,
  OrderNo: String,
  ConsumeValue: Double,
  ConsumeCode: String,
  OperatorId: String,
  OperateTime: java.sql.Timestamp,
  CompanyCode: String
)
