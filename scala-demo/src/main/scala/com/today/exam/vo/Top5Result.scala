package com.today.exam.vo

import com.today.exam.model.Student

/**
  * top5统计结果
  * @param chineseTop5 语文t5
  * @param mathTop5  数学top5
  * @param englishTop5 英语top5
  * @param totalTop5 总分top5
  */
case class Top5Result(chineseTop5:List[Student],
                      mathTop5:List[Student],
                      englishTop5:List[Student],
                      totalTop5:List[Student]
                     )
