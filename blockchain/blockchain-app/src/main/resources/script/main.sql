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
INSERT INTO "main"."t_witness" VALUES (1, '0336b1001417e928e8aa58131faa1ee9aa1f209a565010d5be2aeebe5844b60d67', '192.168.1.155', 8001, 8081);
INSERT INTO "main"."t_witness" VALUES (2, '038eaca84d5df0bbb0eca6d45d4d315e7652c76ab1d3cee43112c6b8627fc44f27', '192.168.1.155', 8002, 8082);
INSERT INTO "main"."t_witness" VALUES (3, '0367a2279fc0910c3feca555461ddda7f9173f74da99e454fcc2f36d0bb4feff6a', '192.168.1.163', 8001, 8081);
INSERT INTO "main"."t_witness" VALUES (4, '0373ab4697382dab395272a270649c1c22bc69146304bdd3036c954499f6a492cc', '192.168.1.163', 8002, 8082);
INSERT INTO "main"."t_witness" VALUES (5, '02c9e19c204e02e7488e1b87516e2aa94e111dd0d6fd9dab680532c3687dd4eb27', '192.168.1.132', 8001, 8081);
INSERT INTO "main"."t_witness" VALUES (6, '03635727cd0f5e6851df2b2bf24fe3eb57c5548cf93c988d23e121d98e8c5d39e7', '192.168.1.132', 8002, 8082);
INSERT INTO "main"."t_witness" VALUES (7, '0216265887073a8ceaa993e07a8d45a37f8a3fc433fdd03b5a211e1fbcbd7beb05', '192.168.1.161', 8001, 8081);
INSERT INTO "main"."t_witness" VALUES (8, '039f56d7f6dd4e5ffe4b8b4fb252f4ea1928d7940dab8b083f33797fec88c7c918', '192.168.1.168', 8001, 8081);
INSERT INTO "main"."t_witness" VALUES (9, '0203e85eab2f5ccd3e6a458bea0ed772809dfa331778f9d57aa6aa35f1fb16922d', '192.168.1.125', 8001, 8081);
INSERT INTO "main"."t_witness" VALUES (10, '03b5a3849e48d04119f7d2d14bb0376d96429e0b5bb2f7c3e21717be7a9c8c5153', '192.168.1.197', 8001, 8081);
INSERT INTO "main"."t_witness" VALUES (11, '03e0c1ff81435a4eac94873ea2236b7c36756d99cd67228a9359e43f6cc60d46bf', '192.168.1.111', 8001, 8081);

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
CREATE INDEX "main"."idx_lock_script_currency"
ON "t_utxo" ("lock_script" ASC,"currency" ASC);

-- ----------------------------
-- Indexes structure for table t_witness
-- ----------------------------
CREATE UNIQUE INDEX "main"."index_pub_key"
ON "t_witness" ("pub_key" ASC);
