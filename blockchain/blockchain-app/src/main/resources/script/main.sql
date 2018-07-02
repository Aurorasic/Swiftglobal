/*
Navicat SQLite Data Transfer

Source Server         : 2
Source Server Version : 30808
Source Host           : :0

Target Server Type    : SQLite
Target Server Version : 30808
File Encoding         : 65001

Date: 2018-06-30 18:44:46
*/

PRAGMA foreign_keys = OFF;
-- ----------------------------
-- Table structure for sqlite_sequence
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_block";
CREATE TABLE "t_block" (
"block_hash"  TEXT(64) NOT NULL,
"height"  INTEGER NOT NULL,
"data"  TEXT NOT NULL
);

-- ----------------------------
-- Records of t_block
-- ----------------------------

-- ----------------------------
-- Table structure for t_block_index
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_block_index";
CREATE TABLE "t_block_index" (
"height"  INTEGER NOT NULL,
"block_hash"  TEXT(64) NOT NULL,
"is_best"  INTEGER NOT NULL,
"miner_address"  TEXT(34) NOT NULL
);

-- ----------------------------
-- Records of t_block_index
-- ----------------------------

-- ----------------------------
-- Table structure for t_dpos
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_dpos";
CREATE TABLE "t_dpos" (
"sn"  INTEGER NOT NULL,
"addresses"  TEXT(100) NOT NULL,
PRIMARY KEY ("sn")
);

-- ----------------------------
-- Records of t_dpos
-- ----------------------------

-- ----------------------------
-- Table structure for t_peer
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_peer";
CREATE TABLE "t_peer" (
"pub_key"  TEXT(66) NOT NULL,
"id"  TEXT(34) NOT NULL,
"ip"  TEXT(15) NOT NULL,
"socket_port"  INTEGER NOT NULL,
"http_port"  INTEGER NOT NULL,
"version"  INTEGER NOT NULL,
"signature"  TEXT(88) NOT NULL,
"retry"  INTEGER NOT NULL
);

-- ----------------------------
-- Records of t_peer
-- ----------------------------

-- ----------------------------
-- Table structure for t_score
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_score";
CREATE TABLE "t_score" (
"address"  TEXT(34) NOT NULL,
"score"  INTEGER NOT NULL,
PRIMARY KEY ("address")
);

-- ----------------------------
-- Records of t_score
-- ----------------------------

-- ----------------------------
-- Table structure for t_spent_transaction_out_index
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_spent_transaction_out_index";
CREATE TABLE "t_spent_transaction_out_index" (
"pre_transaction_hash"  TEXT NOT NULL,
"out_index"  INTEGER NOT NULL DEFAULT -1,
"now_transaction_hash"  TEXT NOT NULL
);

-- ----------------------------
-- Records of t_spent_transaction_out_index
-- ----------------------------

-- ----------------------------
-- Table structure for t_transaction_index
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_transaction_index";
CREATE TABLE "t_transaction_index" (
"transaction_hash"  TEXT NOT NULL,
"block_hash"  TEXT(64) NOT NULL,
"transaction_index"  INTEGER NOT NULL
);

-- ----------------------------
-- Records of t_transaction_index
-- ----------------------------

-- ----------------------------
-- Table structure for t_utxo
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_utxo";
CREATE TABLE "t_utxo" (
"transaction_hash"  TEXT NOT NULL,
"out_index"  INTEGER NOT NULL,
"amount"  TEXT(16) NOT NULL,
"currency"  TEXT(8) NOT NULL,
"script_type"  INTEGER NOT NULL,
"lock_script"  TEXT NOT NULL
);

-- ----------------------------
-- Records of t_utxo
-- ----------------------------

-- ----------------------------
-- Table structure for t_witness
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_witness";
CREATE TABLE [t_witness](
  [id] INTEGER PRIMARY KEY ASC AUTOINCREMENT, 
  [pub_key] VARCHAR(32), 
  [address] VARCHAR(50), 
  [socket_port] INTEGER, 
  [http_port] INTEGER);

