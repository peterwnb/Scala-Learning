package com.today.mysql.member.dto.freemud

/**
  * 礼品卡消费流水
  * @param ConsumeId
  * @param ConsumeValue
  * @param MemberId
  * @param CardId
  * @param CardNo
  * @param TypeCode
  * @param OrderId
  * @param OrderNo
  * @param ConsumeCode
  * @param OperateTime
  */
case class CardCardConsume(
ConsumeId: String,
ConsumeValue: Double,
MemberId: String,
CardId:String,
CardNo: String,
TypeCode: String,
OrderId:String,
OrderNo: String,
ConsumeCode: String,
OperateTime: java.sql.Timestamp,
)
