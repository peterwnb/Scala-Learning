package com.today.mysql.member.sql

import com.today.common.MemberDataSource
import com.today.enums.{BackTypeEnum, LogSourceEnum, MemberSourceEnum}
import com.today.mysql.member.dto._
import com.today.mysql.member.dto.freemud.MemberMemberChannel
import org.slf4j.LoggerFactory
import wangzx.scala_commons.sql._

object MemberWashSql {
  val log = LoggerFactory.getLogger(getClass);

  /**
    * 查询所有手机号重复的会员列表信息
    */
  def findDuplicatePhoneMember(): List[Member] ={
    //多个会员有相同的⼿手机号，除11个1之外
    MemberDataSource.mysqlData.rows[Member](sql""" select * from member where mobile_phone in (select mobile_phone from member WHERE delete_flag = 0 AND mobile_phone <> '11111111111' AND mobile_phone <> '' group by mobile_phone having count(mobile_phone) > 1) """)
  }

  /**
    * 查询所有的openId重复的数据
    */
  def findDuplicateOpenIdMember(): List[Member] ={
    MemberDataSource.mysqlData.rows[Member](
      sql"""
           select
            *
           from
              member m
           where
              wechat_open_id in
           (
           	   select open_id from tmp_duplicate_openids
           )
         """
    )
  }

  /**
    * 查询所有的openId重复的数据
    */
  def findDuplicateOpenIdMemberNew(): List[MemberAccount] ={
    MemberDataSource.mysqlData.rows[MemberAccount](
      sql"""
           select
             m.id as member_id,
             m.old_memberId as old_member_id,
             m.mobile_phone,
           	 m.member_name,
             m.member_head_url,
             m.wechat_open_id as openId,
             m.member_score,
             ma.id as account_id,
             ma.card_balance,
            (select count(*) as coupon_count from member_coupon c where c.member_id = m.id and c.is_used = 0) as coupon_count
           from
              member m
           left JOIN member_card_account ma on m.id = ma.member_id
           where
              wechat_open_id in
           (
           	   select open_id from tmp_duplicate_openids
           )
         """
    )
  }

  def backMainMember(targetId:Long): Unit ={
    //目标只能一次执行
    //记录备份表，原记录 和 目标记录  都需要 备份
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           INSERT INTO member_bak SELECT  * FROM member WHERE id = ${targetId}
         """
    )
  }

  /**
    * 合并会员主档信息
    */
  def mergeMember(targetId:Long , sourceId:Long): Unit ={
    //合并之前的对象
    val m = MemberDataSource.mysqlData.rows[Member](sql""" SELECT * FROM member WHERE id = ${sourceId}""").head;

    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           INSERT INTO member_bak SELECT  * FROM member WHERE id = ${sourceId}
         """
    )

