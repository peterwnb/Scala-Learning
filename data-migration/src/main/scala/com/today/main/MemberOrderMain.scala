package com.today.main

import java.util.UUID
import java.util.concurrent.{CountDownLatch, Executors}

import com.today.api.member.scala.MemberServiceClient
import com.today.api.member.scala.enums.{MemberRegisterSourceEnum, MemberSexEnum, UserCardStatusEnum}
import com.today.api.user.scala.request.BindUserCardRequest
import org.springframework.context.support.GenericXmlApplicationContext

object MemberOrderMain {

  val ThreadNum = 10
  //发令枪 ,
  val cdl = new  CountDownLatch(ThreadNum)

  //线程池
  val exec = Executors.newFixedThreadPool(ThreadNum)

  val memberClient = new MemberServiceClient // 会员服务
  def main(args: Array[String]): Unit = {
    val context = new GenericXmlApplicationContext
    context.setValidating(false);
    context.load("./META-INF/spring/services.xml")
    context.refresh()
    System.setProperty("soa.zookeeper.host", "127.0.0.1") //设置soa服务注册地址

    for(i <- 1 to ThreadNum) yield  {
      //val n = i%5
      //val phone = f"159027831$n%02d"
      exec.execute(new Request(BindUserCardRequest(
        membershipNumber = "18698890987812",
        wechatOpenId = "omPPGjnjEWY9ld88bGI0Rn1_ghsE",
        unionId = "8bGI0Rn1_ghsEIHGFY12",
        phone = "15902783101",
        nickname = "Sekai",
        registerSource = MemberRegisterSourceEnum.WECHAT_H5,
        userCardStatus = UserCardStatusEnum.NORMAL,
        sex = Option(MemberSexEnum.MALE
        ))))
      cdl.countDown()
    }
  }

  /**
    * 客户端绑卡请求
    * @param request
    */
  class Request(request:BindUserCardRequest) extends Runnable{
    override def run(): Unit = {
      try {
        //一直阻塞当前线程，直到计时器的值为0
        cdl.await();
      } catch{
        case e => e.printStackTrace()
      }
      println(s"请求：shipNumber=${request.membershipNumber},wechatOpenId=${request.wechatOpenId}, unionId=${request.unionId} , phone=${request.phone} , nickname=${request.nickname} , registSource=${request.registerSource.name}")
      //发送绑卡请求
      memberClient.bindUserCardService(request)
    }
  }
}
