package com.today.main

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import com.today.common.MemberDataSource
import com.today.enums.{ChannelDeleteType, MemberSourceEnum}
import com.today.mysql.member.dto.{Member, MemberAccount, OpenIdMidMap}
import com.today.mysql.member.dto.freemud.MemberMemberChannel
import com.today.mysql.member.sql.MemberWashSql
import org.slf4j.LoggerFactory
import org.springframework.context.support.GenericXmlApplicationContext
import wangzx.scala_commons.sql.ResultSetMapper

/**
  * 会员清洗
  * 1、所有手机号规整  NULL 或者 "----"  =>  ""
  * 2、所有的OpenId    NULL 或者 "----"  =>  ""
  * 3、手动处理同一个openID对应多个手机号的数据
  *    清理理同⼀一个 openid 对应多个 mobile_phone 的情况。
  *    ⽬目前只有2个membe 具有相同的open_id，不不同的mobile_phone。
  *    omPPGjjcMrKP7K6sMsaSqynsHVSo(13163336359,18589259535)，⼿手动清理理其中⼀一个⼿手机
  *    号码。
  * 4、清洗无用的channel数据(MemberId不存在于member主表中)
  * 5、映射open_ip --> memberId关系到
  * 6、合并手机号相同的数据（多个会员有相同的⼿手机号，除11个1之外
  */
object MemberWashMain {

  private val log = LoggerFactory.getLogger(MemberWashMain.getClass)

