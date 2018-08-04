-- cvs 的类目结构 oracle 转为 mysql
drop table if exists cvs_category;
CREATE TABLE `cvs_category` (
  `AC_CATEGORY_ID` varchar(7) NOT NULL comment '类别代码',
  `AC_CATEGORY_DESCRIPTION` varchar(20) comment '类别描述',
  `AC_RACK_TYPE` varchar(3) comment '货架分类',
  `AC_ORDER_TYPE` varchar(2) comment '订购方式',
  `AC_ARTICLE_TYPE` varchar(2) comment '商品类型',
  `AC_VAT_ID` varchar(2) comment '税率',
  `AC_ARTICLE_COUNT` int(11) comment '商品个数',
  `AC_FACECARD_FLAG` varchar(1) comment 'facecard打印标志',
  `AC_CRT_GROUP` varchar(3) comment 'CRT订购组',
  `AC_TYPE` varchar(2) comment '商品区分',
  `AC_CATEGORY_REMARK` varchar(100) comment '类别备注信息',
  `AC_GRADE` varchar(2) comment '类别层级',
  `AC_PARENT_CATEGORY_ID` varchar(7) comment '上一层类别代码',
  `AC_CATEGORY_STATUS` varchar(2) comment '类别状态',
  `AC_HQ_EFFECTIVE_DATE` DATE comment '总部生效日期',
  `AC_STORE_EFFECTIVE_DATE` DATE comment '店铺生效日期',
  `AC_CREATE_USER_ID` varchar(10) comment '创建员工号',
  `AC_CREATE_DATE` DATE comment '创建日期',
  `AC_UPDATE_USER_ID` varchar(10) comment '修改员工号',
  `AC_UPDATE_DATE` DATE comment '修改日期',
  `AC_RSV_STATUS` varchar(2) comment '备份状态',
  `AC_RSV_DATETIME` DATE comment '备份日期',
  `AC_SHOW_AREA` varchar(2) comment '陈列分类',
  `AC_SALE_FLAG` varchar(1) comment '销售标志',
  `AC_STORE_PO_FLAG` varchar(1) comment '店铺订购标志',
  `AC_SALE_RETURN_FLAG` varchar(1)comment '销售退货标志',
  PRIMARY KEY (`AC_CATEGORY_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=28 default CHARSET=utf8 comment='cvs商品类目表';


-- 更新parent_id
update category a,category b set a.parent_id = b.id where a.parent_code = b.code

-- 插入一条现有的类目结构数据供有一条销售属性 规格
INSERT INTO `category_db`.`category`(`id`, `hierarchy_id`, `parent_id`, `parent_name`, `parent_code`, `name`, `code`, `type`, `level`, `flag`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `remark`)
VALUES (101, 3, NULL, NULL, NULL, 'CVS导入数据A类目', '0001', 3, 1, b'0', 99, now(), 1, now(), 1, '');
INSERT INTO `category_db`.`category`(`id`, `hierarchy_id`, `parent_id`, `parent_name`, `parent_code`, `name`, `code`, `type`, `level`, `flag`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `remark`)
VALUES (102, 3, 101, 'CVS导入数据A类目', '0001', 'CVS导入数据B类目', '00010001', 3, 2, b'0', 99, now(), 1, now(), 1, '');
INSERT INTO `category_db`.`category`(`id`, `hierarchy_id`, `parent_id`, `parent_name`, `parent_code`, `name`, `code`, `type`, `level`, `flag`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `remark`)
VALUES (103, 3, 102, 'CVS导入数据B类目', '00010001', 'CVS导入数据C类目', '000100010001', 1, 3, b'0', 99, now(), 1, now(), 1, '');

-- 属性
INSERT INTO `category_db`.`attribute`(`id`, `name`, `type`, `status`, `created_at`, `created_by`, `updated_at`, `updated_by`, `remark`)
VALUES (101, '包装规格', 3, 99, now(), 1, now(), 1, '导入数据的销售属性');

-- 类目属性关联
INSERT INTO `category_db`.`category_attribute`(`id`, `category_id`, `attribute_id`, `category_attribute_type`, `is_required`, `data_type`, `flag`, `created_at`, `created_by`, `updated_at`, `updated_by`, `remark`)
VALUES (101, 103, 101, 2, 1, 1, b'0', now(), 1, now(), 1, '');


