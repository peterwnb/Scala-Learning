package com.today.mysql.member.dto

case class MemberAccount (
                         memberId:Long,            //mid
                         oldMemberId:String,       //oldMemberId
                         mobilePhone:String,       //手机号
                         memberName:String,        //会员昵称
                         memberHeadUrl:String,     //用户头像
                         openId:String,            //微信openID
                         accountId:Long,           //关联的账户ID
                         cardBalance:BigDecimal,   //会员礼品卡总余额
                         couponCount:Int,          //优惠券张数
                         memberScore:Int           //会员积分
                         )
