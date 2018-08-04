package com.today.tasks

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import com.today.common.GenIdUtil
import com.today.enums._
import com.today.mysql.member.dto.MemberBase
import com.today.mysql.member.dto.freemud._
import com.today.mysql.member.sql.{FreemudSql, MemberSql}
import org.slf4j.LoggerFactory

/**
  * 老库会员数据同步到新库任务
  */
class MemberOtonSyncTask(startTime: java.sql.Timestamp) {

  val logger = LoggerFactory.getLogger(getClass)

  val defaultSourceEnum = LogSourceEnum.INCREASE

  def start: Unit = {
    logger.info("MemberOtonSyncTask start.")
    val startTime = System.currentTimeMillis()
    try {
      step1()
      step2()
    } catch {
      case e => logger.error("", e)
    } finally {
      logger.info(s"MemberOtonSyncTask end, spent time=${System.currentTimeMillis() - startTime}")
    }
  }

  def step1(): Unit = {
    val begin = System.currentTimeMillis()
    syncIncMember()
    logger.info(s"syncIncMember finish time mark=${System.currentTimeMillis() - begin}")
    syncIncOrder()
    logger.info(s"syncIncOrder finish time mark=${System.currentTimeMillis() - begin}")
  }

  def step2(): Unit = {
    val begin = System.currentTimeMillis()
    syncIncCard()
    logger.info(s"syncIncCard finish time mark=${System.currentTimeMillis() - begin}")
    syncIncConsume()
    logger.info(s"syncIncConsume finish time mark=${System.currentTimeMillis() - begin}")
    syncIncScore()
    logger.info(s"syncIncScore finish time mark=${System.currentTimeMillis() - begin}")
    syncIncCoupon()
    logger.info(s"syncIncCoupon finish time mark=${System.currentTimeMillis() - begin}")
    syncUsedCoupon()
    logger.info(s"syncUsedCoupon finish time mark=${System.currentTimeMillis() - begin}")
  }

  def syncIncMember(): Unit = {
    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }
    var flag = true
    var start = 0
    while (flag) {
      val list = FreemudSql.listIncMemberBase(startTime,start,1000)
      if (list.nonEmpty) {
        list.foreach(
          x => exec.execute(() => {
            syncMember(x)
          })
        )
        start=start+1000
      }
      if (list.isEmpty || list.size < 1000) {
        flag = false
      }
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        logger.info("会员主档数据全部迁移完成")
        //执行完member然后执行
        return
      }
      Thread.sleep(200);
    }
  }

  /**
    * @param freemudMember
    */
  def syncMember(freemudMember: MemberBase): Unit = {
    val opt = MemberSql.getMemberByOldMemberId(freemudMember.MemberId)
    if (opt.nonEmpty) {
      logger.warn(s"memberId=${freemudMember.MemberId} already sync.")
      return
    }
    val memberCode = GenIdUtil.getId(GenIdUtil.MEMBER_CODE)
    val newMemberId = MemberSql.createUser(freemudMember, memberCode)
    val cardAccount = MemberSql.getCardAccountByMemberId(newMemberId)
    if (cardAccount.isEmpty) {
      MemberSql.createMemberAccount(newMemberId.toInt, freemudMember.MemberName, freemudMember.LeftValue)
    }
  }

