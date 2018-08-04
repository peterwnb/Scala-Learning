package com.today.main.bak

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import com.today.common.MemberDataSource
import com.today.enums.{ChannelDeleteType, MemberSourceEnum}
import com.today.mysql.member.dto.freemud.MemberMemberChannel
import com.today.mysql.member.dto.{Member, MemberChannelBak}
import com.today.mysql.member.sql.MemberWashSql
import org.slf4j.LoggerFactory
import org.springframework.context.support.GenericXmlApplicationContext
import wangzx.scala_commons.sql.{ResultSetMapper, _}

/**
  * 会员数据清洗程序
  */
object DataWashMain {

  private val log = LoggerFactory.getLogger(DataWashMain.getClass)

  def main(args: Array[String]): Unit = {
    val context = new GenericXmlApplicationContext
    context.setValidating(false)
    context.load("./META-INF/spring/services.xml")
    context.refresh()

    //清洗channel表
    washChannel()

    //添加openId -> memberId 映射表
    //addOpenIdAndMemberIdRelation()

    //回滚member数据
    //rollbackMemberFormChannelBak()

    //mergeMemberDataByPhone()

    //mergeMemberDataByOpenId()
  }

  /**
    * 清洗Channel表
    */
  def washChannel():Unit= {
    log.info("------------------------开始清洗Channel表-----------------")

    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    val rs = stmt.executeQuery("select * from member_member_channel  ")
    while (rs.next()) {
      val row = ResultSetMapper.material[MemberMemberChannel].from(rs)

      exec.execute(() => {
        try {
          val memberOps = MemberWashSql.findMemberByOldMemberId(row.MemberId)
          if(memberOps.nonEmpty){
            val member = memberOps.get;
            if(member.wechatOpenId.equals("")){
              //如果主表没有we_chat_open_id , 复制OpenId过去
              log.info(s"清洗Channel表记录-会员ID存在-主表没有OpenId: [channel.Id=${row.Id} , channel.MemberId=${row.MemberId} , channel.OpenId=${row.OpenId} ,member.Id=${member.id} ,member.old_MemberId=${member.old_memberId} ]")
              MemberWashSql.updateMemberForOpenId(row.MemberId,row.OpenId)
              //标记为待删除
              MemberWashSql.markChannelForDelete(row.Id,ChannelDeleteType.MEMBER_NO_OPEN_ID.getVal)
            } else {
              //主表有we_chat_open_id
              if(row.OpenId.equals(member.wechatOpenId)){
                //如果channel的OpenId = member.weChatOpenId，直接删除
                log.info(s"清洗Channel表记录-会员ID存在-openId相同: [channel.Id=${row.Id} , channel.MemberId=${row.MemberId} , channel.OpenId=${row.OpenId} ,member.Id=${member.id} ,member.old_MemberId=${member.old_memberId} ,member.wechatOpenId=${member.wechatOpenId}]")
                //标记为待删除
                MemberWashSql.markChannelForDelete(row.Id,ChannelDeleteType.SAME_OPENID.getVal)
              } else {
                //如果不等，打印一条日志，暂时不做处理
                log.debug(s"清洗Channel表记录-会员ID存在-openId不相同: [channel.Id=${row.Id} , channel.MemberId=${row.MemberId} , channel.OpenId=${row.OpenId} ,member.Id=${member.id} ,member.old_MemberId=${member.old_memberId} ,member.wechatOpenId=${member.wechatOpenId}]")
              }
            }
          } else {
            //没有会员主表信息与其对应，直接删除
            log.warn(s"清洗Channel表记录-非法会员ID: [channel.Id=${row.Id} , channel.MemberId=${row.MemberId} , channel.OpenId=${row.OpenId}]")
            //标记为待删除
            MemberWashSql.markChannelForDelete(row.Id,ChannelDeleteType.INVALID_MEMBER_ID.getVal)
          }
        }catch{
          case ex: Exception =>
            ex.printStackTrace()
            log.info(s" 清理失败 ，id=${row.Id} , OpenId=${row.OpenId} , MemberId=${row.MemberId}")
        }
      })
    }

    //关闭线程池，但是不会立刻关闭，等待所有的任务执行完毕之后，才会执行
    exec.shutdown()
    while (true){
      if (exec.isTerminated()) {
        //处理完channel记录之后,将待删除的channel数据批量移动到备份表member_channel_bak
        MemberWashSql.moveDeletingChannelToBak()
        //然后删除待删除的channel数据
        MemberWashSql.deleteChannels()
        log.info("------------------------结束清洗Channel表-----------------")
        System.exit(0)
        return
      }
      Thread.sleep(200);
    }
  }

