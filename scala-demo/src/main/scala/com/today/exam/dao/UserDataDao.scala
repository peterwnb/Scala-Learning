
package com.today.exam.dao

import com.today.exam.model.Student
import com.today.exam.util.DBUtil
import com.today.exam.vo.{StudentDto, StudentScoreDto}
import wangzx.scala_commons.sql._

/**
  * 用户数据DAO操作
  */
object UserDataDao {
  /**
    * 将学生数据插入到学生数据表里面去
    * @param list
    * @return
    */
  def insertStudent(list :List[Student]):Int= {
    list.map(item =>{
      DBUtil.dataSource.executeUpdate(sql"""insert into student set name = ${item.name}, sex = ${item.sex}, grade = ${item.grade}, class_name = ${item.className}""")
    }).size
  }

  def insertScore(list:List[StudentScoreDto]):Int = {
    list.map(item =>{
      DBUtil.dataSource.executeUpdate(
        sql"""
              insert
                into
              student_score
              set student_id = ${item.studentId},
              chinese_score = ${item.chineseScore},
              math_score = ${item.mathScore},
              english_score = ${item.englishScore}
          """)
    }).size
  }

  def selectAllStudent():List[StudentDto]= {
    DBUtil.dataSource.rows[StudentDto]("select id,name,sex,grade,class_name from student")
  }

  def selectStudentWithScore():List[Student] = {
    DBUtil.dataSource.rows[Student]("SELECT st.`name`,st.sex,st.grade,st.class_name,sc.chinese_score,sc.math_score,sc.english_score FROM `student` st LEFT JOIN student_score sc on st.id = sc.student_id")
  }
}