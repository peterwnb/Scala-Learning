package com.today.mysql.member.sql

import java.sql.Timestamp
import java.util.concurrent.atomic.AtomicLong

import com.today.common.{GenIdUtil, MemberDataSource, RedisDataSource}
import com.today.enums.{CardStatusEnum, CardTypeEnum, MemberCardAccountStatusEnum}
import com.today.mysql.member.dto._
import com.today.mysql.member.dto.freemud._
import com.today.service.commons.`implicit`.Implicits.long2Date
import org.slf4j.LoggerFactory
import wangzx.scala_commons.sql._


object MemberSql {

  val log = LoggerFactory.getLogger(getClass);

  def updateStatus(id: Long): Unit = {
    MemberDataSource.mysqlData.executeUpdate(
      sql"""  UPDATE member_update SET status=1 WHERE id=${id}

      """)
  }

  def updateUser(member: MemberBase, memberId: String): Unit = {

    MemberDataSource.mysqlData.executeUpdate(
      sql"""  UPDATE member
            SET
           member_name=${member.MemberName},
           member_score=${member.Score},
           member_head_url=${member.Photo},
           mobile_phone=${member.Mobile},
           sex=${member.SexFlag},
           register_source=${member.ChannelFlag},
           register_city_name=${member.City},
           member_birthday=${member.Birthday},
           wechat_open_id=${member.WeixinOpenId},
           wechat_union_id="",
           alipay_open_id=${member.AliOpenId},
           updated_at= now(),
           old_memberId=${member.MemberId},
           member_address=${member.Address},
           member_account=${member.Account}

         WHERE  id=${memberId}
         """)
    //修改账户数据
    MemberDataSource.mysqlData.executeUpdate(
      sql"""UPDATE member_account
            SET
           member_balance=${member.LeftValue},
           updated_at= now()
           WHERE member_id=${memberId}
            """)

  }

  def findMemberById(memberId: String): MemberBase = {
    MemberDataSource.mysqlData.row[MemberBase](sql"""SELECT * FROM member_member_base   WHERE MemberId=${memberId} """).get
  }

  def findUpdateMemberList(): List[MemberUpdate] = {
    MemberDataSource.mysqlData.rows[MemberUpdate](sql"""  SELECT * FROM member_update   WHERE status=0    """)
  }

  def findMemberCouponByMemberId(memberId: String): List[MemberCoupon] = {
    MemberDataSource.mysqlData.rows[MemberCoupon](sql"""SELECT * FROM member_member_coupon WHERE MemberId=${memberId}    """)
  }

  def findScoreJournalLListByMemberId(memberId: String): List[MemberSource] = {
    MemberDataSource.mysqlData.rows[MemberSource](sql"""SELECT * FROM member_score_history WHERE MemberId=${memberId}   """)
  }

  def findNewMemberList(startTime: String, endTime: String): List[MemberBase] = {
    MemberDataSource.mysqlData.rows[MemberBase](sql"""SELECT * FROM member_member_base  WHERE RegisterTime>=${startTime} AND RegisterTime<=${endTime}     """)
  }

  val couponIdGen = new AtomicLong(0)

  /**
    * 参考新方法，createMemberCoupon
    *
    * @param coupon
    */
  @Deprecated
  def createUserCoupon(coupon: MemberCoupon): Unit = {
    val tm1 = System.currentTimeMillis()
    val memberId = RedisDataSource.redisData.getSet(coupon.MemberId).iterator.next()

    val tm2 = System.currentTimeMillis()
    val couponId = GenIdUtil.getId(GenIdUtil.MEMBER_COUPON)
    MemberDataSource.mysqlData.executeUpdate(
      sql"""INSERT INTO member_coupon
           SET
           member_id=${memberId},
           member_name="",
           coupon_id=${couponId},
           coupon_name=${coupon.CouponName},
           Start_Time=${coupon.StartTime},
           end_time= ${coupon.EndTime},
           coupon_remark=${coupon.Remark},
           receive_time= ${coupon.CreateTime},
           source_flag=${coupon.SourceFlag},
           Source_id=5,
           is_used=${coupon.IsUsed},
           old_coupon_id=${coupon.CouponId},
            used_time=${coupon.UseTime},
            Fk_id=${coupon.FkId},
            scan_code_id=${coupon.ScanCodeId},
            created_at= now(),
           created_by=1,
           updated_at= now(),
           updated_by=1,
           coupon_key=${coupon.CouponId},
           remark=''



        """
    )

    val tm3 = System.currentTimeMillis()

    log.info(s"createUserCoupon ${couponId} time1:${tm2 - tm1}ms time2:${tm3 - tm2}ms")

  }

