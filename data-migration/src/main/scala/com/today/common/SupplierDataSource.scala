package com.today.common

import javax.annotation.Resource
import javax.sql.DataSource

object SupplierDataSource {
  var mysqlData: DataSource = _
}
class SupplierDataSource {
  /**
    * 使用 事务控制的dataSource
    *
    */
  @Resource(name = "tx_supplier_dataSource")
  def setMysqlData(mysqlData: DataSource): Unit = {
    SupplierDataSource.mysqlData = mysqlData
  }

}
