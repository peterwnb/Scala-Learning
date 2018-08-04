package com.today.mysql.member.sql

import com.today.common.FreemudDataSource.mysqlData
import com.today.mysql.member.dto.MemberBase
import com.today.mysql.member.dto.freemud._
import com.today.service.commons.`implicit`.Implicits._
import wangzx.scala_commons.sql._

object FreemudSql {

  def getMemberBase(memberId: String): Option[MemberBase] = {
    mysqlData.row[MemberBase](
      sql"""
           SELECT * FROM member_member_base WHERE MemberId=${memberId}
         """)
  }

  def listIncMemberBase(referTime: java.sql.Timestamp,start:Int,limit:Int): List[MemberBase] = {
    mysqlData.rows[MemberBase](
      sql"""
           SELECT * FROM member_member_base WHERE RegisterTime>=${referTime} ORDER BY RegisterTime LIMIT ${start},${limit}
         """)
  }

  def getMemberMemberChannel(memberId: String): Option[MemberMemberChannel] = {
    mysqlData.row[MemberMemberChannel](
      sql"""
           SELECT * FROM member_member_base WHERE MemberId=${memberId}
         """)
  }

  def listIncOrderBase(referTime: java.sql.Timestamp, start: Int, limit: Int): List[OrderOrderBase] = {
    mysqlData.rows[OrderOrderBase](
      sql"""
           SELECT * FROM order_order_base WHERE OperateTime>=${referTime} ORDER BY OperateTime LIMIT ${start},${limit}
         """)
  }


  def listIncScoreHistory(referTime: java.sql.Timestamp, start: Int, limit: Int): List[MemberScoreHistory] = {
    mysqlData.rows[MemberScoreHistory](
      sql"""
           SELECT * FROM member_score_history WHERE CreateTime>=${referTime} ORDER BY CreateTime LIMIT ${start},${limit}
         """)
  }

  def insertMemberBase(memberBase: MemberMemberBase): Unit = {
    val headSql = sql""" INSERT INTO member_member_base SET """

    val optionSql = List[SQLWithArgs](
      sql"""  MemberId=${memberBase.MemberId}, """,
      sql"""  PartnerId = ${memberBase.PartnerId}, """,
      sql"""  Mobile = ${memberBase.Mobile}, """,
      sql"""  MemberName = ${memberBase.MemberName}, """,
      sql"""  SexFlag = ${memberBase.SexFlag}, """,
      sql"""  Birthday = ${memberBase.Birthday}, """,
      sql"""  WeixinOpenId = ${memberBase.WeixinOpenId}, """,
      sql"""  AliOpenId = ${memberBase.AliOpenId}, """,
      sql"""  Photo = ${memberBase.Photo}, """,
      sql"""  Tags = ${memberBase.Tags}, """,
      sql"""  Score = ${memberBase.Score}, """,
      sql"""  Coin = ${memberBase.Coin}, """,
      sql"""  CoinActiveFlag = ${memberBase.CoinActiveFlag}, """,
      sql"""  LevelCode = ${memberBase.LevelCode}, """,
      sql"""  ActiveFlag = ${memberBase.ActiveFlag}, """,
      sql"""  RegisterTime = ${memberBase.RegisterTime}, """,
      sql"""  RegionCode = ${memberBase.RegionCode}, """,
      sql"""  Address = ${memberBase.Address}, """,
      sql"""  Account = ${memberBase.Account}, """,
      sql"""  Password = ${memberBase.Password}, """,
      sql"""  LeftValue = ${memberBase.LeftValue}, """,
      sql"""  ChannelFlag = ${memberBase.ChannelFlag}, """,
      sql"""  MemberSign = ${memberBase.MemberSign}, """,
      //      sql"""  TotalConsume = ${memberBase.TotalConsume} """,
      //      sql"""  TotalOrder = ${memberBase.TotalOrder} """,
      //      sql"""  OrderTime = ${memberBase.OrderTime} """,
      //      sql"""  AvgConsume = ${memberBase.AvgConsume} """,
      //      sql"""  ImageURL = ${memberBase.ImageURL} """,
      //      sql"""  RegisterChannelCode = ${memberBase.RegisterChannelCode} """,
      //      sql"""  RegisterStoreCode = ${memberBase.RegisterStoreCode} """,
      //      sql"""  ScanCodeId = ${memberBase.ScanCodeId} """,
      //      sql"""  RegisterStoreCode = ${memberBase.RegisterStoreCode} """,
      //      sql"""  NickName = ${memberBase.NickName} """,
      //      sql"""  City = ${memberBase.City} """,
      //      sql"""  Province = ${memberBase.Province} """,
      sql"""  ApproveTime = ${memberBase.ApproveTime} """
      //        memberBase.City.optional(City => sql"""  City = ${City} """),
    ).reduceLeft(_ + _)
    mysqlData.executeUpdate(headSql + optionSql)
  }

