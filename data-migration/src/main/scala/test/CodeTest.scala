package test

object CodeTest {
  def main(args: Array[String]): Unit = {
    val now = System.currentTimeMillis()
    val count = 1000
    println(f"$now%tT process $count lines")




//    val list = List("1101","1102","1103").filterNot(p => p.equals("1101"))
//    println(list.mkString(","))

    val userList = List(
      User("1001","15902783101","张三",""),
      User("1002","15902783101","李四",null),
      User("1003","15902783101","王五","dasdasd"),
      User("1004","15902783102","赵六","_asdasdff"),
      User("1005","15902783102","田七","HSHJ_ajsda")
    )

    for(i <- 0 to 100) yield  {
      val n = i%10
      val phone = f"159027831$n%02d"
      println(phone)
    }

    userList.groupBy(_.mobile).map(s =>{
      println(s._1)
      s._2.foreach(item =>{
        println(item)
      })
    })

//    println(userList.map(_.id))
//
//    userList.groupBy(_.mobile).map(s=>{
//      println("分组信息："+s._1)
//      val us = s._2
//      println(us)
//      val target = us.filter(p => p.openId!= null && !p.openId.equals("")).head;
//      println(target)
//    })
  }

}
case class User(id:String,mobile:String,name:String,openId:String)