  /**
    * 添加OpenId -> old_memberId  的映射关系到openid_mid_map表
    */
  def addOpenIdAndMemberIdRelation(): Unit ={
    log.info("将MemberId和OpenId的关系维护到openid_mid_map映射表")

    //先处理member主表
    MemberDataSource.mysqlData.withConnection(conn => {
      val stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
      stmt.setFetchSize(Integer.MIN_VALUE)
      val rs = stmt.executeQuery("select * from member where old_memberId != '' and old_memberId is not null ")
      while (rs.next()) {
        val row = ResultSetMapper.material[Member].from(rs)
        if(MemberWashSql.isOpenIdToMemberIdRelationExist(row.wechatOpenId,row.old_memberId) == 0){
          //如果关系还没有进行维护，加入到维护关系表
          MemberWashSql.addOpenIdToMemberIdRelation(row.wechatOpenId,row.old_memberId,MemberSourceEnum.SOURCE_MEMBER.getVal)
        }
      }
    })

    //再处理member_member_channel表
    MemberDataSource.mysqlData.withConnection(conn => {
      val stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
      stmt.setFetchSize(Integer.MIN_VALUE)
      val rs = stmt.executeQuery("select * from member_member_channel where length(OpenId) > 26 ")
      while (rs.next()) {
        val row = ResultSetMapper.material[MemberMemberChannel].from(rs)
        if(MemberWashSql.isOpenIdToMemberIdRelationExist(row.OpenId,row.MemberId) == 0){
          //如果关系还没有进行维护，加入到维护关系表
          MemberWashSql.addOpenIdToMemberIdRelation(row.OpenId,row.MemberId,MemberSourceEnum.SOURCE_CHANNEL.getVal)
        }
      }
    })
  }

  /**
    * 回滚清除channel表时对member表做的openId赋值的更改
    */
  def rollbackMemberFormChannelBak(): Unit ={
    //再处理member_member_channel表
    MemberDataSource.mysqlData.withConnection(conn => {
      val stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
      stmt.setFetchSize(Integer.MIN_VALUE)
      val rs = stmt.executeQuery("select * from member_channel_bak where length(OpenId) > 26 and delete_type = 3")
      while (rs.next()) {
        val row = ResultSetMapper.material[MemberChannelBak].from(rs)
          //还原member表的we_chat_open_id
          MemberWashSql.updateMemberForOpenId(row.MemberId,"")
      }
    })
  }

  /**
    * 将多个member的数据合并到targetId这个会员信息里面
    * 包括member , member_card_account , score_journal, member_order, card_card_base等
    * @param targetId
    * @param sourceIds
    */
  def mergeData(targetId:Long, sourceIds:List[Long]):Unit ={
    for(sourceId <- sourceIds){
      log.info(s"""合并一条会员数据 : sourceMemberId = ${sourceId} , targetMemberId = ${targetId}""")
      //合并会员主档信息,并为待删除的记录打上删除标记
      MemberWashSql.mergeMember(targetId,sourceId)
      //将sourceId对应的card余额数据累加到目标targetId对应的card余额上
      MemberWashSql.mergeMemberCardAccount(targetId,sourceId)
      //合并优惠券
      MemberWashSql.mergeCoupon(targetId,sourceId)
      //合并礼品卡
      MemberWashSql.mergeMemberCard(targetId,sourceId)
      //合并订单数据表
      MemberWashSql.mergeMemberOrder(targetId,sourceId)
      //合并消费流水表
      MemberWashSql.mergeCardConsume(targetId,sourceId)
      //合并积分流水表
      MemberWashSql.mergeScoreJournal(targetId,sourceId)
      //
    }
  }

  /**
    * 将待删除的member记录的we_chat_open_id字段值填到channel表的openId字段
    */
  def fillWeChatOpenIdToChannel(mergedMemberId:String,openIds:List[String]): Unit ={
    for(openId <- openIds){
      if(MemberWashSql.isOpenIdWithMemberIdExistInChannel(openId,mergedMemberId) == 0){
        //如果channel表不存在 OpenId=${OpenId} and MemberId=${mergedMemberId}的数据
        //那么插入新的组合成的数据
        log.info(s"""插入一条channel数据 : mergedMemberId = ${mergedMemberId} , OpenId = ${openId}""")
        MemberWashSql.insertChannel(mergedMemberId,openId)
      }
    }
  }


