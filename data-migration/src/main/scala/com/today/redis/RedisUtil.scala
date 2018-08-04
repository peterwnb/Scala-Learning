package com.today.redis

import java.net.{InetAddress, Socket}

object RedisUtil {
  //val hostIp = "127.0.0.1"
  val hostIp = "192.168.20.125"
  val port = 6004

  //var socket:Socket



  def main(args: Array[String]): Unit = {
      //flushData()
  }

  /**
    * 测试hget
    */
  def testHGet():Unit = {
    try {
      val cmd = "hget"
      val cmdLength = "$"+cmd.length

      val PARAM1 = "CARD_CACHE"
      val PARAM1_LEN = "$"+PARAM1.getBytes().length

      val PARAM2 = "00000000000000000000000-110200000173"
      val PARAM2_LEN = "$"+PARAM2.getBytes().length

      val CRCL = "\r\n"

      //hget CARD_CACHE 00000000000000000000000-110200000173
      //"*3\r\n$3\r\nset\r\n$2\r\nwk\r\n$4\r\n2018"

      //*3
      //$4
      //hget
      //$10
      //CARD_CACHE
      //$36
      //00000000000000000000000-110200000173
      val sendCommand = s"""*3${CRCL}${cmdLength}${CRCL}${cmd}${CRCL}${PARAM1_LEN}${CRCL}${PARAM1}${CRCL}${PARAM2_LEN}${CRCL}${PARAM2}${CRCL}"""
      println("发送给redis的内容："+sendCommand)
      val socket = new Socket(InetAddress.getByName(hostIp),port)
      socket.getOutputStream.write(sendCommand.getBytes)
      val b = new Array[Byte](2048)
      val in = socket.getInputStream
      in.read(b)
      println(new String(b))
    }catch {
      case ex: Exception =>
        ex.printStackTrace
    }
  }

  /**
    * 清除redis缓存数据
    */
  def flushData():Unit = {
    val CRCL = "\r\n"
    val cmd = "flushall"
    val cmdLength = "$"+cmd.length
    val sendCommand = s"""*1${CRCL}${cmdLength}${CRCL}${cmd}${CRCL}"""
    println("发送给redis的内容：\r\n"+sendCommand)

    val socket = new Socket(InetAddress.getByName(hostIp),port)
    socket.getOutputStream.write(sendCommand.getBytes)
    val b = new Array[Byte](2048)
    val in = socket.getInputStream
    in.read(b)
    println(new String(b))
    socket.close()
  }
}
