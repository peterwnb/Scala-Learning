package com.today.mysql.member.dto.freemud

case class CardCardBase(
 CardId: String,
 CardNo: String,
 FaceValue: Double,
 StatusFlag: Int,
 MemberId: String,
 Password: String,
 LeftValue: Double,
 SaleValue: Double,
 StartDate: java.sql.Timestamp,
 EndDate: java.sql.Timestamp,
 OperateTime: java.sql.Timestamp
)
