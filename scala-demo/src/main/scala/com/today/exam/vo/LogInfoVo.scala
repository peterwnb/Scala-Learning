package com.today.exam.vo

/**
  * LogInfo实体
  * @param threadId
  * @param logLevel
  * @param serviceName
  * @param method
  * @param logTime
  * @param logInfo
  */
case class LogInfoVo(
                    threadId:String,
                    logLevel:String,
                    serviceName:String,
                    method:String,
                    logTime:Long,
                    logInfo:String
                    )
{
  override def toString: String = {
    "线程ID:" + this.threadId + " , 服务名称:" + this.serviceName + ",方法名称:" + serviceName + " ,日志时间:" + this.logTime + " ,信息:" + logInfo
  }
}