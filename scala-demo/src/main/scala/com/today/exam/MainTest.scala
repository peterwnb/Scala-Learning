package com.today.exam

import com.today.exam.model.Student
import com.today.exam.service.impl.StudentServiceImpl

object MainTest {
  val studentList = List(
    Student("xs10","男","一年级","一班",90,85,95),
    Student("xs11","女","一年级","一班",65,79,80),
    Student("xs12","女","一年级","一班",88,79,85),
    Student("xs13","女","一年级","一班",86,90,80),
    Student("xs14","男","一年级","一班",79,80,88),
    Student("xs15","女","一年级","一班",65,79,80),
    Student("xs16","男","一年级","一班",95,100,100),
    Student("xs17","女","一年级","一班",65,79,80),
    Student("xs18","男","一年级","一班",65,79,70),
    Student("xs19","女","一年级","一班",65,79,59),

    Student("xs20","女","一年级","二班",97,99,99),
    Student("xs21","女","一年级","二班",95,100,100),
    Student("xs22","女","一年级","二班",90,75,90),
    Student("xs23","女","一年级","二班",65,79,80),
    Student("xs24","女","一年级","二班",55,59,58),
    Student("xs25","女","一年级","二班",65,75,60),
    Student("xs26","女","一年级","二班",80,85,70),
    Student("xs27","女","一年级","二班",50,50,60),
    Student("xs28","女","一年级","二班",65,79,80),
    Student("xs29","女","一年级","二班",65,79,80),

    Student("xs30","女","二年级","一班",65,79,80),
    Student("宁采臣","男","二年级","一班",96,99,100),
    Student("xs32","女","二年级","一班",75,70,80),
    Student("xs33","男","二年级","一班",55,89,80),
    Student("xs34","女","二年级","一班",85,93,80),
    Student("xs35","男","二年级","一班",65,79,80),
    Student("xs36","男","二年级","一班",95,79,80),
    Student("xs37","女","二年级","一班",80,79,80),
    Student("xs38","男","二年级","一班",78,79,80),
    Student("xs39","女","二年级","一班",95,79,80),

    Student("小倩","女","二年级","二班",100,99,99),
    Student("xs41","女","二年级","二班",95,89,97),
    Student("xs42","男","二年级","二班",90,75,90),
    Student("xs43","女","二年级","二班",65,79,80),
    Student("xs44","男","二年级","二班",55,59,58),
    Student("xs45","男","二年级","二班",65,75,60),
    Student("xs46","女","二年级","二班",80,85,70),
    Student("xs47","男","二年级","二班",50,50,60),
    Student("xs48","女","二年级","二班",65,79,80),
    Student("xs49","男","二年级","二班",65,79,80)

  )

  /**
    * 输出结果
    * @param mapResult
    */
  def display(mapResult:Map[String,Any]):Unit = {
    mapResult.keys.foreach((key) =>{
      println(key)
      println(x = mapResult(key).toString)
    })
  }

  def main(args: Array[String]): Unit = {
    val studentService = new StudentServiceImpl()

    //计算各班级各科平均分
    //display(studentService.avgStatByClassAndCourse(studentList))

    //计算各班级各个分数阶段人数占比
    //display(studentService.ratioStatByClassAndCourse(studentList))

    //计算年级top20
    //display(studentService.top20StatByGrade(studentList))

    //计算年级各科男女状元
    val mapRes = studentService.firstRankStatByCourseAndSex(studentList)
    mapRes.keys.foreach(f = key => {
      println(key)
      val list = mapRes(key)
      println("\t\t语文  男状元：" + (if (list.head.maleStudent == null) "无" else s"${list(0).maleStudent.name} ${list(0).maleStudent.chineseScore}分 ") + " \t\t 女状元 ：" + (if (list(0).femaleStudent == null) "无" else s"${list(0).femaleStudent.name} ${list(0).femaleStudent.chineseScore}分 "))

      println("\t\t数学  男状元：" + (if (list(1).maleStudent == null) "无" else s"${list(1).maleStudent.name} ${list(1).maleStudent.mathScore}分 ") + " \t\t 女状元：" + (if (list(1).femaleStudent == null) "无" else s"${list(1).femaleStudent.name} ${list(1).femaleStudent.mathScore}分"))

      println("\t\t英语  男状元：" + (if (list(2).maleStudent == null) "无" else s"${list(2).maleStudent.name} ${list(2).maleStudent.englishScore}分 ") + " \t\t 女状元：" + (if (list(2).femaleStudent == null) "无" else s"${list(2).femaleStudent.name} ${list(2).femaleStudent.englishScore}分"))
    })
  }
}
