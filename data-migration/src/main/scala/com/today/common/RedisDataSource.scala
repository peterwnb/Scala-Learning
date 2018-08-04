package com.today.common

import javax.annotation.Resource

import org.springframework.stereotype.Service

object RedisDataSource {
  var redisData: RedisResource = _
}


@Service
class RedisDataSource {
  /**
    * 使用 事务控制的dataSource
    *
    */
  @Resource(name = "redisResource")
  def setMysqlData(redisData: RedisResource): Unit = {
    RedisDataSource.redisData = redisData
  }




}