package com.today.common

import javax.annotation.Resource
import javax.sql.DataSource

/**
  * 非码数据源
  */
object FreemudDataSource {
  var mysqlData: DataSource = _
}

class FreemudDataSource {

  @Resource(name = "tx_freemud_dataSource")
  def setMysqlData(mysqlData: DataSource): Unit = {
    FreemudDataSource.mysqlData = mysqlData
  }

}