  def createMemberCoupon(coupon: MemberMemberCoupon, memberId: Long): Long = {
    val couponId = GenIdUtil.getId(GenIdUtil.MEMBER_COUPON)

    val couponType = if(coupon.TypeCode.equals("CTYPE001")){4}else{1}
    val couponPrice = if(coupon.TypeCode.equals("CTYPE001")) {
      MemberDataSource.mysqlData.row[Double](
        sql"""
             SELECT PriceA FROM activity_coupon_base WHERE CouponId = ${coupon.CouponId}
           """).getOrElse(0.00)
    }else{0.00}

    MemberDataSource.mysqlData.executeUpdate(
      sql"""INSERT INTO member_coupon
           SET
           member_id=${memberId},
           member_name="",
           coupon_id=${couponId},
           coupon_name=${coupon.CouponName},
           Start_Time=${coupon.StartTime},
           end_time= ${coupon.EndTime},
           coupon_remark=${coupon.Remark},
           receive_time= ${coupon.CreateTime},
           source_flag=${coupon.SourceFlag},
           Source_id=5,
           is_used=${coupon.IsUsed},
           old_coupon_id=${coupon.Id},
           used_time=${coupon.UseTime},
           Fk_id=${coupon.FkId},
           scan_code_id=${coupon.ScanCodeId},
           created_at= ${coupon.CreateTime},
           created_by=1,
           updated_at= now(),
           updated_by=1,
           coupon_key=${coupon.CouponId},
           coupon_type = ${couponType},
           coupon_price = ${couponPrice},
           remark=''
        """
    )
    couponId
  }

  def findMemberCoupon(start: Int, end: Int): List[MemberCoupon] = {
    MemberDataSource.mysqlData.rows[MemberCoupon](sql"""SELECT * FROM member_member_coupon LIMIT ${start},${end}    """)
  }


  def findScoreJournalLList(start: Int, end: Int): List[MemberSource] = {
    MemberDataSource.mysqlData.rows[MemberSource](sql"""SELECT * FROM member_score_history LIMIT ${start},${end}    """)
  }

  def createScoreJournal(source: MemberScoreHistory, newMemberId: Long, newOrderId: Long): Long = {

    MemberDataSource.mysqlData.generateKey[Long](
      sql"""INSERT INTO score_journal
           SET
           member_id=${newMemberId},
           score_price=${source.Score},
           order_id=${newOrderId},
           resouce_type=${source.RecordType},
           score_last=${source.AfterScore},
           created_at= now(),
           created_by=1,
           updated_at= ${source.CreateTime},
           updated_by=1,
           remark='',
           old_order_id=${source.OrderId},
           old_score_id=${source.ScoreId}
        """
    )
  }


  def createScoreJournal(memberId: Long, score: Int, resourceType: Int, scoreLast: Int, createTime: java.sql.Timestamp, oldOrderId: String): Long = {
    MemberDataSource.mysqlData.generateKey[Long](
      sql"""INSERT INTO score_journal
           SET
           member_id=${memberId},
           score_price=${score},
           order_id=1,
           resouce_type=${resourceType},
           score_last=${scoreLast},
           created_at= now(),
           created_by=1,
           updated_at= ${createTime},
           updated_by=1,
           remark='',
           old_order_id=${oldOrderId}
        """
    )
  }

  def findMemberAccountList(start: Int, end: Int): List[MemberAccountBase] = {
    MemberDataSource.mysqlData.rows[MemberAccountBase](sql"""SELECT * FROM member_consume_log   LIMIT ${start},${end}   """)
  }

  def findMemberAccountListByMemberId(memberId: String): List[MemberAccountBase] = {
    MemberDataSource.mysqlData.rows[MemberAccountBase](sql"""SELECT * FROM member_consume_log   WHERE  MemberId=${memberId}  """)
  }

