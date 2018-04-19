package com.today.exam.service

import com.today.exam.model.Student
import com.today.exam.vo.{AvgResult, CourseRatioResult, FirstRankStatResult, Top5Result}

/**
  * 学生服务接口
  */
trait StudentService {
  /**
    * 统计top5数据
    * @param studentList
    * @return
    */
  def top5Stat(studentList :List[Student]) : Map[String,Top5Result]

  /**
    * 统计每个班级各门课程的平均分
    * @param studentList
    * @return
    */
  def avgStatByClassAndCourse(studentList:List[Student]):Map[String,AvgResult]

  /**
    * 统计每个班级 三科各等级人数比例
    * 优:>=90;
    * 良:>=75 and <90
    * 及格:>=60,<75;
    * 不及格:<60
    * @param studentList
    * @return
    */
  def ratioStatByClassAndCourse(studentList:List[Student]):Map[String,CourseRatioResult]

  /**
    * 计算年级前二十名的同学
    * @param studentList
    * @return
    */
  def top20StatByGrade(studentList:List[Student]):Map[String, List[Student]]

  /**
    * 统计每个年级 每个学科 男 和 女 的状元
    * @param studentList
    * @return -> grade+sex+courseName
    */
  def firstRankStatByCourseAndSex(studentList:List[Student]):Map[String,List[FirstRankStatResult]]
}
