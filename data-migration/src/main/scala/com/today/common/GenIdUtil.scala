package com.today.common

import java.text.DecimalFormat

import com.github.dapeng.core.SoaException
import com.today.soa.idgen.scala.cache.IDCacheClient



object GenIdUtil {

  val SUPPLIER = "supplier_id"
  val SUPPLIER_DRAFT = "supplier_draft_id"
  val SUPPLIER_NO = "supplier_no"
  val SUPPLIER_VERSION = "supplier_version"
  val SUPPLIER_CERTIFICATE = "supplier_certificate_id"
  val SUPPLIER_CERTIFICATE_DRAFT = "supplier_certificate_draft_id"
  val SUPPLIER_LOGISTICS = "supplier_logistics_id"
  val SUPPLIER_LOGISTICS_DRAFT = "supplier_logistics_draft_id"
  val SUPPLIER_JOURNAL = "supplier_journal_id"
  val SUPPLIER_GOODS = "supplier_goods_id"
  val SUPPLIER_GOODS_DRAFT = "supplier_goods_draft_id"
  val GOODS = "goods_id"
  val SKU = "sku_id"
  val SKU_VERSION = "sku_version"
  val SKU_NO = "sku_no"
  val SKU_DRAFT = "sku_draft_id"
  val SKU_BARCODE = "sku_barcode_id"
  val SKU_BARCODE_DRAFT = "sku_barcode_draft_id"
  val GOODS_JOURNAL = "goods_journal_id"
  val MEMBER = "member"
  val MEMBER_COUPON = "member_coupon"
  val MEMBER_CODE = "member_code"
  val CATEGORY = "category_id"
  val RELATE_CATEGORY = "category_related_id"
  val ATTRIBUTE = "attribute_id"
  val ATTRIBUTE_VALUE = "attribute_value_id"
  val CATEGORY_ATTRIBUTE = "category_attribute_id"
  val CATEGORY_ATTRIBUTE_VALUE = "category_attribute_value_id"
  val MEMBER_CARD_ACCOUNT = "member_card_account"
  val CARD_BASE = "card_base"
  val CARD_CONSUME = "card_consume"
  val MEMBER_ORDER = "member_order"
  val versionFormat = new DecimalFormat("V000000000")

  lazy val idService = new IDCacheClient
  //idService.getId("sku_id")

  /**
    * 获取主键id
    *
    * @param bizTag
    * @return
    */
  def getId(bizTag: String): Long = {
    //    System.setProperty("soa_zookeeper_host","123.206.103.113:2181")
    try{

      idService.getId(bizTag)
    }catch {
      case e:SoaException =>
        if("Error-Core-003".equals(e.getCode)) {
          getId(bizTag)
        }else{
          throw e
        }
    }
  }

  /**
    * 获取主键id
    *
    * @param bizTag
    * @return
    */
  def getIdByCount(bizTag: String,count:Int): Long = {
    //    System.setProperty("soa_zookeeper_host","123.206.103.113:2181")
    try{
      idService.getId(bizTag)
    }catch {
      case e:SoaException =>
        if("Error-Core-003".equals(e.getCode)) {
          getId(bizTag)
        }else{
          throw e
        }
    }
  }

  /**
    * 定制唯一标识
    *
    * @param tag
    * @return
    */
  def getCustomId(tag: String): String = {
    //     System.setProperty("soa_zookeeper_host","123.206.103.113:2181")
    val long = idService.getId(tag)
    tag match {
      case SUPPLIER_VERSION =>
        s"version_${long.toString}"
      case SUPPLIER_NO =>
        s"S_000000000${long.toString}" //datetime+long
      case SKU_VERSION =>
        s"version_${long.toString}"
      case SKU_NO =>
        s"S_000000000${long.toString}" //datetime+long
      case _ => long.toString
    }
  }


  def main(args: Array[String]): Unit = {
  }
}
