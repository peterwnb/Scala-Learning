package com.today.common

class GenIdTool {
  var hostIp: String = _
  var port: String = _

  def init() {}

  def setHostIp(hostIp:String): Unit ={
    this.hostIp = hostIp
  }
  def setPort(port:String): Unit ={
    this.port = port
  }

}
