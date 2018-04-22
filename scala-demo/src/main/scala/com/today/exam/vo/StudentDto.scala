package com.today.exam.vo

case class StudentDto (id:String,name:String,sex:String,grade:String,className:String){
  override def toString: String = {
    "id="+id +" , name="+name +" , sex="+sex +" , grade="+grade + " , className="+className
  }
}