  def createAccountStream(account: MemberAccountBase): Unit = {
    val memberId = RedisDataSource.redisData.getSet(account.MemberId).iterator.next()
    //    val memberId = 100 // fixme
    MemberDataSource.mysqlData.executeUpdate(
      sql"""INSERT INTO member_account_journal
            SET
           member_id=${memberId},
           member_name='',
           order_id='1',
           consume_amount=${account.ConsumeValue},
           last_balance=0,
           old_order_id=${account.OrderId},
           old_order_no=${account.OrderNo},
           consume_code=${account.ConsumeCode},
           created_at= now(),
           created_by=1,
           updated_at= now(),
           updated_by=1,
           remark=''
            """)
  }

  def createAccountJournal(log: MemberConsumeLog, newMemberId: Long, newOrderId: Long): Unit = {
    MemberDataSource.mysqlData.executeUpdate(
      sql"""INSERT INTO member_account_journal
            SET
           member_id=${newMemberId},
           member_name='',
           order_id='1',
           consume_amount=${log.ConsumeValue},
           last_balance=0,
           old_order_id=${newOrderId},
           old_order_no=${log.OrderNo},
           consume_code=${log.ConsumeCode},
           created_at= now(),
           created_by=1,
           updated_at= now(),
           updated_by=1,
           remark=''
            """)
  }


  /**
    * 创建账户
    *
    * @param memberId
    * @param MemberName
    * @param LeftValue
    * @return
    */
  def createMemberAccount(memberId: Int, MemberName: Option[String], LeftValue: Double): Unit = {
    //使用取号服务生成card_account的主键ID
    //val id = GenIdUtil.getId(GenIdUtil.MEMBER_CARD_ACCOUNT)
    MemberDataSource.mysqlData.generateKey[Long](
      sql"""INSERT INTO member_card_account
            SET
           member_id=${memberId},
           member_name=${MemberName.getOrElse("")},
           card_balance=${LeftValue},
           card_account_status=1,
           created_at= now(),
           created_by=1,
           updated_at= now(),
           updated_by=1,
           remark=''
            """)

  }


  /**
    * 创建用户
    *
    * @param member
    * @return
    */
  def createUser(member: MemberBase, memberCode: Long): Int = {
    val memberName = if(member.MemberName.isEmpty){""}else{member.MemberName.get}
    //Birthday 默认值 --> '1970-01-07 18:40:19'
    val id = MemberDataSource.mysqlData.generateKey[Long](
      sql"""INSERT INTO member
            SET
           member_code=${memberCode},
           member_name=${memberName},
           member_password=${member.Password.getOrElse("")},
           member_score=${member.Score},
           member_head_url=${member.Photo},
           member_salt="1",
           mobile_phone=${member.Mobile.getOrElse("")},
           sex=${member.SexFlag},
           member_status=1,
           member_type=1,
           register_source=${member.ChannelFlag},
           register_city_id=0,
           register_city_name=${member.City.getOrElse("")},
           member_birthday=${member.Birthday.getOrElse(new Timestamp(0))},
           register_store_id=0,
           register_time=${member.RegisterTime},
           email="",
           wechat_open_id=${member.WeixinOpenId},
           wechat_union_id="",
           alipay_open_id=${member.AliOpenId},
           last_login_time=now(),
           created_at= now(),
           created_by=1,
           updated_at= now(),
           updated_by=1,
           remark='',
           is_new=0,
           old_memberId=${member.MemberId},
           member_address=${member.Address.getOrElse("")},
           member_account=${member.Account}
         """)
    id.toInt
  }


  def findMemberList(start: Int, end: Int): List[MemberBase] = {
    MemberDataSource.mysqlData.rows[MemberBase](sql"""SELECT * FROM member_member_base ORDER BY RegisterTime   LIMIT ${start},${end} """)

    //MemberDataSource.mysqlData.rows[MemberBase](sql"""select * from member_member_base  where MemberId='0ca50617-9baf-4320-858a-ff597bfb48f5' """)
  }


  def main(args: Array[String]): Unit = {
    val site = Set("Runoob", "Google", "Baidu")
    println("第一网站是 : " + site.head)
  }

  def getMember(memberId: Long): Option[Member] = {
    MemberDataSource.mysqlData.row[Member](
      sql"""
           SELECT * FROM member WHERE id=${memberId}
         """)
  }

  def listIncMember(referTime: java.sql.Timestamp): List[Member] = {
    MemberDataSource.mysqlData.rows[Member](
      sql"""
           SELECT * FROM member WHERE register_time>=${referTime} ORDER BY register_time
         """)
  }