  def main(args: Array[String]): Unit = {
    val context = new GenericXmlApplicationContext
    context.setValidating(false)
    context.load("./META-INF/spring/services.xml")
    context.refresh()

    val cmd = args(0).toInt
    cmd match {
      case 1 =>washChannel()
      case 2 => mergeMemberDataByOpenId()
      case 3 => rollbackData()
      case 4 => updateChannelBakForDeleteType()  //更新channel_bak表的delete_type
      case 5 => cleanChannel()                   //清理channel表
      case 6 => deleteUselessMember()            //删除无效的会员信息
      case _ => println(s"""命令错误，请确认需要 1:清洗Channel   2：合并openId重复账号   3：数据回滚  4:channel数据合并  5: 删除无效的member""")
    }

    //1、清洗channel表数据
    //washChannel()

    //2、合并手机号相同的手机号
    //mergeMemberDataByOpenId()
    // mergeMemberDataByOpenId()

   // rollbackData()
    System.exit(0)
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
        log.info("------------------------结束清洗Channel表-----------------")
        System.exit(0)
        return
      }
      Thread.sleep(200);
    }
  }

  def mergeMemberDataByOpenId(): Unit ={
    //查询出来所有WeChatOpenId重复的会员列表
    val list = MemberWashSql.findDuplicateOpenIdMemberNew()
    log.info(s"WeChatOpenId相同的会员总记录条数 : ${list.length}")
    list.groupBy(_.openId).map(s=>{
      log.info(s"根据WeChatOpenIdWeChatOpenId=${s._1}")
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
      //排除target对象之后的其余的剩余对象 ,注意这里取得是id
      val sourceList = memberList.filter(p => p.memberId != target.memberId )
      //合并数据
      log.info(s"""合并数据 ：openId=${target.openId}, targetMemberId=${target.memberId} , sourceMemberIds=[${sourceList.map(_.memberId).mkString(",")}]""")
      mergeData(target,sourceList)

      //待删除的member的所有openIds
      val openIds = memberList.filter(p => p.memberId != target.memberId ).map(_.openId)
      //注意这里取的是 old_memberId 而不是 id ,  将 open_id 集合 写⼊入到 member_channel 表。open_id + 合并后mid 组成的多条记录 ，写⼊到channel,并且不重复。
      addNewOpenId2MemberIdRelation(target,openIds)
    })
  }

  /**
    * 合并openId相同的member数据
    * 4. 清理理重复的 open_id （多个会员有相同的 open_id） select from openid_mid_map
    *   1. 选择其中⼀一个会员（有⼿手机号的优先）
    *   2 . 将所有其它会员的券、卡全部合并到 ⽬目标会员上（并同时删除原会员记录），delete
    *       mmc(openid  old_mid)
    */
  def mergeMemberDataByOpenIdLog(): Unit ={
    //查询出来所有WeChatOpenId重复的会员列表
    val list = MemberWashSql.findDuplicateOpenIdMemberNew()
    log.info(s"WeChatOpenId相同的会员总记录条数 : ${list.length}")
    list.groupBy(_.openId).map(s=>{
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
      //排除target对象之后的其余的剩余对象 ,注意这里取得是id
      val sourceList = memberList.filter(p => p.memberId != target.memberId )
      //合并数据
      log.info(s"""合并数据 ：openId=${target.openId}, targetMemberId=${target.memberId} , sourceMemberIds=[${sourceList.map(_.memberId).mkString(",")}]""")
      addMergeLog(target,sourceList)
      //mergeData(target,sourceList)

      //待删除的member的所有openIds
      val openIds = memberList.filter(p => p.memberId != target.memberId ).map(_.openId)
      //注意这里取的是 old_memberId 而不是 id ,  将 open_id 集合 写⼊入到 member_channel 表。open_id + 合并后mid 组成的多条记录 ，写⼊到channel,并且不重复。
      addNewOpenId2MemberIdRelation(target,openIds)
    })
  }

  /**
    * 将多个member的数据合并到targetId这个会员信息里面
    * 包括member , member_card_account , score_journal, member_order, card_card_base等
    * @param target
    * @param sourceList
    */
  def mergeData(target:MemberAccount,sourceList:List[MemberAccount]):Unit ={
    try{
      //备份主记录
      MemberWashSql.backMainMember(target.memberId)
      MemberWashSql.backMemberCardAccountMain(target.memberId)
      for(source <- sourceList){
        log.info(s"""合并一条会员数据 : sourceMemberId = ${source.memberId} , targetMemberId = ${target.memberId}""")
        //合并会员主档信息,并为待删除的记录打上删除标记
        MemberWashSql.mergeMember(target.memberId,source.memberId)

        //将sourceId对应的card余额数据累加到目标targetId对应的card余额上
        MemberWashSql.mergeMemberCardAccount(target.memberId,source.memberId)

        //合并优惠券
        MemberWashSql.mergeCoupon(target.memberId,source.memberId)
        //合并礼品卡
        MemberWashSql.mergeMemberCard(target.memberId,source.memberId)
        //合并订单数据表
        MemberWashSql.mergeMemberOrder(target.memberId,source.memberId)
        //合并消费流水表
        MemberWashSql.mergeCardConsume(target.memberId,source.memberId)
        //合并积分流水表
        MemberWashSql.mergeScoreJournal(target.memberId,source.memberId)
        //
      }
    } catch {
      case ex:Exception => ex.printStackTrace()
    }
  }

  /**
    * 插入合并的日志信息
    * @param target
    * @param sourceList
    */
  def addMergeLog(target:MemberAccount,sourceList:List[MemberAccount]): Unit ={
    //这里不做物理合并，只把需要合并的数据记录到处理记录日志表里面去
    //1、先插入表log_member_merge
    val sourceOpenIds = sourceList.map(_.memberId).mkString(",")
    val logId = MemberWashSql.insertMergeLog(target,sourceOpenIds)
    for(ma <- sourceList){
      //在插入关联信息
      MemberWashSql.insertMergeLogRel(ma,logId)
      //删除关系映射
      MemberWashSql.deleteMapperRel(ma.memberId,ma.openId)
    }
  }

  /**
    * 插入Slave表的channel
    * @param target
    * @param openIds
    */
  def addNewOpenId2MemberIdRelation(target:MemberAccount,openIds:List[String]): Unit ={
    //插入新的open_id => memberId 映射集合
    for(openId <- openIds){
      if(MemberWashSql.isOpenId2MemberIdExistInMapperTable(openId,target.memberId) == 0){
        //如果映射表不存在 OpenId=${OpenId} and MemberId=${mergedMemberId}的数据
        //那么插入新的组合成的关系映射数据
        log.info(s"""插入一条open->mid映射数据 : mergedMemberId = ${target.memberId} , OpenId = ${openId}""")
        MemberWashSql.insertMapperTable(
          OpenIdMidMap(target.openId,target.memberId,MemberSourceEnum.SOURCE_CHANNEL.getVal,target.oldMemberId)
        )
      }
    }
  }

  /**
    * 数据回滚
    */
  def rollbackData(): Unit ={
    //1、会员数据回滚
    val list = MemberWashSql.getBackRecord();
    list.foreach(item =>{
      //循环回滚消息
      MemberWashSql.updateRecord(item)
    })
    MemberWashSql.cleanRecord()

    //回滚会员主表
    val memberList = MemberWashSql.getMemberBack()
    memberList.foreach(item =>{
      MemberWashSql.rollbackMember(item)
    })
    MemberWashSql.cleanMemberBak()

    //回滚会员余额
    val accountList = MemberWashSql.getMemberAccountBack()
    accountList.foreach(item =>{
      MemberWashSql.rollbackAccount(item)
    })
    MemberWashSql.cleanAccountBak()
  }

  /**
    * 清理member_member_channel表
    */
  def cleanChannel(): Unit ={
    //1、先删除该删除的记录(delete_type != 0)
    MemberWashSql.deleteMemberChannelForDelete()
    //2、清理open_id + MemberId(old_member_id) 关系不存在的记录
    val list = MemberWashSql.getAllMemberChannel()
    list.foreach(ch =>{
      if(MemberWashSql.isOpenIdToMemberIdRelationExist(ch.OpenId,ch.MemberId) == 0){
        //如果不存在该条关系，也需要删除
        MemberWashSql.deleteMemberChannelOpenIdAndMid(ch.OpenId,ch.MemberId)
      }
    })
    //3、openMidMap有 但是 member_channel没有的记录
    val list2 = MemberWashSql.getOpenMidMapperForChann()
    list2.foreach(ch =>{
      if(MemberWashSql.isChannelExist(ch.openId,ch.oldMemberId) == 0){
        //如果channel不存在该条记录，插入新的记录
        //insertChannel(memberId:String,openId:String)
        MemberWashSql.insertChannel(ch.oldMemberId,ch.openId)
      }
    })
  }

  /**
    * 删除无用的会员主表信息
    */
  def deleteUselessMember(): Unit ={
    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    val rs = stmt.executeQuery(" select * from member where delete_flag = 1 ")
    while (rs.next()) {
      val row = ResultSetMapper.material[Member].from(rs)
      if(MemberWashSql.deleteMemberById(row.id) > 0){
        MemberWashSql.deleteCardAccountByMemberId(row.id)
      }

    }
  }


  /**
    * 清洗Channel表
    */
  def updateChannelBakForDeleteType():Unit= {
    log.info("------------------------开始清洗Channel表-----------------")

    val exec = {
      new ThreadPoolExecutor(20, 20, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable](100),
        new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }

    val stmt = MemberDataSource.mysqlData.getConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
    stmt.setFetchSize(Integer.MIN_VALUE)
    val rs = stmt.executeQuery("select * from member_member_channel where delete_type <> 1")  //258827条
    while (rs.next()) {
      val row = ResultSetMapper.material[MemberMemberChannel].from(rs)

      exec.execute(() => {
        try {
           MemberWashSql.updateChannelBakForDeleteType(row.deleteType,row.Id)
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
        log.info("------------------------更新ChannelBak表的删除状态完毕-----------------")
        System.exit(0)
        return
      }
      Thread.sleep(200);
    }
  }
}
