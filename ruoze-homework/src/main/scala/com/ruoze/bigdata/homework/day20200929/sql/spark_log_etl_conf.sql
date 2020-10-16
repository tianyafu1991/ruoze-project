/*
Navicat MySQL Data Transfer

Source Server         : hadoop
Source Server Version : 50726
Source Host           : hadoop:3306
Source Database       : ruozedata

Target Server Type    : MYSQL
Target Server Version : 50726
File Encoding         : 65001

Date: 2020-10-17 00:16:36
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for spark_log_etl_conf
-- ----------------------------
DROP TABLE IF EXISTS `spark_log_etl_conf`;
CREATE TABLE `spark_log_etl_conf` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `conf_name` varchar(255) DEFAULT NULL COMMENT '配置名称',
  `domain` varchar(255) DEFAULT NULL COMMENT '域名',
  `compress_codec` varchar(255) DEFAULT NULL COMMENT '压缩格式',
  `file_name_prefix` varchar(255) DEFAULT NULL COMMENT '文件名前缀',
  `file_name_time_format` varchar(255) DEFAULT NULL COMMENT '文件名的时间格式',
  `file_name_suffix` varchar(255) DEFAULT NULL COMMENT '文件名后缀',
  `log_content_fields` varchar(255) DEFAULT NULL COMMENT '日志文件所需字段',
  `fields_delimiter` varchar(255) DEFAULT NULL COMMENT '字段内容分隔符',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='spark日志解析配置表';

-- ----------------------------
-- Records of spark_log_etl_conf
-- ----------------------------
INSERT INTO `spark_log_etl_conf` VALUES ('1', 'my_custom_conf', 'ruozedata.com', 'gzip', 'access', 'yyMMddHH', 'log', 'ip,domain,city,isp', '$$$', '2020-10-17 00:13:56', '2020-10-17 00:13:56');
INSERT INTO `spark_log_etl_conf` VALUES ('2', 'my_custom_conf', 'ruoze.ke.qq.com', 'bzip2', null, 'HH', 'log', 'ip,url,province,city,isp', '\\t', '2020-10-17 00:16:28', '2020-10-17 00:16:28');
