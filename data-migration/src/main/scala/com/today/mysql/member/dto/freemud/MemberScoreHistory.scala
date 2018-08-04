package com.today.mysql.member.dto.freemud

case class MemberScoreHistory(
  ScoreId: String,
  MemberId: String,
  FeeAmount: Int,
  Score: Int,
  AfterScore: Int,
  Remark: String,
  CreateTime: java.sql.Timestamp,
  PartnerId: String,
  RecordType: Int,
  MemberScoreId: String,
  MemberScoreNo: String,
  OrderId: String,
  GiveFlag: Int
)
