package com.today.exam.util

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
object DBUtil {

  val dataSource = {
    val ds = new MysqlDataSource();
    ds.setURL(s"jdbc:mysql://localhost:3306/test?user=root&useUnicode=true&characterEncoding=utf-8")
    ds.setPassword("admin")
    ds.setUser("root")
    ds
  }

  def getDataSource () : MysqlDataSource = {
    return dataSource
  }
}