//  /**
//    * 同步会员相关联的订单
//    *
//    * @param freemudMemberId
//    */
//  def syncMemberRelatedOrder(freemudMemberId: String): Unit = {
//    val orderBases = FreemudSql.listOrderByMemberId(freemudMemberId)
//    orderBases.filter(_.StatusFlag != 1).foreach(
//      x => {
//        syncOrder(x)
//      }
//    )
//  }

  def syncIncOrder(): Unit = {
    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }
    var flag = true
    var start = 0
    while (flag) {
      val list = FreemudSql.listIncOrderBase(startTime, start, 1000)
      if (list.nonEmpty) {
        list.foreach(
          x => exec.execute(()=>{syncOrder(x)})
        )
        start = start+1000
      }
      if (list.isEmpty || list.size < 1000) {
        flag = false
      }
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        logger.info("订单主档数据全部迁移完成")
        //执行完member然后执行
        return
      }
      Thread.sleep(200);
    }
  }

  def syncIncScore(): Unit = {
    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    var start = 0
    var flag = true
    val limit = 1000
    while (flag) {
      //根据最后更新时间获取最新订单，并过滤未支付的数据
      val list = FreemudSql.listIncScoreHistory(startTime, start, limit)
      if (list.nonEmpty) {
        list.foreach(
          x => exec.execute(()=>{syncScore(x)})
        )
//        syncMemberScore(list.map(_.MemberId).distinct)
        start = start+ limit
      }
      if (list.isEmpty || list.size < limit) {
        flag = false
      }
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        logger.info("积分流水主档数据全部迁移完成")
        //执行完member然后执行
        return
      }
      Thread.sleep(200);
    }
  }

  def syncScore(scoreHistory: MemberScoreHistory): Unit = {
    val opt = MemberSql.getScoreJournalByOldScoreId(scoreHistory.ScoreId)
    if (opt.nonEmpty) {
      logger.warn(s"ScoreId=${scoreHistory.ScoreId} already sync.")
      return
    }
    val memberId = getMemberIdByOldMemberId(scoreHistory.MemberId)
    if (memberId.isEmpty) {
      logger.warn(s"ScoreId=${scoreHistory.ScoreId} memberId not existed.")
      return
    }
    MemberSql.createScoreJournal(memberId.get, scoreHistory.Score, scoreHistory.RecordType, scoreHistory.AfterScore, scoreHistory.CreateTime, scoreHistory.OrderId)
    //跟新老会员的积分信息
    val oldMember = FreemudSql.getMemberBase(scoreHistory.MemberId)
    if (oldMember.nonEmpty) {
      MemberSql.updateMemberScoreByOldMemberId(scoreHistory.MemberId, oldMember.get.Score)
    }
  }

  def syncOrder(orderOrderBase: OrderOrderBase): Unit = {
    val opt = MemberSql.getOrderByOldOrderId(orderOrderBase.OrderId)
    if (opt.nonEmpty) {
      logger.warn(s"orderId=${orderOrderBase.OrderId} already sync.")
      return
    }
    val memberId = getMemberIdByOldMemberId(orderOrderBase.MemberId)
    if (memberId.isEmpty) {
      logger.warn(s"orderId=${orderOrderBase.OrderId} memberId not existed")
      return
    }
    MemberSql.createOrder(orderOrderBase, memberId.get.toString)
  }

  def syncIncCard(): Unit = {
    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    var start = 0
    var flag = true
    val limit = 1000
    while (flag) {
      //根据最后更新时间获取最新订单，并过滤未支付的数据
      val list = FreemudSql.listIncCardBase(startTime,start,limit)
      if (list.nonEmpty) {
        list.filterNot(x => x.MemberId == null || x.MemberId.isEmpty).foreach(
          x => exec.execute(()=>{syncCard(x)})
        )
        start =start+ limit
      }
      if (list.isEmpty || list.size < limit) {
        flag = false
      }
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        logger.info("礼品卡数据全部迁移完成")
        //执行完member然后执行
        return
      }
      Thread.sleep(200);
    }

  }

  def syncCard(cardCardBase: CardCardBase): Unit = {
    val opt = MemberSql.getCardBaseByOldCardId(cardCardBase.CardId)
    if (opt.nonEmpty) {
      //如果已同步，则更新数据
      MemberSql.updateCardBase(opt.get.id, cardCardBase.StatusFlag, cardCardBase.LeftValue)
    } else {
      val member = MemberSql.getMemberByOldMemberId(cardCardBase.MemberId)
      if (member.nonEmpty) {
        MemberSql.createCardBaseNew(cardCardBase, member.get.id, CardTypeEnum.NORMAL)
      }
    }
  }

  def syncIncConsume(): Unit = {
    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    var start = 0
    var flag = true
    val limit = 1000
    while (flag) {
      //根据最后更新时间获取最新订单，并过滤未支付的数据
      val list = FreemudSql.listIncCardConsumeLog(startTime,start,limit)
      if (list.nonEmpty) {
        list.foreach(
          x => exec.execute(()=>{syncConsume(x)})
        )
        //同步流水完成后，同步更新每一个账户的余额
        //syncMemberBalance(list.map(_.MemberId).distinct)
        start = start+ limit
      }
      if (list.isEmpty || list.size < limit) {
        flag = false
      }
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        logger.info("礼品卡数据全部迁移完成")
        //执行完member然后执行
        return
      }
      Thread.sleep(200);
    }
  }

  /**
    * 同步更新会员账户余额
    *
    * @param oldMemberIds
    */
  def syncMemberBalance(oldMemberIds: List[String]): Unit = {
    oldMemberIds.foreach(
      oldMemberId => {
        val oldMember = FreemudSql.getMemberBase(oldMemberId)
        val memberId = getMemberIdByOldMemberId(oldMemberId)
        if (memberId.isEmpty) {
          logger.warn(s"oldMemberId=${oldMemberId} syncMemberBalance memberId not existed.")
        } else {
          val cardAccount = MemberSql.getCardAccountByMemberId(memberId.get)
          if (cardAccount.isEmpty) {
            MemberSql.createMemberAccount(memberId.get.toInt, oldMember.get.MemberName, oldMember.get.LeftValue)
          } else {
            if (oldMember.get.LeftValue != cardAccount.get.cardBalance) {
              MemberSql.updateCardAccountBalance(memberId.get, oldMember.get.LeftValue)
            }
          }
        }
      }
    )
  }

