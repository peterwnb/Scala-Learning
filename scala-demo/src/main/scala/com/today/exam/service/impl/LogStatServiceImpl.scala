package com.today.exam.service.impl

import com.today.day01.FileTest.DATE_FORMAT
import com.today.exam.service.LogStatService
import com.today.exam.vo.LogInfoVo

import scala.io.Source

 /**
  *  思路：初步之过滤INFO级别的日志来分析各个统计指标
  *  RequestLog格式：
  * /      时间       |       线程ID       |  级别 |  分隔符-  |                服务名称             |version|     方法名称  |        oratorId       |          operatorName    |reqFlag | body:{}
  * 11-29 00:00:00 027 trans-pool-1-thread-34 INFO - com.isuwang.soa.category.service.CategoryService 1.0.0 getAllCategories operatorId:Optional.empty operatorName:Optional.empty request body:{}
  * RespoinseLog格式
  *|        时间      |          线程ID     |Level| - |                    full   package           |version|    MethodName |         operatorId       |        operatorName     |rspFlag| body:{.....}
  *11-29 00:00:00 052 trans-pool-1-thread-34 INFO - com.isuwang.soa.category.service.CategoryService 1.0.0 getAllCategories operatorId:Optional.empty operatorName:Optional.empty response body:{"json字符串..."}
  *
  */
class LogStatServiceImpl extends LogStatService{
  val REQUEST_LOG_REGEX = s"(\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{3}) (\\S+?) (INFO) (\\S+) (\\S+) (\\d+.\\d+.\\d+) (\\S+) (\\S+:\\S+.\\S+) (\\S+:\\S+.\\S+) (\\S{7}) ([\\s\\S]*?)".r
  val RESPONSE_LOG_REGEX = s"(\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{3}) (\\S+?) (INFO) (\\S+) (\\S+) (\\d+.\\d+.\\d+) (\\S+) (\\S+:\\S+.\\S+) (\\S+:\\S+.\\S+) (\\S{8}) ([\\s\\S]*?)".r
  val ALL_LOG_REGEX = s"(\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{3}) (\\S+?) (INFO) (\\S+) (\\S+) (\\d+.\\d+.\\d+) (\\S+) (\\S+:\\S+.\\S+) (\\S+:\\S+.\\S+) (\\S{7,8}) ([\\s\\S]*?)".r
  /**
    * 统计每个服务的调用次数
    * 根据业务线程池的线程号来分析单次的服务调用。 线程号规则为:trans-pool-1-thread-N(N为1-2位数字)
    * @param filePath ：文件路径
    * @return
    */
  override def statServiceInvokeTimes(filePath: String): Map[String, Long] = {
    //使用文件API读取文件
    val source = Source.fromURL(getClass.getResource(filePath),"UTF-8")
    //group(0) 表示整条日志
    //group(1) 表示正则表达式中的第一部分，即： 时间
    //group(5) 表示 服务名称
    //group(7) 表示 方法名称
    //val list = matchResult.toList.foreach(item => println(s"""服务名：${item.group(5)}  | 方法名：${item.group(7)}"""))
    REQUEST_LOG_REGEX.findAllMatchIn(source.mkString).toList
        .groupBy(s => (s.group(5),s.group(7))).map(elem => s"""${elem._1._1}.${elem._1._2}""" -> elem._2.size.toLong)
  }

  /**
    * 统计每个服务调用的平均时长
    * @param filePath 文件路径
    * @return
    */
override def statInvokeAvgDuration(filePath: String): Map[String, Long] = {
  val source = Source.fromURL(getClass.getResource(filePath),"UTF-8")
  //计算出每个请求的消耗时间的map
  ALL_LOG_REGEX.findAllMatchIn(source.mkString).toList.groupBy(s => (s.group(5),s.group(7))).
    map(x => s"""${x._1._1}.${x._1._2}""" ->{
        val sumReqTime = x._2.filter( p=> p.group(10).trim.equals("request")).map(item => DATE_FORMAT.parse(item.group(1)).getTime).sum
        val sumRespTime = x._2.filter(  p=> p.group(10).trim.equals("response")).map(item => DATE_FORMAT.parse(item.group(1)).getTime).sum
        //(总的应答日志的TimeStamp之和 - 总的请求的TimeStamp之和)/(请求和应答日志之和/2)
        //应为按照服务名称分组之后，会存在相同的多个请求，多个请求，直接求和之后进行相减。外层再求平均值
       (sumRespTime - sumReqTime)/(x._2.length/2)
    })
}

  override def statTopN(filePath: String, n: Int): Map[String, List[LogInfoVo]] = {
    null
  }
}
