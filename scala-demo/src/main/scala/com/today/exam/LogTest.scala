package com.today.exam

import com.today.exam.service.impl.LogStatServiceImpl

object LogTest {
  val FILE_PATH = "/data/detail-productdb-service.2017-11-29.log"
  def main(args: Array[String]): Unit = {
     val logService = new LogStatServiceImpl()
     //注意这里的文件目录是对应项目的resources/data目录下的日志文件，相对目录读取
//     val invokeTimesResult = logService.statServiceInvokeTimes(FILE_PATH)
//     invokeTimesResult.keys.map(key =>{
//        println("服务方法名："+key + " , 调用次数："+invokeTimesResult(key))
//     })

    logService.statInvokeAvgDuration(FILE_PATH)
  }
}
