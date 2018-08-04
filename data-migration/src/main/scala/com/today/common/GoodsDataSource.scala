package com.today.common

import javax.annotation.Resource
import javax.sql.DataSource


object GoodsDataSource {
  var mysqlData: DataSource = _
}
class GoodsDataSource {
  /**
    * 使用 事务控制的dataSource
    *
    */
  @Resource(name = "tx_goods_dataSource")
  def setMysqlData(mysqlData: DataSource): Unit = {
    GoodsDataSource.mysqlData = mysqlData
  }

}
