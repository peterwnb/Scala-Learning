package com.today.mysql.member.dto

/**
  * @author dapeng-tool
  */
case class MemberCoupon(


                         MemberId: String,

                         TypeCode: String,


                         CouponName: String,

                         StartTime: java.sql.Timestamp,


                         EndTime: java.sql.Timestamp,

                         Remark: String,

                         CreateTime: java.sql.Timestamp,

                         SourceFlag: Int,

                         IsUsed: Int,

                         UseTime: java.sql.Timestamp,

                         FkId: String,

                         SourceId: String,

                         CouponId: String,

                         RegisterChannelCode: String,

                         RegisterStoreCode: String,

                         ScanCodeId: String,

                       )