//  /**
//    * 同步更新会员积分
//    *
//    * @param oldMemberIds
//    */
//  def syncMemberScore(oldMemberIds: List[String]): Unit = {
//    oldMemberIds.foreach(
//      oldMemberId => {
//        try {
//          val oldMember = FreemudSql.getMemberBase(oldMemberId)
//          if (oldMember.nonEmpty) {
//            MemberSql.updateMemberScoreByOldMemberId(oldMemberId, oldMember.get.Score)
//          }
//        } catch {
//          case e: Exception => logger.error("", e)
//        }
//      }
//    )
//  }

  def syncIncCoupon(): Unit = {
    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    var start = 0
    var flag = true
    val limit = 1000
    while (flag) {
      //根据最后更新时间获取最新订单，并过滤未支付的数据
      val list = FreemudSql.listIncMemberCoupon(startTime,start,limit)
      if (list.nonEmpty) {
        list.foreach(
          x => exec.execute(()=>{syncCoupon(x)})
        )
        //同步流水完成后，同步更新每一个账户的余额
        //syncMemberBalance(list.map(_.MemberId).distinct)
        start = start+ limit
      }
      if (list.isEmpty || list.size < limit) {
        flag = false
      }
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        logger.info("优惠卷数据全部迁移完成")
        //执行完member然后执行
        return
      }
      Thread.sleep(200);
    }

  }

  def syncCoupon(coupon: MemberMemberCoupon): Unit = {
    val opt = MemberSql.getCouponByOldCouponId(coupon.Id)
    if (opt.nonEmpty) {
      logger.warn(s"CouponId=${coupon.Id} already sync.")
      return
    }
    val member = MemberSql.getMemberByOldMemberId(coupon.MemberId)
    if (member.isEmpty) {
      logger.warn(s"CouponId=${coupon.Id} sync failed ,member not existed.")
      return
    }
    val couponId = MemberSql.createMemberCoupon(coupon, member.get.id)
  }

  def syncConsume(cardCardConsume: CardCardConsume): Unit = {
    val opt = MemberSql.getCardConsumeByOldConsumeId(cardCardConsume.ConsumeId)
    if (opt.nonEmpty) {
      logger.warn(s"ConsumeId=${cardCardConsume.ConsumeId} already sync.")
      return
    }
    val cardCardBase = FreemudSql.getCardCardBase(cardCardConsume.CardNo, cardCardConsume.MemberId)
    if (cardCardBase.isEmpty) {
      logger.warn(s"ConsumeId=${cardCardConsume.ConsumeId} sync failed ,cardCardBase not existed.")
      return
    }
    val orderId = getOrderIdByOldOrderId(cardCardConsume.OrderId)
    if (orderId.isEmpty) {
      logger.warn(s"ConsumeId=${cardCardConsume.ConsumeId} sync failed ,orderId not existed.")
      return
    }
    val memberId = getMemberIdByOldMemberId(cardCardConsume.MemberId)
    if (memberId.isEmpty) {
      logger.warn(s"ConsumeId=${cardCardConsume.ConsumeId} sync failed ,memberId not existed.")
      return
    }
    val cardBase = MemberSql.getCardBaseByCardNo(cardCardConsume.CardNo)
    if (cardBase.isEmpty) {
      logger.warn(s"ConsumeId=${cardCardConsume.ConsumeId} sync failed ,cardBase not existed.")
      return
    }
    val consumeId = MemberSql.createCardConsume(
      cardBase.get.id,
      orderId.get,
      memberId.get,
      cardCardConsume
    )
    if (cardCardBase.get.LeftValue != cardBase.get.cardBalance) {
      //非码礼品卡流水是Int型，精度是不准确的，所以这里通过礼品卡余额进行余额同步，保证与非码最终一致，流水和最终的账户余额可能会有一点偏差
      MemberSql.modifyCardBalance(cardBase.get.id, cardCardBase.get.LeftValue, None)
    }

    //同步会员账户余额信息
    val oldMember = FreemudSql.getMemberBase(cardCardConsume.MemberId)
    val cardAccount = MemberSql.getCardAccountByMemberId(memberId.get)
      if (cardAccount.isEmpty) {
        MemberSql.createMemberAccount(memberId.get.toInt, oldMember.get.MemberName, oldMember.get.LeftValue)
      } else {
        if (oldMember.get.LeftValue != cardAccount.get.cardBalance) {
          MemberSql.updateCardAccountBalance(memberId.get, oldMember.get.LeftValue)
        }
      }
  }

  def getMemberIdByOldMemberId(oldMemberId: String): Option[Long] = {
    val member = MemberSql.getMemberByOldMemberId(oldMemberId)
    if (member.isEmpty) {
      None
    } else {
      Some(member.get.id)
    }
  }

  def getOrderIdByOldOrderId(oldOrderId: String): Option[Long] = {
    val order = MemberSql.getOrderByOldOrderId(oldOrderId)
    if (order.isEmpty) {
      None
    } else {
      Some(order.get.id)
    }
  }

  def syncUsedCoupon(): Unit = {
    logger.info("syncUsedCoupon start.")
    try {
      val coupons = FreemudSql.listUsedMemberCoupon(startTime)
      if (coupons.nonEmpty) {
        coupons.filter(_.IsUsed == 1).foreach(
          x => {
            val optCoupon = MemberSql.getCouponByOldCouponId(x.Id)
            if (optCoupon.nonEmpty && optCoupon.get.isUsed != 1) {
              MemberSql.updateMemberCoupon(optCoupon.get.id, x.IsUsed, x.UseTime)
            }
          }
        )
      }
    } catch {
      case e: Exception => logger.error("", e)
    } finally {
      logger.info("syncUsedCoupon end.")
    }
  }
}
