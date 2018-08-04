package com.today.common

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import scala.annotation.tailrec

object Util {

  val FIRST_VERSION = 1
  lazy val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  /**
    * 获取下一个版本号
    * @param version
    */
  def getNextVersion(version :Int): Int ={
    version + 1
  }

  /**
    * 把标志转换成存储的二进制
    * 输入顺序为标志位的右边开始
    * @param flagList
    * @return
    */
  def transformFlag(flagList: Boolean*): Int = {
    val bytes = flagList.map(flag => {
      if (flag) "1" else "0"
    }).toBuffer[String].reverse.mkString("")
    Integer.valueOf(bytes, 2)
  }

  def getYesterdayDate(): String ={
    val calendar = Calendar.getInstance() //得到日历
    calendar.setTime(new Date())//把当前时间赋给日历
    calendar.add(Calendar.DAY_OF_MONTH, -1)  //设置为前一天
    s"${dateFormat.format(calendar.getTime)} 00:00:00"   //得到前一天的时间
  }

  /**
    * 生成一个字符串的hashCode
    *
    * @param str
    * @return
    */
  def bKDRHash2(str: String): Int = {
    val seed: Int = 131 // 31 131 1313 13131 131313 etc..
    val hash: Int = 0 // 返回的hash结果
    val index: Int = 0 // 用来判断跳出递归的条件
    @tailrec
    def generateHash(hash: Int, index: Int): Int = {
      if (index == str.length) hash else generateHash((hash * seed + str.charAt(index)) & 0x7FFFFFFF, index + 1)
    }

    generateHash(hash, index)
  }


  def changCategoryParentCode(oldParentCode:Option[String]):Option[String] ={
    if(oldParentCode.isDefined){
      Option(changCategoryCode(oldParentCode.get))
    }else{
      Option.empty[String]
    }
  }

  /**
    *
    * @param oldCode 011001 ==> 000100010001
    * @return
    */
  def changCategoryCode(oldCode:String): String ={
    if(oldCode.length == 2){
      val a = oldCode.substring(0)
      s"00${a}"
    }else if(oldCode.length == 3){
      val a = oldCode.substring(0,2)
      val b = oldCode.substring(2)
      s"00${a}000${b}"
    }else{
      val a = oldCode.substring(0,2)
      val b = oldCode.substring(2,3)
      val c = oldCode.substring(3)
      s"00${a}000${b}0${c}"
    }

  }

  def main(args: Array[String]): Unit = {
    println(changCategoryCode("011001"))
  }

}