  def getMemberOrder(id: Long): Option[MemberOrder] = {
    MemberDataSource.mysqlData.row[MemberOrder](
      sql"""
           SELECT * FROM member_order WHERE id=${id}
         """)
  }

  def getCardConsume(id: Long): Option[CardConsume] = {
    MemberDataSource.mysqlData.row[CardConsume](
      sql"""
           SELECT * FROM card_consume WHERE id=${id}
         """)
  }

  def getScoreJournal(id: Long): Option[ScoreJournal] = {
    MemberDataSource.mysqlData.row[ScoreJournal](
      sql"""
           SELECT * FROM score_journal WHERE id=${id}
         """)
  }

  def getMemberCoupon(id: Long): Option[Coupon] = {
    MemberDataSource.mysqlData.row[Coupon](
      sql"""
           SELECT * FROM member_coupon WHERE id=${id}
         """)
  }

  def listIncOrder(referTime: java.sql.Timestamp): List[MemberOrder] = {
    //todo 订单状态需要确认，只查询最终订单状态
    MemberDataSource.mysqlData.rows[MemberOrder](
      sql"""
           SELECT * FROM member_order WHERE update_time>=${referTime} AND order_status IN (2,3,4) ORDER BY update_time
         """)
  }

  def listIncCardConsume(referTime: java.sql.Timestamp): List[CardConsume] = {
    MemberDataSource.mysqlData.rows[CardConsume](
      sql"""
           SELECT * FROM card_consume WHERE created_at>=${referTime} ORDER BY created_at
         """)
  }

  def listIncScoreJournal(referTime: java.sql.Timestamp): List[ScoreJournal] = {
    MemberDataSource.mysqlData.rows[ScoreJournal](
      sql"""
           SELECT * FROM score_journal WHERE created_at>=${referTime} ORDER BY created_at
         """)
  }

  def listIncMemberCoupon(referTime: java.sql.Timestamp): List[Coupon] = {
    MemberDataSource.mysqlData.rows[Coupon](
      sql"""
           SELECT * FROM member_coupon WHERE created_at>=${referTime} ORDER BY created_at
         """)
  }

  /**
    * 查询礼品卡获取账户余额
    *
    * @param memberId
    * @return
    */
  def getCardTotalBalance(memberId: Long): Option[Double] = {
    val querySql = sql""" SELECT sum(card_balance) FROM card_base WHERE member_id = ${memberId} AND status = 6 AND end_date > now() """
    MemberDataSource.mysqlData.row[Double](querySql)
  }

  def getCardAccountByMemberId(memberId: Long): Option[MemberCardAccount] = {
    MemberDataSource.mysqlData.row[MemberCardAccount](
      sql"""
           SELECT * FROM member_card_account WHERE member_id=${memberId}
         """)
  }

