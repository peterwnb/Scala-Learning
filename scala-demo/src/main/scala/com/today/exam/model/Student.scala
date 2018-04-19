package com.today.exam.model

/**
  * 学生数据结构
  * @param name 学生姓名
  * @param sex 学生性别
  * @param grade 年级
  * @param className 班级
  * @param chineseScore 语文成绩
  * @param mathScore 数学成绩
  * @param englishScore 英文成绩
  */
 case class Student(name : String, sex : String, grade : String, className : String, chineseScore : Int, mathScore : Int, englishScore : Int){
  override def toString: String = {
    "[姓名："+this.name + ",性别："+this.sex + " ,年级："+this.grade + " , 班级：" + this.className + " , 语文成绩："+this.chineseScore +" , 数学成绩："+this.mathScore + " , 英语成绩："+this.englishScore+"]\r\n"
  }
}
