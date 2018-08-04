package com.today.mysql.member.dto

case class OpenIdMidMap(
                       openId:String,
                       mid:Long,
                       source:String,
                       oldMemberId:String
                       )