  def insertOrderBase(orderBase: OrderOrderBase): Unit = {
    val headSql = sql""" INSERT INTO order_order_base SET """
    val optionSql = List[SQLWithArgs](
      sql"""  OrderId=${orderBase.MemberId}, """,
      sql"""  OrderNo = ${orderBase.PartnerId}, """,
      sql"""  PartnerId = ${orderBase.PartnerId}, """,
      sql"""  OrgId = ${orderBase.OrgId}, """,
      sql"""  OrgCode = ${orderBase.OrgCode}, """,
      sql"""  TypeFlag = ${orderBase.TypeFlag}, """,
      sql"""  StatusFlag = ${orderBase.StatusFlag}, """,
      sql"""  MemberId = ${orderBase.AliOpenId}, """,
      sql"""  CardNos = ${orderBase.CardNos}, """,
      sql"""  ShouldPay = ${orderBase.ShouldPay}, """,
      sql"""  ActualPay = ${orderBase.ActualPay}, """,
      sql"""  CardPay = ${orderBase.CardPay}, """,
      sql"""  CashPay = ${orderBase.CashPay}, """,
      sql"""  CodePay = ${orderBase.CodePay}, """,
      sql"""  Remark = ${orderBase.Remark}, """,
      sql"""  OperatorId = ${orderBase.OperatorId}, """,
      sql"""  OperateTime = ${orderBase.OperateTime}, """,
      sql"""  OutTradeNo = ${orderBase.OutTradeNo}, """,
      sql"""  RefundAmount = ${orderBase.RefundAmount}, """,
      sql"""  TransId = ${orderBase.TransId}, """,
      sql"""  ScorePay = ${orderBase.ScorePay}, """,
      sql"""  PosCode = ${orderBase.PosCode}, """,
      sql"""  DiscountAmount = ${orderBase.DiscountAmount}, """,
      sql"""  PayChannelCode = ${orderBase.PayChannelCode}, """,
      sql"""  BusinessDate = ${orderBase.BusinessDate}, """,
      sql"""  AliOpenId = ${orderBase.AliOpenId}, """,
      sql"""  UpdateTime = now() """
    ).reduceLeft(_ + _)
    mysqlData.executeUpdate(headSql + optionSql)
  }

  def insertMemberConsume(consumeLog: MemberConsumeLog): Unit = {
    val headSql = sql""" INSERT INTO member_consume_log SET """
    val optionSql = List[SQLWithArgs](
      sql"""  ConsumeId=${consumeLog.ConsumeId}, """,
      sql"""  PartnerId=${consumeLog.PartnerId}, """,
      sql"""  MemberId=${consumeLog.MemberId}, """,
      sql"""  OrgId=${consumeLog.OrgId}, """,
      sql"""  OrgCode=${consumeLog.OrgCode}, """,
      sql"""  OrderId=${consumeLog.OrderId}, """,
      sql"""  OrderNo=${consumeLog.OrderNo}, """,
      sql"""  ConsumeValue=${consumeLog.ConsumeValue}, """,
      sql"""  ConsumeCode=${consumeLog.ConsumeCode}, """,
      sql"""  OperatorId=${consumeLog.OperatorId}, """,
      sql"""  OperateTime=${consumeLog.OperateTime}, """
    ).reduceLeft(_ + _)
    mysqlData.executeUpdate(headSql + optionSql)
  }

  def insertScoreHistory(scoreHistory: MemberScoreHistory): Unit = {
    val headSql = sql""" INSERT INTO member_score_history SET """
    val optionSql = List[SQLWithArgs](
      sql"""  ScoreId=${scoreHistory.ScoreId}, """,
      sql"""  MemberId=${scoreHistory.MemberId}, """,
      sql"""  FeeAmount=${scoreHistory.FeeAmount}, """,
      sql"""  Score=${scoreHistory.Score}, """,
      sql"""  AfterScore=${scoreHistory.AfterScore}, """,
      sql"""  Remark=${scoreHistory.Remark}, """,
      sql"""  CreateTime=${scoreHistory.CreateTime}, """,
      sql"""  PartnerId=${scoreHistory.PartnerId}, """,
      sql"""  RecordType=${scoreHistory.RecordType}, """,
      sql"""  MemberScoreId=${scoreHistory.MemberScoreId}, """,
      sql"""  MemberScoreNo=${scoreHistory.MemberScoreNo}, """,
      sql"""  OrderId=${scoreHistory.OrderId}, """,
      sql"""  GiveFlag=${scoreHistory.GiveFlag}, """
    ).reduceLeft(_ + _)
    mysqlData.executeUpdate(headSql + optionSql)
  }

  def updateMemberLeftValue(memberId: String, consumeAmount: Double): Int = {
    mysqlData.executeUpdate(
      sql"""
           UPDATE member_member_base SET LeftValue=LeftValue - ${consumeAmount} WHERE MemberId=${memberId}
         """)
  }

