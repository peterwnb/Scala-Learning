package com.today.exam.service

import com.today.exam.vo.LogInfoVo

/**
  * 日志统计接口服务
  */
trait LogStatService {
  /**
    * 统计每个服务的调用次数
    * 根据业务线程池的线程号来分析单次的服务调用。 线程号规则为:trans-pool-1-thread-N(N为1-2位数字)
    * @param filePath ：文件路径
    * @return
    */
  def statServiceInvokeTimes(filePath:String):Map[String,Long]

  /**
    * 统计每个服务调用的单次平均时长
    * @param filePath 文件路径
    * @return
    */
  def statInvokeAvgDuration(filePath:String):Map[String,Long]

  def statTopN(filePath:String,n:Int):Map[String,List[LogInfoVo]]
}
