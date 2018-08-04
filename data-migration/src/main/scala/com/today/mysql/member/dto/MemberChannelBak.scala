package com.today.mysql.member.dto

case class MemberChannelBak(

          Id:Long,
          PartnerId:String,
          MemberId:String,
          ChannelCode:Option[String],
          OpenId:String,
          CreateTime:Option[java.sql.Timestamp],
          SourceCode:Option[String],
          CardId:Option[String],
          CardCode:Option[String],
          CustomCardNO:Option[String],
          deleteType:Option[Int]

)
