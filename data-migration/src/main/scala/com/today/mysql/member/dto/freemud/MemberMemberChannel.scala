package com.today.mysql.member.dto.freemud

case class MemberMemberChannel (

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
          deleteType:Int
)
