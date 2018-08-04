package com.today.mysql.member.dto

/**
  * @author dapeng-tool
  */case class Member (
   /**
   * 利用主键策略生成的唯一键
   */
id : Long,

   /**
   * 根据规则引擎生成
   */
memberCode : String,

   /**
   * 会员名称
   */
memberName : String,

   /**
   * 会员密码
   */
memberPassword : String,

   /**
   * 积分
   */
memberScore : Int,

   /**
   * 用户头像url
   */
memberHeadUrl : String,

   /**
   * 加密盐
   */
memberSalt : String,

   /**
   * 用户手机号
   */
mobilePhone : String,

   /**
   * 性别,0:男(male);1:女(female)
   */
sex : Int,

   /**
   * 用户状态,1:正常(normal);2:冻结(frozen)
   */
memberStatus : Int,

   /**
   * 会员类型,1:正常(支付会员);2:冻结(认证会员)
   */
memberType : Int,

   /**
   * 注册渠道来源
   */
registerSource : Int,

   /**
   * 注册城市，取字典表id
   */
registerCityId : Int,

   /**
   * 注册城市名称，做冗余
   */
registerCityName : String,

   /**
   * 会员生日
   */
memberBirthday : java.sql.Timestamp,

   /**
   * 注册门店id
   */
registerStoreId : Long,

   /**
   * 注册时间
   */
registerTime : java.sql.Timestamp,

   /**
   * 电子邮箱
   */
email : String,

   /**
   * 微信openid
   */
wechatOpenId : String,

   /**
   * 微信unionid
   */
wechatUnionId : String,

   /**
   * 支付宝openid
   */
alipayOpenId : String,

   /**
   * 最后一次登录时间
   */
lastLoginTime : java.sql.Timestamp,

   /**
   * 创建时间
   */
createdAt : java.sql.Timestamp,

   /**
   * 特指后台创建人(公司员工 id)
   */
createdBy : Int,

   /**
   * 更新时间
   */
updatedAt : java.sql.Timestamp,

   /**
   * 特指后台更新人(公司员工 id)
   */
updatedBy : Int,

   /**
   * 备注
   */

remark : String,

oldMemberId: Option[String]= None,

old_memberId:String
)
