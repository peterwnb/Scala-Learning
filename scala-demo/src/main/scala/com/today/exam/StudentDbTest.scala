package com.today.exam

import com.today.exam.model.Student
import com.today.exam.service.impl.StudentDbServiceImpl

object StudentDbTest {
  val service = new StudentDbServiceImpl();
  def main(args: Array[String]): Unit = {
    //importDataTest(MainTest.studentList)
    selectAllStudentInfoTest()
  }

  /**
    * 导入学生信息 以及 学生分数 信息
    * @param list
    */
  def importDataTest(list:List[Student]):Unit = {
    service.importDataToDb(list)
  }

  /**
    * 查询所有的学生信息
    */
  def selectAllStudentInfoTest():Unit = {
    val list = service.selectAllStudent()
    list.foreach(item =>{
      println(item)
    })
  }
}
