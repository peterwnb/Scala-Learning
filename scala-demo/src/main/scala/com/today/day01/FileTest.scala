package com.today.day01

import java.io.PrintWriter
import java.text.SimpleDateFormat

import com.today.exam.vo.LogInfoVo

import scala.io.Source

object FileTest {
  val LOG_TIME = """(\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}\s\d{3})"""
  val THREAD_ID = """(\S+?)"""
  val LOG_LEVEL = """(INFO)"""
  val SPLIT = """(\S+)"""
  val SERVICE_NAME = """(\S+)"""
  val VERSION = """(\d+.\d+.\d+)"""
  val METHOD = """(\S+)"""
  val OPERATE_ID = """(\S+:\S+.\S+)"""
  val OPERATE_NAME = """(\S+:\S+.\S+)"""
  val REQUEST_FLAG = """(\S{7})"""
  val RESPONSE_FLAG = """(\S{8})"""
  val REQ_OR_RESP_FLAG = """\S{7,8}"""
  val OTHER = """([\s\S].*)"""

  val DATE_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss SSS")

  val REQ_PAT = s"${LOG_TIME} ${THREAD_ID} ${LOG_LEVEL} ${SPLIT} ${SERVICE_NAME} ${VERSION} ${METHOD} ${OPERATE_ID} ${OPERATE_NAME} ${REQUEST_FLAG} ${OTHER}".r
  val RESP_PAT = s"${LOG_TIME} ${THREAD_ID} ${LOG_LEVEL} ${SPLIT} ${SERVICE_NAME} ${VERSION} ${METHOD} ${OPERATE_ID} ${OPERATE_NAME} ${RESPONSE_FLAG} ${OTHER}".r
  val ALL_PAT = s"${LOG_TIME} ${THREAD_ID} ${LOG_LEVEL} ${SPLIT} ${SERVICE_NAME} ${VERSION} ${METHOD} ${OPERATE_ID} ${OPERATE_NAME} ${REQ_OR_RESP_FLAG} ${OTHER}".r

  val ALL_LOG_REGEX = s"(\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{3}) (\\S+?) (INFO) (\\S+) (\\S+) (\\d+.\\d+.\\d+) (\\S+) (\\S+:\\S+.\\S+) (\\S+:\\S+.\\S+) (\\S{7,8}) ([\\s\\S]*?)".r
//  val TIME = "(\\d{2}\\-\\d{2} \\d{2}:\\d{2}:\\d{2} \\d{3})" //时间
//  val THREAD_NUM = "(\\S+?)"
//  val LEVEL = "(INFO)"
//  val BYTE = "(\\S+)"
//  val SERVICE = "(\\S+)"
//  val VERSION = "(\\d{1}\\.\\d{1}\\.\\d{1})"
//  val METHOD = "(\\S+)"
//  val OTHER = "(.*?)"
//  val REQUEST_LOG_PATTERN = s"$TIME $THREAD_NUM $LEVEL $BYTE $SERVICE $VERSION $METHOD $OTHER".r


  def main(args: Array[String]){
    val lines = Source.fromURL(getClass.getResource("/data/detail-productdb-service.2017-11-29.log"),"UTF-8")
    //将读取的文件内容读取成一个大的字符串
    //val reqLen = REQ_PAT.findAllMatchIn(lines.mkString).toList.length

    //val respLen = RESP_PAT.findAllMatchIn(lines.mkString).toList.length

    val allLen = ALL_LOG_REGEX.findAllMatchIn(lines.mkString).toList.length

    println("reqLen="+7258+" , respLen="+7258 +" , allLen="+allLen)

    //group(0) 表示整条日志
    //group(1) 表示正则表达式中的第一部分，即 ${LOG_TIME}
    //group(5) --> ${SERVICE_NAME}
    //group(7) --> ${METHOD}
    //val list = matchResult.toList.foreach(item => println(s服务名：${item.group(5)}  | 方法名：${item.group(7)}"""))

    //统计每个方法的调用次数
//    val res = matchResult.toList.groupBy(s=> (s.group(5),s.group(7))).map(elem =>
//      s"""${elem._1._1} ${elem._1._2}""" -> elem._2.size
//    )
//
//    res.keys.map(key =>{
//      println("服务"+key +",调用次数"+res(key))
//    })

    //封装成List[LogInfoVo]
//    val res = matchResult.map(k => s"""${k.group(2)}-${k.group(5)}.${k.group(7)}""" ->
//      LogInfoVo(
//        k.group(2),
//        k.group(3),
//        k.group(5),
//        k.group(7),
//        DATE_FORMAT.parse(k.group(1)).getTime,
//        k.group(11))
//    ).toMap
//
//    val out = new PrintWriter("E:\\work\\scala-learn\\scala-demo\\src\\main\\resources\\data\\consumeTimeAvg.log")
//    println("-----------")
//
//    res.keys.map(key =>
//      out.println("方法名"+key +"，请求时间"+res(key))
//    )
//    out.close()

   // intln(list.length)
    //list.foreach(s => println(s))
    lines.close();
  }
}
