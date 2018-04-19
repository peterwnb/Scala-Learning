package com.today.exam.vo

/**
  * 三科各等级人数比例(优:>=90;良:>=75,<90,及格:>=60,<75;不及格:<60)
  * @param chineseRatio 语文各等级人数占比
  * @param mathRatio    数学各等级人数占比
  * @param englishRatio 英语各等级人数占比
  */
 case class CourseRatioResult(chineseRatio:String,
                         mathRatio:String,
                         englishRatio:String){
 override def toString: String = {
  "\t\t"+this.chineseRatio +"\r\n\t\t"+this.mathRatio+"\r\n\t\t" +this.englishRatio+"\r\n\t\t"
 }
}
