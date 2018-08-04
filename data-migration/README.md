# 迁移程序部署文档

  ## 1.环境配置

    1.java jdk 1.8 
    2.oracle连接系统参数配置
      DB_ORACLE_URL=jdbc:oracle:thin:@10.171.220.234:1521:ORCL
      DB_ORACLE_USER=cvs_ma
      DB_ORACLE_PASSWD=today888
    3.mysql连接系统参数配置
      DB_SUPPLIER_URL=jdbc:mysql://127.0.0.1/supplier_db?useUnicode=true&characterEncoding=utf8
      DB_SUPPLIER_USER=td_goods
      DB_SUPPLIER_PASSWD=36524@Today
      
      DB_GOODS_URL=jdbc:mysql://rm-bp107555813e52726o.mysql.rds.aliyuncs.com/td_goods?useUnicode=true&characterEncoding=utf8
      DB_GOODS_USER=td_goods
      DB_GOODS_PASSWD=36524@Today
      
      DB_MEMBER_URL=jdbc:mysql://125.88.153.75/member?useUnicode=true&characterEncoding=utf8
      MEMBER_USER=root
      MEMBER_PASSWD=today-36524
  ## 2.运行
  
    1.促销数据导入  
    第一次运行必须运行 com.today.PromotionMain 函数
     后续更新需要先运行com.today.PromotionUpdateMain 再运行 com.today.PromotionUpdateBarcodeMain
      
    java -cp promotion-migration.jar com.today.PromotionMain [catogary|goods|all] [11|12|all] [minArticleId]
       第一个参数：
        导入数据类型 【必填】
            catogary 类目
            goods 商品
            all 所有
      第二个参数：【必填】
        采购中心 
            11 华中采购中心
            10 华南采购中心
      第三个参数：【可选】
        最小的商品货号ID 为数字 在程序失败时日志最后的一条记录Id
    
    java -cp promotion-migration.jar com.today.PromotionUpdateMain
      无参数
    
    java -cp promotion-migration.jar com.today.PromotionUpdateBarcodeMain
      无参数
    