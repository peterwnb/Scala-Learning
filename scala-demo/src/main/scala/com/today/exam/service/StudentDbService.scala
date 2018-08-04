package com.today.exam.service

import com.today.exam.model.Student
import com.today.exam.vo._

trait StudentDbService {
  /**
    * 导入学生和成绩信息到数据库
    * @param list
    * @return
    */
  def importDataToDb(list:List[Student]):Int

  def selectAllStudent():List[StudentDto]

  def selectStudentWithScore():List[Student]
}
