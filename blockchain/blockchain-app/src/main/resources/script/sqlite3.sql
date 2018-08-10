/*
Navicat SQLite Data Transfer

Source Server         : data
Source Server Version : 30808
Source Host           : :0

Target Server Type    : SQLite
Target Server Version : 30808
File Encoding         : 65001

Date: 2018-07-26 16:28:56
*/

PRAGMA foreign_keys = OFF;

-- ----------------------------
-- Table structure for t_block
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_block";
CREATE TABLE "t_block" (
  "id"         INTEGER,
  "block_hash" VARCHAR(64) NOT NULL,
  "data"       TEXT        NOT NULL,
  "height"     INTEGER     NOT NULL,
  PRIMARY KEY ("id" ASC)
);

-- ----------------------------
-- Table structure for t_block_index
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_block_index";
CREATE TABLE "t_block_index" (
  "id"            INTEGER,
  "block_hash"    VARCHAR(64) NOT NULL,
  "height"        INTEGER     NOT NULL,
  "is_best"       INTEGER     NOT NULL,
  "miner_address" VARCHAR(34) NOT NULL,
  PRIMARY KEY ("id" ASC)
);

-- ----------------------------
-- Table structure for t_dpos
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_dpos";
CREATE TABLE "t_dpos" (
  "id"        INTEGER,
  "addresses" VARCHAR(100) NOT NULL,
  "sn"        INTEGER      NOT NULL,
  PRIMARY KEY ("id" ASC)
);

-- ----------------------------
-- Table structure for t_score
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_score";
CREATE TABLE "t_score" (
  "id"      INTEGER,
  "address" VARCHAR(34) NOT NULL,
  "score"   INTEGER     NOT NULL,
  PRIMARY KEY ("id" ASC)
);

-- ----------------------------
-- Table structure for t_transaction_index
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_transaction_index";
CREATE TABLE "t_transaction_index" (
  "id"                INTEGER,
  "block_hash"        VARCHAR(64) NOT NULL,
  "transaction_hash"  VARCHAR(64) NOT NULL,
  "transaction_index" INTEGER     NOT NULL,
  PRIMARY KEY ("id" ASC)
);

-- ----------------------------
-- Table structure for t_utxo
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_utxo";
CREATE TABLE "t_utxo" (
  "id"               INTEGER,
  "amount"           VARCHAR(16) NOT NULL,
  "currency"         VARCHAR(8)  NOT NULL,
  "lock_script"      VARCHAR     NOT NULL,
  "out_index"        INTEGER     NOT NULL,
  "script_type"      INTEGER     NOT NULL,
  "transaction_hash" VARCHAR(64) NOT NULL,
  PRIMARY KEY ("id" ASC)
);

-- ----------------------------
-- Table structure for t_witness
-- ----------------------------
DROP TABLE IF EXISTS "main"."t_witness";
CREATE TABLE "t_witness" (
  "id"          INTEGER,
  "address"     VARCHAR(50),
  "http_port"   INTEGER,
  "pub_key"     VARCHAR(32),
  "socket_port" INTEGER,
  PRIMARY KEY ("id" ASC)
);

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
-- Indexes structure for table t_dpos
-- ----------------------------
CREATE UNIQUE INDEX "main"."idx_dpos_sn"
  ON "t_dpos" ("sn" ASC);

-- ----------------------------
-- Indexes structure for table t_score
-- ----------------------------
CREATE INDEX "main"."idx_score_address"
  ON "t_score" ("address" ASC);

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
