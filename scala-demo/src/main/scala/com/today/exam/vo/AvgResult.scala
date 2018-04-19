package com.today.exam.vo

/**
  * 平均分统计结果
  * @param chineseAvgScore
  * @param mathAvgScore
  * @param englishAvgScore
  */
case class AvgResult(chineseAvgScore:Int ,
                     mathAvgScore:Int,
                     englishAvgScore:Int) {
  override def toString: String = {
    "\t\t语文平均分:"+this.chineseAvgScore + " , 数学平均分:"+this.mathAvgScore+" ,英语平均分:"+this.englishAvgScore+"\r\n"
  }
}
