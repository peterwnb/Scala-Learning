package com.today.code

object SeqListTest {
  def main(args: Array[String]): Unit = {
    val thrill = List("Will","fill","until")
    //输出长度为4的元素的个数
    println(thrill.count(s => s.length == 4))
    //返回去掉了thrill的头两个元素的列表
    println(thrill.drop(2)) // -- List('until')
    //返回去掉了后边两个元素之后的列表
    println(thrill.dropRight(2))  //-- List("Will")

    println(thrill.exists(s => s == "until"))  //true

    //过滤出来长度等于4的元素的列表
    println("filter--> "+thrill.filter(s => s.length ==4))   //List("Will","fill")
    println("filterNot--> "+thrill.filterNot(s=>s.length==4))

    //判断是否thrill元素是否全部以 "l" 结尾
    println(thrill.forall(s => s.endsWith("l")))  //true

    thrill.foreach(s => print(s +" "))  //Will fill until

    println()

    thrill.foreach(println)

    println("head--> " +thrill.head)  //Will

    println("tail--> "+thrill.tail) //???

    println("last--> "+thrill.last)  //until

    println("init--> "+thrill.init)  //返回列表出了最后一个元素之外的所有元素  List("Will","fill")

    println("isEmpty--> "+thrill.isEmpty)

    println("length--> "+ thrill.length)

    println(thrill.map(s => s+"y")) //List("Willy" , "filly" ,"untily")  //全部元素+"y"

    println("mkString--> "+thrill.mkString(",")) //逗号拼接成一个字符串
  }
}