  /**
    * 合并有相同手机号的会员信息
    * 3. 清理理重复的⼿手号（多个会员有相同的⼿手机号，除11个1  和 为空的之外）
    *  3.1. 选择其中⼀一个会员
    *  3.2. 计算 open_id 集合
    *  3.3. 将所有其它会员的券、卡 全部合并到 ⽬目标会员上（并同时删除原记录）
    *  3.4. 将 open_id 集合 写⼊入到 member_channel 表。(open_id, 合并后mid)写⼊入并且不不重复
    */
  def mergeMemberDataByPhone(): Unit ={
    //查询出来所有手机号重复的会员列表
    val list = MemberWashSql.findDuplicatePhoneMember()
    log.info(s"手机号相同的总条数 : ${list.length}")
    //按照手机号进行分组
    list.groupBy(_.mobilePhone).map(s=>{
      //key(手机号)
      log.info(s"根据手机号分组，手机号=${s._1}")
      val memberList = s._2;
      //手机号相同的情况
      val target = if(memberList.filter(p=> p.wechatOpenId != null && !p.wechatOpenId.equals("")).length >0){
        //优先取出含有openId的记录
        memberList.filter(p=> p.wechatOpenId != null && !p.wechatOpenId.equals("")).head
      } else {
        //否则随机取一条
        memberList.head;
      }

      //排除target对象之后的其余的剩余对象 ,注意这里取得是 id 而不是 old_memberId
      val sourceIds = memberList.filter(p => p.id != target.id ).map(_.id)
      //合并数据
      log.info(s"""合并数据 ： targetMemberId=${target.id} , sourceMemberIds=${sourceIds}""")
      mergeData(target.id,sourceIds)

      //待删除的member的所有openIds
      val openIds = memberList.filter(p => p.id != target.id ).map(_.wechatOpenId)
      //注意这里取的是 old_memberId 而不是 id ,  将 open_id 集合 写⼊入到 member_channel 表。open_id + 合并后mid 组成的多条记录 ，写⼊到channel,并且不重复。
      fillWeChatOpenIdToChannel(target.old_memberId,openIds)
    })
  }

  /**
    * 合并openId相同的member数据
    * 4. 清理理重复的 open_id （多个会员有相同的 open_id） select from openid_mid_map
    *   1. 选择其中⼀一个会员（有⼿手机号的优先）
    *   2 . 将所有其它会员的券、卡全部合并到 ⽬目标会员上（并同时删除原会员记录），delete
    *       mmc(openid  old_mid)
    */
  def mergeMemberDataByOpenId(): Unit ={
    //查询出来所有WeChatOpenId重复的会员列表
    val list = MemberWashSql.findDuplicateOpenIdMember()
    log.info(s"WeChatOpenId相同的会员总记录条数 : ${list.length}")
    list.groupBy(_.wechatOpenId).map(s=>{
      log.info(s"根据WeChatOpenId分组，WeChatOpenId=${s._1}")
      val memberList = s._2;
      //openId相同的情况
      val target = if(memberList.filter(p=> p.mobilePhone != null && !p.mobilePhone.equals("")).length >0){
        //优先取出含有手机号的记录
        memberList.filter(p=> p.mobilePhone != null && !p.mobilePhone.equals("")).head
      } else {
        //否则随机取一条
        memberList.head;
      }

      //数据合并
      //排除target对象之后的其余的剩余对象 ,注意这里取得是 id 而不是 old_memberId
      val sourceIds = memberList.filter(p => p.id != target.id ).map(_.id)
      //合并数据
      log.info(s"""合并数据 ： targetMemberId=${target.id} , sourceMemberIds=${sourceIds}""")
      mergeData(target.id,sourceIds)

      //待删除的member的所有openIds
      val openIds = memberList.filter(p => p.id != target.id ).map(_.wechatOpenId)
      //注意这里取的是 old_memberId 而不是 id ,  将 open_id 集合 写⼊入到 member_channel 表。open_id + 合并后mid 组成的多条记录 ，写⼊到channel,并且不重复。
      fillWeChatOpenIdToChannel(target.old_memberId,openIds)
    })
  }
}
