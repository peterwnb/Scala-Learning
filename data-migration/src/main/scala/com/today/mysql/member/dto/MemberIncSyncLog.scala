package com.today.mysql.member.dto

/**
* 非码库同步至新库日志表
*/
case class MemberIncSyncLog(
  id: Long,
  newId: Long,
  oldId: String,
  orderNo: Option[String],
  status: Int,
  source: Int,
  logType: Int,
  createTime: java.sql.Timestamp,
  updateTime: java.sql.Timestamp
)