-- ----------------------------
-- Records of t_witness
-- ----------------------------
INSERT INTO "main"."t_witness" VALUES (1, '0377b85fbc137825bac7d933faf7b9807579c62afaf2cd462cc471a1ea2b14ed90', '192.168.10.252', 8000, 8081);
INSERT INTO "main"."t_witness" VALUES (2, '03faab97fae96d4c492dd1bc0764c5a96a8b582c6ca4b41a583de1367b15d95812', '192.168.10.252', 8001, 8082);
INSERT INTO "main"."t_witness" VALUES (3, '031107ce9ca6db21b8732893873a0a5afb8f393601148acce9402bbab8562709a7', '192.168.10.252', 8003, 8083);
INSERT INTO "main"."t_witness" VALUES (4, '0367a2279fc0910c3feca555461ddda7f9173f74da99e454fcc2f36d0bb4feff6a', '192.168.10.252', 8004, 8084);
INSERT INTO "main"."t_witness" VALUES (5, '024d2913d1390e5fcb74567291fe1cb3f7e53bac1fda5703e16df0b9df1fbc5e38', '192.168.10.200', 8000, 8080);
INSERT INTO "main"."t_witness" VALUES (6, '02f9d158b8227bed46d916454be5cd2140d0b3e4d1f569a7c542f27e44d5ba4d43', '192.168.10.200', 8001, 8081);
INSERT INTO "main"."t_witness" VALUES (7, '03e14a63a0c34630a174e8df47a65bb0da4b540411e1daed43d00c30c1da93a17e', '192.168.10.200', 8002, 8082);
INSERT INTO "main"."t_witness" VALUES (8, '02a7aaeb38529b367c24f9d255cbc072ef2d339af0ebe71ae9723cc89f2f56acb2', '192.168.10.200', 8003, 8083);
INSERT INTO "main"."t_witness" VALUES (9, '0381ce03c0211a871a7a23bda3210c475eeda843503549f89b53e79c3e8f88a644', '192.168.11.217', 8001, 8081);
INSERT INTO "main"."t_witness" VALUES (10, '02a7c81cd3fe3ff0c05b7a3f87642d9aefa1386d8e58b09f613b25587be098b3d4', '192.168.11.217', 8002, 8082);
INSERT INTO "main"."t_witness" VALUES (11, '0330b29542bf6a0ea4bb9b99118d5ee50998031ec6ac2ba3fc0fc03f6b24c621b4', '192.168.11.217', 8003, 8083);

-- ----------------------------
-- Indexes structure for table t_block
-- ----------------------------
CREATE INDEX "main"."idx_block_height"
ON "t_block" ("height" ASC);
CREATE UNIQUE INDEX "main"."uniq_block_hash"
ON "t_block" ("block_hash" ASC);

-- ----------------------------
-- Indexes structure for table t_block_index
-- ----------------------------
CREATE INDEX "main"."idx_block_index_height"
ON "t_block_index" ("height" ASC);
CREATE INDEX "main"."idx_block_index_miner_address"
ON "t_block_index" ("miner_address" ASC);
CREATE UNIQUE INDEX "main"."uniq_block_index_block_hash"
ON "t_block_index" ("block_hash" ASC);

-- ----------------------------
-- Indexes structure for table t_peer
-- ----------------------------
CREATE UNIQUE INDEX "main"."uniq_peer_id"
ON "t_peer" ("id" ASC);
CREATE UNIQUE INDEX "main"."uniq_peer_pub_key"
ON "t_peer" ("pub_key" ASC);

-- ----------------------------
-- Indexes structure for table t_spent_transaction_out_index
-- ----------------------------
CREATE INDEX "main"."idx_spt_tran_out_idx_now_tran_hash"
ON "t_spent_transaction_out_index" ("now_transaction_hash" ASC);
CREATE INDEX "main"."idx_spt_tran_out_idx_pre_tran_hash"
ON "t_spent_transaction_out_index" ("pre_transaction_hash" ASC);

-- ----------------------------
-- Indexes structure for table t_transaction_index
-- ----------------------------
CREATE UNIQUE INDEX "main"."uniq_transaction_index_hash"
ON "t_transaction_index" ("transaction_hash" ASC);

-- ----------------------------
-- Indexes structure for table t_utxo
-- ----------------------------
CREATE UNIQUE INDEX "main"."uniq_utxo_transaction_hash_out_index"
ON "t_utxo" ("transaction_hash" ASC, "out_index" ASC);

-- ----------------------------
-- Indexes structure for table t_witness
-- ----------------------------
CREATE UNIQUE INDEX "main"."index_pub_key"
ON "t_witness" ("pub_key" ASC);
