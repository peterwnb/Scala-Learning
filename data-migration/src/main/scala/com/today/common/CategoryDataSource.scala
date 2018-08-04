package com.today.common

import javax.annotation.Resource
import javax.sql.DataSource


object CategoryDataSource {
  var mysqlData: DataSource = _
}

class CategoryDataSource {
  /**
    * 使用 事务控制的dataSource
    *
    */
  @Resource(name = "tx_category_dataSource")
  def setMysqlData(mysqlData: DataSource): Unit = {
    CategoryDataSource.mysqlData = mysqlData
  }

}
