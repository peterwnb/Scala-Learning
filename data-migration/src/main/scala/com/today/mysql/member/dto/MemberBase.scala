package com.today.mysql.member.dto

/**
  * @author dapeng-tool
  */case class MemberBase (
   /**
   * 会员编号
   */
   MemberId : String,

   /**
   * 合作伙伴编号，关联basis_partner_base.PartnerId
   */
   PartnerId : String,

   /**
   * 手机号码
   */
   Mobile : Option[String],

   /**
   * 姓名
   */
   MemberName : Option[String],

   /**
   * 性别标识{1：男2：女}
   */
   SexFlag : Int,

   /**
   * 出生日期
   */
   Birthday : Option[java.sql.Timestamp],

   /**
   * 微信OpenId
   */
   WeixinOpenId : String,

   /**
   * 支付宝OpenId
   */
   AliOpenId : String,

   /**
   * 头像
   */
   Photo : String,

   /**
   * 标签，多个标签以半角空格间隔
   */
   Tags : String,

   /**
   * 积分
   */
   Score : Int,

   /**
   * 金币
   */
   Coin : Int,

   /**
   * 金币有效标识
   */
   CoinActiveFlag : Int,

   /**
   * 注册时间
   */
   RegisterTime : java.sql.Timestamp,

   /**
   * 会员地址信息
   */
   Address : Option[String],

   /**
   * 会员唯一标志，账号
   */
   Account : String,

   /**
   * 密码
   */
   Password : Option[String],

   /**
   * 会员余额
   */
   LeftValue : Double,

   /**
   * 渠道信息。{1: 微信支付，2:公众号，3:分享 ,4:二维码}
   */
   ChannelFlag : Int,

   /**
   * 会员标志（虚拟卡号）
   */
   MemberSign : String,

   /**
   * 会员微信头像url
   */
   ImageURL : String,

   /**
   * 注册渠道
   */
   RegisterChannelCode :String,

   /**
   * 注册门店
   */
   RegisterStoreCode : String,

   /**
   * 二维码标识
   */
   ScanCodeId : String,

   /**
   * 关联门店
   */
   RelationStoreCode : String,

   /**
   * 昵称
   */
   NickName : String,

   /**
   * 城市
   */

   City : Option[String],
     /**
       * 省份
       */

     Province : String,
/**
  * 区域
  */

   District : String

)
