package com.today.main

import java.sql.Timestamp

import com.today.tasks.MemberOtonSyncTask
import org.springframework.context.support.GenericXmlApplicationContext

object SyncTaskMain {

  def main(args: Array[String]): Unit = {
    val context = new GenericXmlApplicationContext
    context.setValidating(false)
    context.load("./META-INF/spring/services.xml")
    context.refresh()
    System.setProperty("soa.zookeeper.host", "192.168.10.127") //取号服务器zookeeper配置
    //数据开始时间
    val startTime = System.currentTimeMillis() - 72 * 3600 * 1000
    new MemberOtonSyncTask(new Timestamp(startTime)).start
    //new MemberMemberBaseMain(new Timestamp(startTime)).start
  }
}
