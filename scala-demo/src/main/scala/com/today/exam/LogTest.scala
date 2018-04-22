package com.today.exam

import java.io.PrintWriter

import com.today.exam.service.impl.LogStatServiceImpl

/**
  * 日志文件操作调用测试
  */
object LogTest {
  val FILE_PATH = "/data/detail-productdb-service.2017-11-29.log"
  def main(args: Array[String]): Unit = {
    //1、测试每个服务调用次数统计
    testStatInvokeTimes()

    //2、测试每个服务的平均调用时长
    //testStatInvokeAvgDuration()
  }

  def testStatInvokeTimes():Unit = {
    val logService = new LogStatServiceImpl()
    //注意这里的文件目录是对应项目的resources/data目录下的日志文件，相对目录读取
    val invokeTimesResult = logService.statServiceInvokeTimes(FILE_PATH)
    invokeTimesResult.keys.map(key =>
      println("服务方法名："+key + " , 调用次数："+invokeTimesResult(key)))
  }

  def testStatInvokeAvgDuration():Unit = {
    val logService = new LogStatServiceImpl()
    val mapResult = logService.statInvokeAvgDuration(FILE_PATH)
    println(getClass.getResource("/data/consumeTimeAvg.log").getPath.substring(1))

    val out = new PrintWriter(getClass.getResource("/data/consumeTimeAvg.log").getPath)
    mapResult.keys.map(key=>{
      out.println("服务名称:"+key + "平均耗时:"+mapResult(key))
    })
    out.println()
    out.close()
  }
}
