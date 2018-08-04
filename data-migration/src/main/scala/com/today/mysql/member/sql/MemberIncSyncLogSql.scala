package com.today.mysql.member.sql

import com.today.common.MemberDataSource.mysqlData
import com.today.enums.{LogSourceEnum, LogTypeEnum, SyncLogStatusEnum}
import com.today.mysql.member.dto.MemberIncSyncLog
import com.today.service.commons.`implicit`.Implicits._
import wangzx.scala_commons.sql._

@Deprecated
object MemberIncSyncLogSql {
/*

  def getLastRecord(logType: Option[LogTypeEnum], source: Option[LogSourceEnum], status: Option[SyncLogStatusEnum]): Option[MemberIncSyncLog] = {
    val querySql =
      sql"""
        SELECT * FROM member_inc_sync_log WHERE 1=1
        """
    val optionSql = List[SQLWithArgs](
      status.optional(status => sql""" AND status = ${status.getVal} """),
      logType.optional(logType => sql""" AND log_type = ${logType.getVal} """),
      source.optional(source => sql""" AND source = ${source.getVal} """)
    ).reduceLeft(_ + _)
    val sortSql = sql""" ORDER BY create_time DESC """
    mysqlData.row[MemberIncSyncLog](querySql + optionSql + sortSql)
  }

  def get(newId: Option[Long], oldId: Option[String], logType: Option[LogTypeEnum], source: Option[LogSourceEnum]): Option[MemberIncSyncLog] = {
    val querySql =
      sql"""
        SELECT * FROM member_inc_sync_log WHERE 1=1
        """
    val optionSql = List[SQLWithArgs](
      newId.optional(newId => sql""" AND new_id = ${newId} """),
      oldId.optional(oldId => sql""" AND old_id = ${oldId} """),
      logType.optional(logType => sql""" AND log_type = ${logType.getVal} """),
      source.optional(source => sql""" AND source >= ${source.getVal} """)
    ).reduceLeft(_ + _)
    mysqlData.row[MemberIncSyncLog](querySql + optionSql)
  }

  def insert(newId: Long, oldId: String, logTypeEnum: LogTypeEnum, sourceEnum: LogSourceEnum, status: SyncLogStatusEnum, orderNo: Option[String]): Long = {
    mysqlData.generateKey[Long](
      sql"""
    INSERT INTO member_inc_sync_log
           SET
           new_id=${newId},
           old_id=${oldId},
           log_type=${logTypeEnum.getVal()},
           source=${sourceEnum.getVal()},
           status=${status.getVal()},
           order_no=${orderNo.getOrElse("")},
           create_time=now(),
           update_time=now()
        """
    )
  }

  def getMemberIdByFreemudId(freemudMemberId: String): Option[Long] = {
    val opt = get(None, Some(freemudMemberId), Some(LogTypeEnum.MEMBER_MEMBER_BASE), None)
    if (opt.nonEmpty) {
      Some(opt.get.newId)
    } else {
      None
    }
  }

  def getCouponIdByFreemudId(freemudMemberId: String): Option[Long] = {
    val opt = get(None, Some(freemudMemberId), Some(LogTypeEnum.MEMBER_MEMBER_COUPON), None)
    if (opt.nonEmpty) {
      Some(opt.get.newId)
    } else {
      None
    }
  }


  def getOrderIdByFreemudOrderId(freemudOrderId: String): Option[Long] = {
    val opt = get(None, Some(freemudOrderId), Some(LogTypeEnum.ORDER_ORDER_BASE), None)
    if (opt.nonEmpty) {
      Some(opt.get.newId)
    } else {
      None
    }
  }

  def getCardIdByFreemudCardNo(freemudCardNo: String): Option[Long] = {
    val opt = get(None, Some(freemudCardNo), Some(LogTypeEnum.CARD_CARD_BASE), None)
    if (opt.nonEmpty) {
      Some(opt.get.newId)
    } else {
      None
    }
  }

  def updateStatus(id: Long, status: SyncLogStatusEnum): Int = {
    mysqlData.executeUpdate(
      sql"""
           UPDATE member_inc_sync_log SET status=${status.getVal()},update_time=now() WHERE id=${id}
         """)
  }

*/

}