    //积分合并，积分累加到target对象
    MemberDataSource.mysqlData.executeUpdate(
      sql"""UPDATE member SET member_score = member_score + ${m.memberScore} WHERE id =${targetId}"""
    )
    //标记原纪录为待删除标记
    MemberDataSource.mysqlData.executeUpdate(
      sql"""UPDATE member SET delete_flag = 1,member_score = 0.0 WHERE id =${sourceId}"""
    )
  }

  def backMemberCardAccountMain(targetId:Long): Unit ={
    //目标只能一次执行
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           INSERT INTO member_card_account_bak SELECT  * FROM member_card_account WHERE member_id = ${targetId}
         """
    )
  }

  /**
    * 将sourceId对应的会员卡余额累加到targetId对应的会员余额中
    * @param targetId 目标会员ID
    * @param sourceId 源会员ID
    */
  def mergeMemberCardAccount(targetId:Long , sourceId:Long): Unit ={

    //备份源
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           INSERT INTO member_card_account_bak SELECT  * FROM member_card_account WHERE member_id = ${sourceId}
         """
    )

    //将sourceId对应的会员卡余额累加到targetId对应的会员余额中
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           UPDATE member_card_account m2, (SELECT SUM(card_balance) AS balance FROM member_card_account m WHERE m.member_id = ${sourceId}) m3 SET m2.card_balance = m2.card_balance + m3.balance WHERE	m2.member_id = ${targetId}
         """
    )

    //将源会员的余额更新为0
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           UPDATE member_card_account SET card_balance = 0.0 WHERE member_id = ${sourceId}
         """
    )
  }

  /**
    * 将 source会员的 优惠券 合并到 target会员下面
    * @param targetId
    * @param sourceId
    */
  def mergeCoupon(targetId:Long , sourceId:Long): Unit ={
    val m:Option[Coupon] = MemberDataSource.mysqlData.row[Coupon](sql""" SELECT * FROM member_coupon WHERE member_id = ${targetId} limit 1""");
    if(m.nonEmpty){
      //备份
      MemberDataSource.mysqlData.executeUpdate(
        sql"""
             INSERT INTO record_bak (record_id,member_id,tag) SELECT id,member_id ,${BackTypeEnum.MEMBER_COUPON.getVal} AS tag FROM member_coupon WHERE  member_id = ${sourceId}
           """
      )
      MemberDataSource.mysqlData.executeUpdate(
        sql"""UPDATE member_coupon set member_id = ${m.get.memberId},member_name=${m.get.memberName} WHERE member_id =${sourceId}"""
      )

    }
  }

  /**
    * 合并会员礼品卡
    * @param targetId 目标对象
    * @param sourceId 源对象
    */
  def mergeMemberCard(targetId:Long , sourceId:Long): Unit ={
    //将源ID更新为目标ID

    //备份
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
             INSERT INTO record_bak (record_id,member_id,tag) SELECT id,member_id ,${BackTypeEnum.CARD_BASE.getVal} as tag FROM card_base WHERE  member_id = ${sourceId}
           """
    )

    MemberDataSource.mysqlData.executeUpdate(
      sql"""UPDATE card_base set member_id = ${targetId} WHERE member_id =${sourceId}"""
    )
  }

  /**
    * 合并会员订单
    * @param targetId：目标会员ID
    * @param sourceId：源ID
    */
  def mergeMemberOrder(targetId:Long , sourceId:Long): Unit ={
    //备份
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
             INSERT INTO record_bak (record_id,member_id,tag) SELECT id,member_id ,${BackTypeEnum.MEMBER_ORDER.getVal} as tag FROM member_order WHERE member_id = ${sourceId}
           """
    )
    //将源ID更新为目标ID
    MemberDataSource.mysqlData.executeUpdate(
      sql"""UPDATE member_order set member_id = ${targetId} WHERE member_id =${sourceId}"""
    )
  }

  /**
    * 合并礼品卡消费流水
    * @param targetId：目标会员ID
    * @param sourceId：源ID
    */
  def mergeCardConsume(targetId:Long , sourceId:Long): Unit ={
    //备份
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
             INSERT INTO record_bak (record_id,member_id,tag) SELECT id,member_id ,${BackTypeEnum.CARD_CONSUME.getVal} as tag FROM card_consume WHERE member_id = ${sourceId}
           """
    )
    //将源ID更新为目标ID
    MemberDataSource.mysqlData.executeUpdate(
      sql"""UPDATE card_consume set member_id = ${targetId} WHERE member_id =${sourceId}"""
    )
  }

  /**
    * 合并积分流水
    * @param targetId：目标会员ID
    * @param sourceId：源ID
    */
  def mergeScoreJournal(targetId:Long , sourceId:Long): Unit ={
    //备份
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
             INSERT INTO record_bak (record_id,member_id,tag) SELECT id,member_id ,${BackTypeEnum.SCORE_JOURNAL.getVal} as tag FROM score_journal WHERE member_id = ${sourceId}
           """
    )
    //将源ID更新为目标ID
    MemberDataSource.mysqlData.executeUpdate(
      sql"""UPDATE score_journal set member_id = ${targetId} WHERE member_id =${sourceId}"""
    )
  }

  def markChannelForDelete(id:Long,deleteType:Int): Unit ={
    //标记数据为待删除
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           UPDATE member_member_channel SET delete_type = ${deleteType} WHERE Id = ${id}
         """
    )
  }

  /**
    * 批量删除无用的channels
    */
  def deleteChannels(): Unit ={
    //再删除该数据
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           DELETE FROM member_member_channel WHERE delete_type <> 0
         """
    )
  }

  /**
    * 将待删除的channel记录备全部移动到bak表
    */
  def moveDeletingChannelToBak(): Unit ={
    //先备份该条数据
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           INSERT INTO
               member_channel_bak
           SELECT * from member_member_channel WHERE delete_type <> 0
         """
    )
  }

  /**
    * 添加openId -> oldMemberId 的关系映射
    * @param openId
    * @param oldMemberId
    */
  def addOpenIdToMemberIdRelation(openId:String,oldMemberId:String,source:String): Unit ={
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           INSERT INTO openid_mid_map SET open_id = ${openId},old_member_id = ${oldMemberId},source = ${source}
         """)
  }

  /**
    * 判断openId -> oldMemberId 的关系是否已经存在
    * @param openId
    * @param oldMemberId
    * @return
    */
  def isOpenIdToMemberIdRelationExist(openId:String,oldMemberId:String):Int = {
    MemberDataSource.mysqlData.row[Int](     sql"""
            SELECT count(*) FROM openid_mid_map WHERE open_id = ${openId} AND  old_member_id = ${oldMemberId}
         """).getOrElse(0)
  }

  /**
    * 根据OldMemberId查询member表
    * @param oldMemberId
    * @return
    */
  def findMemberByOldMemberId(oldMemberId : String):Option[Member] = {
    MemberDataSource.mysqlData.row[Member](
      sql"""
           SELECT
           	*
           FROM
           	member
           WHERE
           	old_memberId = ${oldMemberId}
      """
    )
  }

  /**
    * 根据oldMemberId更新OpenId的值
    * @param oldMemberId
    * @param openId
    */
  def updateMemberForOpenId(oldMemberId:String,openId:String):Unit = {
    MemberDataSource.mysqlData.executeUpdate(
      sql""" UPDATE  member SET wechat_open_id = ${openId} WHERE old_memberId = ${oldMemberId}"""
    )
  }

  /**
    * 查询wechat_open_id重复的记录
    * @return
    */
  def findDuplicateOpenIdList(): List[Member] = {
    MemberDataSource.mysqlData.rows[Member](
      sql"""
           SELECT
           	*
           FROM
           	member
           WHERE
           	wechat_open_id IN (
           		| SELECT
                  wechat_open_id
                FROM
                  member
                WHERE
                  wechat_open_id IS NOT NULL
                AND
                  wechat_open_id <> ''
                AND
                  wechat_open_id <> '----'
                GROUP BY
                  wechat_open_id
                HAVING
                  count(wechat_open_id) > 1
           	)
      """
    )
  }


  def findDuplicateOpenIdListFromChannel():List[MemberMemberChannel] = {
    MemberDataSource.mysqlData.rows[MemberMemberChannel](
      sql"""
           SELECT
           	*
           FROM
           	member_member_channel
           WHERE
           	OpenId IN (
           		  SELECT
                  OpenId
                FROM
                   member_member_channel
                WHERE
                   length(OpenId) > 26
                GROUP BY
                  OpenId
                HAVING
                  count(OpenId) > 1
           	)
      """
    )
  }

  /**
    * 判断openId -> memberId 的关系是否已经存在余channel表
    * @param openId
    * @param memberId
    * @return
    */
  def isOpenIdWithMemberIdExistInChannel(openId:String,memberId:String):Int = {
    MemberDataSource.mysqlData.row[Int](
      sql"""
            SELECT count(*) FROM openid_mid_map WHERE open_id = ${openId} AND  mid = ${memberId}
         """).getOrElse(0)
  }


  /**
    * 判断openId -> oldMemberId 的关系是否已经存在余channel表
    * @param openId
    * @param memberId
    * @return
    */
  def isOpenId2MemberIdExistInMapperTable(openId:String,memberId:Long):Int = {
    MemberDataSource.mysqlData.row[Int](
      sql"""
            SELECT count(*) FROM openid_mid_map WHERE open_id = ${openId} AND  mid = ${memberId}
         """).getOrElse(0)
  }

  /**
    * 插入新生成的channel记录
    */
  def insertChannel(memberId:String,openId:String): Unit ={
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
            INSERT  INTO
              member_member_channel
            SET
              Id = ( SELECT id FROM ( SELECT MAX(Id) + 1 AS id FROM member_member_channel) as a ),
              PartnerId="",
              MemberId=${memberId},
              ChannelCode="",
              OpenId=${openId},
              delete_type=0
         """
    )
  }

  /**
    * 删除映射关系
    */
  def deleteMapperRel(memberId:Long,openId:String): Unit ={
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
            DELETE FROM openid_mid_map WHERE open_id = ${openId} AND mid = ${memberId}
         """
    )
  }

  /**
    * 插入新生成的映射关系
    */
  def insertMapperTable(map:OpenIdMidMap): Unit ={
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
            INSERT  INTO
              openid_mid_map
            SET
              open_id=${map.openId},
              mid=${map.mid},
              source=${map.source},
              old_member_id=${map.oldMemberId},
         """
    )
  }

  def insertMergeLog(target:MemberAccount,sourceMemberIds:String): Long ={
    MemberDataSource.mysqlData.generateKey[Long](
      sql"""
            INSERT  INTO
              log_member_merge
            SET
              open_id=${target.openId},
              member_id=${target.memberId},
              source_member_ids=${sourceMemberIds},
              member_score=${target.memberScore},
              coupon_count=${target.couponCount},
              card_balance=${target.cardBalance},
              account_id=${target.accountId}
         """
    )
  }

  def insertMergeLogRel(ma:MemberAccount,logId:Long ): Unit ={
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
            INSERT  INTO
              log_merge_rel
            SET
              merge_log_id=${logId},
              open_id=${ma.openId},
              member_id=${ma.memberId},
              card_balance=${ma.cardBalance},
              coupon_count=${ma.couponCount},
              member_score=${ma.memberScore},
              account_id=${ma.accountId}
         """
    )
  }

  /**
    * 备份要合并的记录类型的数据
    * @param recordId
    * @param memberId
    * @param tag
    */
  def backupRecord(recordId:Long,memberId:Long,tag:Int): Unit ={
    MemberDataSource.mysqlData.executeUpdate(
      sql"""
           INSERT INTO record_bak
           SET
           record_id = ${recordId},
           member_id=${memberId},
           tag=${tag}
         """
    )
  }

  def getBackRecord(): List[RecordBak] ={
    MemberDataSource.mysqlData.rows[RecordBak](
      sql"""
            SELECT * FROM record_bak

         """
    )
  }

  def updateRecord(record:RecordBak): Unit ={
    println(record.tag+"----------------")
    record.tag match {
      case 1 => MemberDataSource.mysqlData.executeUpdate(sql""" UPDATE  card_base SET  member_id = ${record.memberId} WHERE  id = ${record.recordId}""")
      case 2 => MemberDataSource.mysqlData.executeUpdate(sql""" UPDATE  member_coupon SET  member_id = ${record.memberId} WHERE  id = ${record.recordId}""")
      case 3 =>MemberDataSource.mysqlData.executeUpdate(sql""" UPDATE  member_order SET  member_id = ${record.memberId} WHERE  id = ${record.recordId}""")
      case 4 =>MemberDataSource.mysqlData.executeUpdate(sql""" UPDATE  card_consume SET  member_id = ${record.memberId} WHERE  id = ${record.recordId}""")
      case 5 => MemberDataSource.mysqlData.executeUpdate(sql""" UPDATE  score_journal SET  member_id = ${record.memberId} WHERE  id = ${record.recordId}""")
      case _ =>{}
    }
  }

  def main(args: Array[String]): Unit = {
    println( BackTypeEnum.CARD_BASE.getVal)
  }

  def cleanRecord(): Unit ={
    MemberDataSource.mysqlData.executeUpdate(sql""" DELETE FROM record_bak """)
  }

  def getMemberBack(): List[Member] ={
    MemberDataSource.mysqlData.rows[Member](
      sql"""
            SELECT * FROM member_bak

         """
    )
  }

  def rollbackMember(m:Member): Unit ={
    MemberDataSource.mysqlData.executeUpdate(sql"""  UPDATE member SET member_score = ${m.memberScore},delete_flag = 0  WHERE id = ${m.id}""")
  }

  def cleanMemberBak(): Unit ={
    MemberDataSource.mysqlData.executeUpdate(sql""" DELETE FROM member_bak """)
  }

  def getMemberAccountBack(): List[MemberCardAccount] ={
    MemberDataSource.mysqlData.rows[MemberCardAccount](
      sql"""
            SELECT * FROM member_card_account_bak

         """
    )
  }

  def rollbackAccount(mc:MemberCardAccount): Unit ={
    MemberDataSource.mysqlData.executeUpdate(sql"""  UPDATE member_card_account SET card_balance = ${mc.cardBalance}  WHERE id = ${mc.id}""")
  }

  def cleanAccountBak(): Unit ={
    MemberDataSource.mysqlData.executeUpdate(sql""" DELETE FROM member_card_account_bak """)
  }

  def getOpenMidMapperForChann(): List[OpenIdMidMap] ={
    val source = MemberSourceEnum.SOURCE_CHANNEL.getVal
    MemberDataSource.mysqlData.rows[OpenIdMidMap](sql"""SELECT * FROM openid_mid_map WHERE source = ${source}""")
  }

  def getAllMemberChannel(): List[MemberMemberChannel] ={
    MemberDataSource.mysqlData.rows[MemberMemberChannel](sql"""SELECT * FROM member_member_channel WHERE delete_type = 0 """)
  }

  def deleteMemberChannelForDelete(): Unit ={
    //删除 delete_type <> 0  的数据
    MemberDataSource.mysqlData.executeUpdate(sql"""DELETE  from member_member_channel WHERE delete_type <> 0""")
  }

  def deleteMemberChannelOpenIdAndMid(openId:String,memberId:String): Unit ={
    //删除 openId,memberId  的数据
    MemberDataSource.mysqlData.executeUpdate(sql"""DELETE  from member_member_channel WHERE OpenId = ${openId} AND MemberId = ${memberId} """)
  }

  /**
    * 判断openId -> oldMemberId 是否在channel中存在
    * @param openId
    * @param oldMemberId
    * @return
    */
  def isChannelExist(openId:String,oldMemberId:String):Int = {
    MemberDataSource.mysqlData.row[Int](
      sql"""
            SELECT count(*) FROM member_member_channel WHERE OpenId = ${openId} AND  MemberId = ${oldMemberId}
         """).getOrElse(0)
  }

  /**
    * 根据ID删除记录
    * @param memberId
    */
  def deleteMemberById(memberId:Long): Int ={
    val deleteFlag = 1; //删除标记为1
    MemberDataSource.mysqlData.executeUpdate(sql""" DELETE FROM member where id = ${memberId} AND delete_flag = ${deleteFlag}""")
  }

  def deleteCardAccountByMemberId(memberId:Long): Unit ={
    MemberDataSource.mysqlData.executeUpdate(sql""" DELETE FROM member_card_account where member_id = ${memberId} """)
  }

   def updateChannelBakForDeleteType (deleteType:Int,id:Long): Unit ={
    MemberDataSource.mysqlData.executeUpdate(sql""" UPDATE  member_channel_bak SET delete_type = ${deleteType} WHERE  Id = ${id}""")
  }
}
