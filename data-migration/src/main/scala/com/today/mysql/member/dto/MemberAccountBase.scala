package com.today.mysql.member.dto

/**
* @author dapeng-tool
*/
case class MemberAccountBase(


  MemberId: String,

  OrderId: String,


  OrderNo: String,


  ConsumeValue: Double,


  ConsumeCode: String,


  OperateTime: java.sql.Timestamp


)
