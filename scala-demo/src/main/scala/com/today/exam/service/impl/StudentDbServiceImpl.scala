package com.today.exam.service.impl

import com.today.exam.dao.UserDataDao
import com.today.exam.model.Student
import com.today.exam.service.StudentDbService
import com.today.exam.vo.{StudentDto, StudentScoreDto}

class StudentDbServiceImpl extends StudentDbService{
  override def importDataToDb(list: List[Student]): Int = {
    UserDataDao.insertStudent(list)
    val dbList = selectAllStudent();
    val scoreList : List[StudentScoreDto] = for(item <- dbList) yield{
      val head = list.filter(s=> s.name.trim.equals(item.name.trim)).head
      StudentScoreDto(item.id,head.chineseScore,head.mathScore,head.englishScore,0)
    }
    UserDataDao.insertScore(scoreList)
  }

  override def selectAllStudent(): List[StudentDto] = {
    UserDataDao.selectAllStudent()
  }

  override def selectStudentWithScore(): List[Student] = {
    UserDataDao.selectStudentWithScore()
  }
}