  def updateMemberScore(memberId: String, modifyScore: Double): Int = {
    mysqlData.executeUpdate(
      sql"""
           UPDATE member_member_base SET Score=Score - ${modifyScore} WHERE MemberId=${memberId}
         """)
  }

  def getMemberCoupon(id: String): Option[MemberMemberCoupon] = {
    mysqlData.row[MemberMemberCoupon](
      sql"""
           SELECT * FROM member_member_coupon WHERE Id=${id}
         """)
  }

  def getMemberScoreHistory(id: String): Option[MemberScoreHistory] = {
    mysqlData.row[MemberScoreHistory](
      sql"""
           SELECT * FROM member_score_history WHERE ScoreId=${id}
         """)
  }

  def getOrderBase(id: String): Option[OrderOrderBase] = {
    mysqlData.row[OrderOrderBase](
      sql"""
           SELECT * FROM order_order_base WHERE OrderId=${id}
         """)
  }

  def getOrderBaseByNo(orderNo: String): Option[OrderOrderBase] = {
    mysqlData.row[OrderOrderBase](
      sql"""
           SELECT * FROM order_order_base WHERE OrderNo=${orderNo}
         """)
  }

  def getCardConsumeLog(id: String): Option[CardCardConsume] = {
    mysqlData.row[CardCardConsume](
      sql"""
           SELECT * FROM card_card_consume WHERE ConsumeId=${id}
         """)
  }


  def listIncMemberConsumeLog(referTime: java.sql.Timestamp): List[MemberConsumeLog] = {
    mysqlData.rows[MemberConsumeLog](
      sql"""
           SELECT * FROM member_consume_log WHERE OperateTime>=${referTime} ORDER BY OperateTime
         """)
  }

  def listIncCardConsumeLog(referTime: java.sql.Timestamp,start:Int,limit:Int): List[CardCardConsume] = {
    mysqlData.rows[CardCardConsume](
      sql"""
           SELECT * FROM card_card_consume WHERE OperateTime>=${referTime} ORDER BY OperateTime LIMIT ${start},${limit}
         """)
  }


  def listIncMemberCoupon(referTime: java.sql.Timestamp,start:Int,limit:Int): List[MemberMemberCoupon] = {
    mysqlData.rows[MemberMemberCoupon](
      sql"""
           SELECT * FROM member_member_coupon WHERE CreateTime>=${referTime} ORDER BY CreateTime LIMIT ${start},${limit}
         """)
  }

  /**
    * 获取最近已核销的优惠券
    *
    * @param recentTime
    * @return
    */
  def listUsedMemberCoupon(recentTime: java.sql.Timestamp): List[MemberMemberCoupon] = {
    mysqlData.rows[MemberMemberCoupon](
      sql"""
           SELECT * FROM member_member_coupon WHERE UseTime>=${recentTime} ORDER BY UseTime
         """)
  }

  def listConsumeLogByMemberId(memberId: String): List[MemberConsumeLog] = {
    mysqlData.rows[MemberConsumeLog](sql"""SELECT * FROM member_consume_log WHERE MemberId=${memberId}   """)
  }

  /**
    * 根据订单获取礼品卡充值流水，一个订单只会有一条充值流水
    *
    * @param orderNo
    * @return
    */
  def getRechargeCardConsume(orderNo: String): Option[CardCardConsume] = {
    mysqlData.row[CardCardConsume](
      sql"""
           SELECT * FROM card_card_consume WHERE OrderNo=${orderNo} AND ConsumeCode='CONSUME006'
         """)
  }

  def listCardConsumeByOrderNo(orderNo: String): List[CardCardConsume] = {
    mysqlData.rows[CardCardConsume](
      sql"""
           SELECT * FROM card_card_consume WHERE OrderNo=${orderNo}
         """)
  }

  def getCardCardBase(cardNo: String, memberId: String): Option[CardCardBase] = {
    mysqlData.row[CardCardBase](
      sql"""
           SELECT * FROM card_card_base WHERE CardNo=${cardNo} AND MemberId=${memberId}
         """)
  }

  def getCardCardBase(cardId: String): Option[CardCardBase] = {
    mysqlData.row[CardCardBase](
      sql"""
           SELECT * FROM card_card_base WHERE CardId=${cardId}
         """)
  }

  def listIncCardBase(referTime: java.sql.Timestamp,start:Int,limit:Int): List[CardCardBase] = {
    mysqlData.rows[CardCardBase](
      sql"""
           SELECT * FROM card_card_base WHERE OperateTime>=${referTime} ORDER BY OperateTime LIMIT ${start},${limit}
         """)
  }

  def listOrderByMemberId(memberId: String): List[OrderOrderBase] = {
    mysqlData.rows[OrderOrderBase](
      sql"""
           SELECT * FROM order_order_base WHERE MemberId=${memberId} ORDER BY UpdateTime ASC
         """)
  }
}
