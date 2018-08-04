package com.today.main

import java.text.SimpleDateFormat
import java.util.Calendar

import com.today.common.{GenIdUtil, RedisDataSource}
import com.today.main.MemberMain.{rows, start}
import com.today.mysql.member.sql.MemberSql
import org.springframework.context.support.GenericXmlApplicationContext

@deprecated
object MemberCompensateMain {

  def main(args: Array[String]): Unit = {
    val context = new GenericXmlApplicationContext
    context.setValidating(false);
    context.load("./META-INF/spring/services.xml")
    context.refresh()
    System.setProperty("soa.zookeeper.host", "123.206.103.113") //取号服务器zookeeper配置
    executeMember()
  }

  /**
    * 获取昨天的日期
    * @return
    */
  def getYesterday():String= {
    var dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    var cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.DATE, -1)
    var yesterday = dateFormat.format(cal.getTime())
    yesterday
  }


  def executeMember(): Unit = {
    println("开始执行补全机制")
    //首先开始补全新增的用户信息
    val startTime=getYesterday()+" 00:00:00"
    val endTime=getYesterday()+" 23:59:59"
    val list=MemberSql.findNewMemberList(startTime,endTime)
    for(member <- list){
      //创建用户
      val memberCode = GenIdUtil.getId(GenIdUtil.MEMBER_CODE)
      val memberId=MemberSql.createUser(member,memberCode)
      println("创建用户成功"+member.MemberId.toString+"----"+memberId.toString)
      //把新老memberId对应关系存到redis里面
      RedisDataSource.redisData.addSet(member.MemberId.toString,memberId.toString)
      //创建用户账户
      MemberSql.createMemberAccount(memberId,member.MemberName,member.LeftValue)
      println("创建账户成功")
      println(start+"成功创建一个用户用户id为"+memberId)
      //开始创建账户流水
      val accountList=MemberSql.findMemberAccountListByMemberId(member.MemberId)
      for(account <- accountList){
        MemberSql.createAccountStream(account)
        println("创建账户流水成功")
      }
      //开始创建积分流水
      val sourceList=MemberSql.findScoreJournalLListByMemberId(member.MemberId)
      for(score <- sourceList){
        //MemberSql.createScoreJournal(score)
        println("创建积分流水成功")
      }
      //开始创建用户优惠劵
      val couponList=MemberSql.findMemberCouponByMemberId(member.MemberId)
      for(coupon <- couponList){
        MemberSql.createUserCoupon(coupon)
        println("创建优惠卷成功")
      }

    }
    println("新增用户补全完成")

  }


  def executeUpdateMember(): Unit = {
    val list=MemberSql.findUpdateMemberList()
    for(members <- list){
      val memberId=RedisDataSource.redisData.getSet(members.memberId.toString).toString
      //获取用户当前信息
      val member=MemberSql.findMemberById(memberId)
      MemberSql.updateUser(member,memberId)
      MemberSql.updateStatus(members.id);
    }

  }








}
