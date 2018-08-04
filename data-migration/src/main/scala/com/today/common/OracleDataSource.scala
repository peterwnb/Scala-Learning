package com.today.common

import javax.annotation.Resource
import javax.sql.DataSource

object OracleDataSource {
  var oracleData: DataSource = _
}

class OracleDataSource {
  /**
    * 使用 事务控制的dataSource
    *
    */
  @Resource(name = "tx_oracle_dataSource")
  def setOracleData(oracleData: DataSource): Unit = {
    OracleDataSource.oracleData = oracleData
  }

}