  @Deprecated
  def createCardAccount(memberId: Long, status: MemberCardAccountStatusEnum, remark: Option[String]): Int = {
    val balance = getCardTotalBalance(memberId)
    val id = GenIdUtil.getId(GenIdUtil.MEMBER_CARD_ACCOUNT)
    val member = getMember(memberId)
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           INSERT INTO member_card_account
                       SET
                      id=${id},
                      member_id=${memberId},
                      member_name=${member.get.memberName},
                      card_balance=${balance},
                      card_account_status=${status.getVal},
                      created_at=now(),
                      created_by=1,
                      updated_at= now(),
                      updated_by=1,
                      remark=${remark.getOrElse("")}
      """
    )
  }

  /**
    * 通过openid/手机号，发放礼品卡到用户
    * card_base与用户绑定之后才会有数据，所以这里是插入操作
    */
  def createCardBaseNew(card: CardCardBase, memberId: Long, cardType: CardTypeEnum): Long = {
    //采用主键自动生成
    //val cardId = GenIdUtil.getId(GenIdUtil.CARD_BASE)

    val mappingStatus = card.StatusFlag match {
      case 1 => CardStatusEnum.TO_BE_RECEIVED_OLD
      case 2 => CardStatusEnum.TO_BE_SALE_OLD
      case 3 => CardStatusEnum.HAS_BE_CONVERT
      case 4 => CardStatusEnum.USE_FINISHED
      case 5 => CardStatusEnum.TO_BE_LOSS_OLD
      case 6 => CardStatusEnum.BLACK_CARD
    }
    val endDate = if(card.EndDate==null){new Timestamp(2145888000000L) }else{card.EndDate}
    val startDate = if(card.StartDate==null){new Timestamp(0) }else{card.StartDate}
    //插入数据到礼品卡基础表
    MemberDataSource.mysqlData.generateKey[Long](
      sql"""
           INSERT INTO card_base
               SET
             `card_no` = ${card.CardNo},
             `face_value` = ${card.FaceValue},
             `card_cost` = 0,
             `status` = ${mappingStatus.getVal},
             `created_by` = ${Some(1)},
             `created_at` = now(),
             `updated_by` = ${Some(1)},
             `updated_at` = now(),
             `member_id` = ${memberId},
             `card_balance` = ${card.LeftValue},
             `start_date` = ${startDate},
             `end_date` = ${endDate},
             `sale_value` = ${card.SaleValue},
             `password` = ${card.Password},
             `card_type` = ${cardType.getVal},
             `old_card_id` = ${card.CardId}
         """
    )
  }

  def updateCardBase(cardId: Long, status: Int, balance: Double): Unit = {

    val mappingStatus = status match {
      case 1 => CardStatusEnum.TO_BE_RECEIVED_OLD
      case 2 => CardStatusEnum.TO_BE_SALE_OLD
      case 3 => CardStatusEnum.HAS_BE_CONVERT
      case 4 => CardStatusEnum.USE_FINISHED
      case 5 => CardStatusEnum.TO_BE_LOSS_OLD
      case 6 => CardStatusEnum.BLACK_CARD
    }
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           UPDATE card_base SET status=${mappingStatus.getVal},card_balance=${balance},updated_at=now() WHERE id=${cardId}
         """)
  }


  /**
    * 更新礼品卡账户余额
    */
  def updateCardAccountBalance(memberId: Long, newBalance: Double): Int = {
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           UPDATE member_card_account
                       SET
                      card_balance = ${newBalance}
                      WHERE member_id=${memberId}
      """
    )
  }

  /**
    * 修改礼品卡余额
    */
  def modifyCardBalance(cardId: Long, balance: Double, status: Option[CardStatusEnum]): Unit = {
    val sql = status match {
      case Some(statusEnum) => sql" UPDATE card_base SET status = ${statusEnum.getVal}, card_balance = ${balance} WHERE id = ${cardId} "
      case None => sql" UPDATE card_base SET card_balance = ${balance} WHERE id = ${cardId} "
    }
    MemberDataSource.mysqlData.executeUpdate(sql)
  }

  /**
    * 插入礼品卡使用流水
    */
  def createCardConsume(cardId: Long, orderId: Long, memberId: Long, cardCardConsume: CardCardConsume): Long = {
    //val cardConsumeId = GenIdUtil.getId(GenIdUtil.CARD_CONSUME)
    //新库流水是正数充值负数消费，非码库ConsumeCode为CONSUME001时为消费，需要转换为负数，ConsumeCode为其他值时，为退款或充值
    val consumeValue = if (cardCardConsume.ConsumeCode.equals("CONSUME001")) -cardCardConsume.ConsumeValue else cardCardConsume.ConsumeValue
    MemberDataSource.mysqlData.generateKey[Long](
      sql"""
           INSERT INTO card_consume
           SET
             `card_id` = ${cardId},
             `order_id` = ${orderId},
             `consume_amount` = ${consumeValue},
             `last_balance` = 0,
             `created_by` = 1,
             `created_at` = now(),
             `member_id` = ${memberId},
             `card_no` = ${cardCardConsume.CardNo},
             old_consume_id=${cardCardConsume.ConsumeId}
         """
    )
  }

  def getCardBase(cardId: Long): Option[CardBase] = {
    MemberDataSource.mysqlData.row[CardBase](
      sql"""
           SELECT * FROM card_base WHERE id=${cardId}
         """)
  }


  def getCardBaseByCardNo(cardNo: String): Option[CardBase] = {
    MemberDataSource.mysqlData.row[CardBase](
      sql"""
           SELECT * FROM card_base WHERE card_no=${cardNo}
         """)
  }

  def createOrder(orderOrderBase: OrderOrderBase, newMemberId: String): Long = {
    //val orderId = GenIdUtil.getId(GenIdUtil.MEMBER_ORDER)
    MemberDataSource.mysqlData.generateKey[Long](
      sql"""
          INSERT INTO member_order
          SET
          order_no=${orderOrderBase.OrderNo},
          order_type=${orderOrderBase.TypeFlag},
          order_status=${orderOrderBase.StatusFlag},
          should_pay=${orderOrderBase.ShouldPay},
          actual_pay=${orderOrderBase.ActualPay},
          card_pay=${orderOrderBase.CardPay},
          cash_pay=${orderOrderBase.CashPay},
          code_pay=${orderOrderBase.CodePay},
          remark=${orderOrderBase.Remark},
          operatorId=${orderOrderBase.OperatorId},
          refund_amount=${orderOrderBase.RefundAmount},
          transId=${orderOrderBase.TransId},
          third_trans_id = ${orderOrderBase.OutTradeNo},
          score_pay=${orderOrderBase.ScorePay},
          pos_code=${orderOrderBase.PosCode},
          discount_amount=${orderOrderBase.DiscountAmount},
          pay_channel_code=${orderOrderBase.PayChannelCode},
          business_date=${orderOrderBase.BusinessDate},
          ali_openId=${orderOrderBase.AliOpenId},
          update_time=${orderOrderBase.UpdateTime},
          member_id=${newMemberId},
          old_order_id = ${orderOrderBase.OrderId}
         """
    )
  }

  def updateMemberCoupon(couponId: Long, isUsed: Int, usedTime: java.sql.Timestamp): Unit = {
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           UPDATE member_coupon SET is_used=${isUsed},usedTime=${usedTime},updated_at=now() WHERE coupon_id=${couponId}
         """)
  }

  def cleanData(): Unit = {
    MemberDataSource.mysqlData.executeUpdate(sql"""TRUNCATE TABLE member""")
    MemberDataSource.mysqlData.executeUpdate(sql"""TRUNCATE TABLE member_card_account""")
    MemberDataSource.mysqlData.executeUpdate(sql"""TRUNCATE TABLE member_coupon""")
    MemberDataSource.mysqlData.executeUpdate(sql"""TRUNCATE TABLE card_base""")
    MemberDataSource.mysqlData.executeUpdate(sql"""TRUNCATE TABLE card_consume""")
    MemberDataSource.mysqlData.executeUpdate(sql"""TRUNCATE TABLE member_inc_sync_log""")
    MemberDataSource.mysqlData.executeUpdate(sql"""TRUNCATE TABLE score_journal""")
    MemberDataSource.mysqlData.executeUpdate(sql"""TRUNCATE TABLE member_order""")
  }

  def getMemberByOldMemberId(oldMemberId: String): Option[Member] = {
    MemberDataSource.mysqlData.row[Member](
      sql"""
           SELECT * FROM member WHERE old_memberId=${oldMemberId}
         """)
  }

  def getOrderByOldOrderId(oldOrderId: String): Option[MemberOrder] = {
    MemberDataSource.mysqlData.row[MemberOrder](
      sql"""
           SELECT * FROM member_order WHERE old_order_id=${oldOrderId}
         """)
  }

  def getCardBaseByOldCardId(oldCardId: String): Option[CardBase] = {
    MemberDataSource.mysqlData.row[CardBase](
      sql"""
           SELECT * FROM card_base WHERE old_card_id=${oldCardId}
         """)
  }

  def getCouponByOldCouponId(oldCouponId: String): Option[Coupon] = {
    MemberDataSource.mysqlData.row[Coupon](
      sql"""
           SELECT * FROM member_coupon WHERE old_coupon_id=${oldCouponId}
         """)
  }

  def getScoreJournalByOldScoreId(oldScoreId: String): Option[ScoreJournal] = {
    MemberDataSource.mysqlData.row[ScoreJournal](
      sql"""
           SELECT * FROM score_journal WHERE old_score_id=${oldScoreId}
         """)
  }

  def getCardConsumeByOldConsumeId(oldConsumeId: String): Option[CardConsume] = {
    MemberDataSource.mysqlData.row[CardConsume](
      sql"""
           SELECT * FROM card_consume WHERE old_consume_id=${oldConsumeId}
         """)
  }

  def updateMemberScoreByOldMemberId(oldMemberId: String, score: Int): Unit = {
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           UPDATE member SET member_score=${score},updated_at=now() WHERE old_memberId=${oldMemberId}
         """)
  }
}
