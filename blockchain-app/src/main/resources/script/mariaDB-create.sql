/*
Navicat MySQL Data Transfer

Source Server         : localhost_3346
Source Server Version : 50509
Source Host           : localhost:3346
Source Database       : higgs

Target Server Type    : MYSQL
Target Server Version : 50509
File Encoding         : 65001

Date: 2018-08-22 15:18:36
*/


SET FOREIGN_KEY_CHECKS = 0;
SET GLOBAL innodb_file_per_table=1;
SET GLOBAL innodb_file_format=Barracuda;

-- ----------------------------
-- Table structure for t_block
-- ----------------------------
DROP TABLE IF EXISTS `t_block`;
CREATE TABLE `t_block` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `block_hash` varchar(64) NOT NULL,
  `data` text NOT NULL,
  `height` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_block_hash` (`block_hash`),
  KEY `idx_block_height` (`height`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 KEY_BLOCK_SIZE=8;

-- ----------------------------
-- Table structure for t_block_index
-- ----------------------------
DROP TABLE IF EXISTS `t_block_index`;
CREATE TABLE `t_block_index` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `block_hash` varchar(64) NOT NULL,
  `height` int(11) NOT NULL,
  `is_best` int(11) NOT NULL,
  `miner_address` varchar(34) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_block_index_block_hash` (`block_hash`),
  KEY `idx_block_index_height` (`height`),
  KEY `idx_block_index_miner_address` (`miner_address`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 KEY_BLOCK_SIZE=8;

-- ----------------------------
-- Table structure for t_dpos
-- ----------------------------
DROP TABLE IF EXISTS `t_dpos`;
CREATE TABLE `t_dpos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `addresses` varchar(2000) NOT NULL,
  `sn` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dpos_sn` (`sn`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 KEY_BLOCK_SIZE=8;

-- ----------------------------
-- Table structure for t_score
-- ----------------------------
DROP TABLE IF EXISTS `t_score`;
CREATE TABLE `t_score` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `address` varchar(64) NOT NULL,
  `score` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_score_address` (`address`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 KEY_BLOCK_SIZE=8;

-- ----------------------------
-- Table structure for t_transaction_index
-- ----------------------------
DROP TABLE IF EXISTS `t_transaction_index`;
CREATE TABLE `t_transaction_index` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `block_hash` varchar(64) NOT NULL,
  `transaction_hash` varchar(64) NOT NULL,
  `transaction_index` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_transaction_index_hash` (`transaction_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 KEY_BLOCK_SIZE=8;
-- ----------------------------
-- Table structure for t_utxo
-- ----------------------------
DROP TABLE IF EXISTS `t_utxo`;
CREATE TABLE `t_utxo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `amount` varchar(16) NOT NULL,
  `currency` varchar(16) NOT NULL,
  `lock_script` varchar(64) NOT NULL,
  `out_index` int(11) NOT NULL,
  `script_type` int(11) NOT NULL,
  `transaction_hash` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `T_UTXO_TRANSACTION_HASH_IDX` (`transaction_hash`,`out_index`),
  KEY `T_UTXO_LOCK_SCRIPT_IDX` (`lock_script`,`currency`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 KEY_BLOCK_SIZE=8;

-- ----------------------------
-- Table structure for t_witness
-- ----------------------------
DROP TABLE IF EXISTS `t_witness`;
CREATE TABLE `t_witness` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `address` varchar(64) DEFAULT NULL,
  `http_port` int(11) DEFAULT NULL,
  `pub_key` varchar(128) DEFAULT NULL,
  `socket_port` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_pub_key` (`pub_key`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 KEY_BLOCK_SIZE=8;