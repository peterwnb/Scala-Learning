package com.today.common

import javax.annotation.Resource
import javax.sql.DataSource

object MemberDataSource {
  var mysqlData: DataSource = _
}
class MemberDataSource {
  /**
    * 使用 事务控制的dataSource
    *
    */
  @Resource(name = "tx_member_dataSource")
  def setMysqlData(mysqlData: DataSource): Unit = {
    MemberDataSource.mysqlData = mysqlData
  }
}
