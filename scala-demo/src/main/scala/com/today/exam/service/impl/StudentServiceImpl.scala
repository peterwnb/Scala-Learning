package com.today.exam.service.impl

import com.today.exam.model.Student
import com.today.exam.service.StudentService
import com.today.exam.vo.{AvgResult, CourseRatioResult, FirstRankStatResult, Top5Result}

class StudentServiceImpl extends StudentService{
  /**
    * 统计top5数据
    * @param studentList 学生列表
    * @return 结果集
    */
  override def top5Stat(studentList: List[Student]): Map[String, Top5Result] = studentList.groupBy(x =>(x.grade , x.className)).map(ele => s"${ele._1._1}${ele._1._2}" -> Top5Result(
      ele._2.sortBy(-_.chineseScore).take(5),
      ele._2.sortWith(_.mathScore > _.mathScore).take(5),
      ele._2.sortBy(-_.englishScore).take(5),
      ele._2.sortBy(s => -(s.chineseScore + s.mathScore + s.englishScore)).take(5)
    )
   )

  /**
    * 统计每个班级各门课程的平均分
    *
    * @param studentList
    * @return
    */
  override def avgStatByClassAndCourse(studentList: List[Student]): Map[String, AvgResult] = {
    studentList.groupBy(x=>(x.grade,x.className)).map(ele => s"${ele._1._1}${ele._1._2}" -> AvgResult(
      ele._2.map(_.chineseScore).sum / ele._2.length,
      ele._2.map(_.mathScore).sum / ele._2.length,
      ele._2.map(_.englishScore).sum / ele._2.length
    ))
  }

  /**
    * 统计每个班级 三科各等级人数比例
    * 优:>=90;
    * 良:>=75 and <90
    * 及格:>=60,<75;
    * 不及格:<60
    *
    * @param studentList
    * @return
    */
  override def ratioStatByClassAndCourse(studentList: List[Student]): Map[String, CourseRatioResult] = {
    studentList.groupBy(x=>(x.grade,x.className)).map(ele => s"${ele._1._1}${ele._1._2}" -> CourseRatioResult(
      s"""语文成绩各级别占比 优=${BigDecimal(ele._2.count(_.chineseScore >= 90).toFloat / ele._2.length).setScale(2, BigDecimal.RoundingMode.HALF_UP)}, 良=${BigDecimal(ele._2.count(x => x.chineseScore >= 75 && x.chineseScore < 90).toFloat / ele._2.length).setScale(2, BigDecimal.RoundingMode.HALF_UP)}, 中=${BigDecimal(ele._2.count(x => x.chineseScore >= 60 && x.chineseScore < 75).toFloat / ele._2.size).setScale(2, BigDecimal.RoundingMode.HALF_UP)}, 差=${BigDecimal(ele._2.count(x => x.chineseScore < 60).toFloat / ele._2.size).setScale(2, BigDecimal.RoundingMode.HALF_UP)}""",
      s"""数学成绩各级别占比 优=${BigDecimal(ele._2.count(_.mathScore >= 90).toFloat / ele._2.length).setScale(2, BigDecimal.RoundingMode.HALF_UP)}, 良=${BigDecimal(ele._2.count(x => x.mathScore >= 75 && x.mathScore < 90).toFloat / ele._2.length).setScale(2, BigDecimal.RoundingMode.HALF_UP)}, 中=${BigDecimal(ele._2.count(x => x.mathScore >= 60 && x.mathScore < 75).toFloat / ele._2.size).setScale(2, BigDecimal.RoundingMode.HALF_UP)}, 差=${BigDecimal(ele._2.count(x => x.mathScore < 60).toFloat / ele._2.size).setScale(2, BigDecimal.RoundingMode.HALF_UP)}""",
      s"""英语成绩各级别占比 优=${BigDecimal(ele._2.count(_.englishScore >= 90).toFloat / ele._2.length).setScale(2, BigDecimal.RoundingMode.HALF_UP)}, 良=${BigDecimal(ele._2.count(x => x.englishScore >= 75 && x.englishScore < 90).toFloat / ele._2.length).setScale(2, BigDecimal.RoundingMode.HALF_UP)}, 中=${BigDecimal(ele._2.count(x => x.englishScore >= 60 && x.englishScore < 75).toFloat / ele._2.size).setScale(2, BigDecimal.RoundingMode.HALF_UP)}, 差=${BigDecimal(ele._2.count(x => x.englishScore < 60).toFloat / ele._2.size).setScale(2, BigDecimal.RoundingMode.HALF_UP)}"""
    ))
  }

  /**
    * 计算年级总分前二十名的同学
    * @param studentList 学生列表
    * @return
    */
  override def top20StatByGrade(studentList: List[Student]): Map[String, List[Student]] = {
    studentList.groupBy(_.grade).map(s =>
      //s._1 -> s._2.sortBy(sc => -(sc.chineseScore + sc.mathScore + sc.englishScore))
      s._1 -> s._2.sortWith((a,b)=> (a.chineseScore + a.mathScore + a.englishScore) > (b.chineseScore + b.mathScore + b.englishScore)).take(20)
    )
  }

  /**
    * 统计每个年级 每个学科 男 和 女 的状元
    * 按照年级+性别分组
    * @param studentList
    * @return -> grade+sex+courseName
    */
  override def firstRankStatByCourseAndSex(studentList: List[Student]): Map[String, List[FirstRankStatResult]] = {
    //先按照年级分组
    studentList.groupBy(_.grade).map(s => {
       val sexMap = s._2.groupBy(_.sex)
      s._1 -> List(
        FirstRankStatResult(
          sexMap.contains("男") match {
            case true => sexMap("男").sortBy(-_.chineseScore).take(1).head
            case false => null
          }, sexMap.contains("女") match {
            case true => sexMap("女").sortBy(-_.chineseScore).take(1).head
            case false => null
          }),
        FirstRankStatResult(
          sexMap.contains("男") match {
            case true => sexMap("男").sortBy(-_.mathScore).take(1).head
            case false => null
          }, sexMap.contains("女") match {
            case true => sexMap("女").sortBy(-_.mathScore).take(1).head
            case false => null
          }),
        FirstRankStatResult(
          sexMap.contains("男") match {
            case true => sexMap("男").sortBy(-_.englishScore).take(1).head
            case false => null
          }, femaleStudent = sexMap.contains("女") match {
            case true => sexMap("女").sortBy(-_.englishScore).take(1).head
            case false => null
          }
        )
      )
    })
  }
}
