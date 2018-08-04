//package com.today.main.commons
//
//import com.today.BaseMain
//import com.today.common.{OracleDataSource, SupplierDataSource}
//import com.today.mysql.supplier.dto.{SupplierPurchasingDistribution, SupplierPurchasingLogistics, SupplierPurchasingLogisticsShipping, SupplierPurchasingLogisticsStock}
//import org.slf4j.LoggerFactory
//import wangzx.scala_commons.sql._
//
///**
//  * 导入采购中心(PurchasingCentre)，物流中心(logistics_centre)，订货便次(shipping_method)，库存方式(stock_type)，配送区域以及它们的关联关系
//  * 采购中心 和 订货便次作为字典数据保存 value为原cvs的id值
//  * 物流中心 作为字典数据保存 value为原cvs的物流中心编号
//  * 库存方式已作为一个枚举数据 value为原cvs的vuale
//  * 物流中心的订购便次cvs表：DC_DISTRIBUTION_TYPE_REFERENCE
//  * 物流中心的库存方式cvs表：DC_DISTRIBUTION_TYPE_REFERENCE
//  */
//object PurchasingLogistics extends BaseMain {
//
//  private val logger = LoggerFactory.getLogger(getClass)
//
//  def main(args: Array[String]): Unit = {
//    val start = System.currentTimeMillis()
//
//    clearTable()
//
//    processPurchasingLogistics()
//
//    processPurchasingLogisticsShipping()
//
//    processPurchasingLogisticsStock()
//
//    processPurchasingDistrict()
//
//    logger.info("导入采购中心基础数据完成耗时" + (System.currentTimeMillis() - start))
//  }
//
//  def clearTable(): Unit ={
//    SupplierDataSource.mysqlData.executeUpdate(sql"delete from supplier_purchasing_logistics")
//    SupplierDataSource.mysqlData.executeUpdate(sql"delete from supplier_purchasing_logistics_shipping")
//    SupplierDataSource.mysqlData.executeUpdate(sql"delete from supplier_purchasing_logistics_stock")
//    SupplierDataSource.mysqlData.executeUpdate(sql"delete from supplier_purchasing_district")
//  }
//
//  /**
//    * 采购物流中心
//    */
//  def processPurchasingLogistics(): Unit ={
//    val sql =
//      sql"""
//           select pc_Id as purchasing_centre_value,pc_Description as purchasing_centre_name,
//                dp_Dc_Id as logistics_centre_value,dp_Dc_Name as logistics_centre_name,
//                DP_DC_TYPE as "type",
//                to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as created_at,0 as created_by, to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as updated_at,0 as updated_by,'' as remark
//                from Dc_Profile
//                left join Purchasing_Center on dp_Purchasing_Center_Id = pc_Id
//                where DP_DC_STATUS = 10
//         """
//    OracleDataSource.oracleData.eachRow[SupplierPurchasingLogistics](sql)(item => {
//      SupplierDataSource.mysqlData.executeUpdate(
//        sql"""
//             insert into supplier_purchasing_logistics
//             set
//             purchasing_centre_value = ${item.purchasingCentreValue},
//             purchasing_centre_name = ${item.purchasingCentreName},
//             logistics_centre_value = ${item.logisticsCentreValue},
//             logistics_centre_name = ${item.logisticsCentreName},
//             `type` = ${item.`type`},
//             created_at = now(),
//             created_by = 0,
//             updated_by = 0,
//             remark = '后台导入'
//           """)
//    })
//
//  }
//
//  /**
//    * 物流中心的订货便次信息
//    */
//  def processPurchasingLogisticsShipping(): Unit = {
//    val sql =
//      sql"""
//           select pc_Id as purchasing_centre_value,pc_Description as purchasing_centre_name,
//           	dp_Dc_Id as logistics_centre_value,dp_Dc_Name as logistics_centre_name,
//           	ddtr_Delivery_Distribution as shipping_method_value,DTC_DESCRIPTION as shipping_method_name,
//           	to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as created_at,0 as created_by, to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as updated_at,0 as updated_by,'' as remark
//           	from DC_DISTRIBUTION_TYPE_REFERENCE
//           	left join Dc_Profile on ddtr_Dc_Id = dp_Dc_Id
//           	left join Purchasing_Center on dp_Purchasing_Center_Id = pc_Id
//           	left join sys_distribution_type_code on DTC_ID = DDTR_DELIVERY_DISTRIBUTION , dual
//           	where DDTR_RECORD_STATUS = 10
//         """
//    OracleDataSource.oracleData.eachRow[SupplierPurchasingLogisticsShipping](sql)(item => {
//      SupplierDataSource.mysqlData.executeUpdate(
//        sql"""
//             insert into supplier_purchasing_logistics_shipping
//             set
//             purchasing_centre_value = ${item.purchasingCentreValue},
//             purchasing_centre_name = ${item.purchasingCentreName},
//             logistics_centre_value = ${item.logisticsCentreValue},
//             logistics_centre_name = ${item.logisticsCentreName},
//             shipping_method_value = ${item.shippingMethodValue},
//             shipping_method_name = ${item.shippingMethodName},
//             created_at = now(),
//             created_by = 0,
//             updated_by = 0,
//             remark = '后台导入'
//           """)
//    })
//  }
//  /**
//    * 物流中心的库存方式
//    */
//  def processPurchasingLogisticsStock(): Unit = {
//    val sql =
//      sql"""
//           select pc_Id as purchasing_centre_value,pc_Description as purchasing_centre_name,
//           	dp_Dc_Id as logistics_centre_value,dp_Dc_Name as logistics_centre_name,
//           	dsmr_stock_method as stock_type,
//           	to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as created_at,0 as created_by, to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as updated_at,0 as updated_by,'' as remark
//           	from DC_STOCK_METHOD_REFERENCE
//           	left join Dc_Profile on DSMR_Dc_Id = dp_DC_ID
//           	left join Purchasing_Center on dp_Purchasing_Center_Id = pc_Id
//           	where DSMR_RECORD_STATUS = 10
//         """
//    OracleDataSource.oracleData.eachRow[SupplierPurchasingLogisticsStock](sql)(item => {
//      SupplierDataSource.mysqlData.executeUpdate(
//        sql"""
//             insert into supplier_purchasing_logistics_stock
//             set
//             purchasing_centre_value = ${item.purchasingCentreValue},
//             purchasing_centre_name = ${item.purchasingCentreName},
//             logistics_centre_value = ${item.logisticsCentreValue},
//             logistics_centre_name = ${item.logisticsCentreName},
//             stock_type = ${item.stockType},
//             created_at = now(),
//             created_by = 0,
//             updated_by = 0,
//             remark = '后台导入'
//           """)
//    })
//  }
//
//  /**
//    * 采购中心的配送区域
//    */
//  def processPurchasingDistrict(): Unit ={
//    val sql =
//      sql"""
//           select pc_Id as purchasing_centre_value,pc_Description as purchasing_centre_name,
//           		DSC_ID as district_value,DSC_DESCRIPTION as district_name,
//           		to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as created_at,0 as created_by,
//           		to_char(sysdate,'yyyy-mm-dd hh24:mi:ss') as updated_at,0 as updated_by,
//           		'' as remark
//           		from DISTRICT_CODE
//           		left join Purchasing_Center on DSC_PURCHASING_CENTER_ID = pc_Id,dual
//           		where DSC_RECORD_STATUS = 10
//         """
//    OracleDataSource.oracleData.eachRow[SupplierPurchasingDistribution](sql)(item => {
//      SupplierDataSource.mysqlData.executeUpdate(
//        sql"""
//             insert into supplier_purchasing_district
//             set
//             purchasing_centre_value = ${item.purchasingCentreValue},
//             purchasing_centre_name = ${item.purchasingCentreName},
//             district_value = ${item.districtValue},
//             district_name = ${item.districtName},
//             created_at = now(),
//             created_by = 0,
//             updated_by = 0,
//             remark = '后台导入'
//           """)
//    })
//
//  }
//}
