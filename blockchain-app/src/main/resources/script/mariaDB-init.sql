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


INSERT INTO `t_block` VALUES ('1', '2a31ccff4640b2eda18dd76a50c38928f77343fd997bb40db72a77e2770b12df',
                              '{\"blockTime\":1534419961275,\"height\":1,\"minerSigPair\":{\"pubKey\":\"02a7c81cd3fe3ff0c05b7a3f87642d9aefa1386d8e58b09f613b25587be098b3d4\",\"signature\":\"Hy8b0KD6q/bIzJjniUUE2gknetdsIx25gU+MXcfh4FikYaw+w43Kg5mRxVnJyO55EZSwJflxSRf20rQPn/0rMqA=\"},\"prevBlockHash\":null,\"transactions\":[{\"extra\":null,\"inputs\":null,\"lockTime\":0,\"outputs\":[{\"lockScript\":{\"address\":\"15XVxQEG4VPUEpa3me81weN3qZ618QcgHv\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"17jZ8oW1kozBY1sn7RfS7FytYDkCC3yReW\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"17ZNgdya9Xf3sUEQUCbFJuEBUETYKfnZg6\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"18yNvrpLddtrSHUYpXUTXos4wfp1woPqjJ\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1DarjxrXCHjvaTrGVS4TAbgbhTZZfXe67z\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1GKM1a8Hs2qUwWVY14DSGBtapdqjCsfFyh\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1MA15kCw733Hptr2nL39HHR1ewyfKKQ2F6\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1DSbnXkn7Y7thnqovrt8Rnin1epEv3XGUA\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1PRCzE9s7ZWVN5vC44oNjPP9J4PNUooX4f\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1aXD67WvFE1j3mWF48udwyGvGnPYcmt3A\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1EYFLntYVRvGR2pieqd18DgqEo2F9kNyG6\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1Bh4HNq6RabiQEAfzYqmKAaDVtCiaeeYHx\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1Q3SaoT3PDbdQWaTQCLJ9jM6ZgdnD9BEh1\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1EkuEN2YP494dKyzXteuGXLpAT844CNaye\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1EkuEN2YP494dKyzXteuGXLpAT844CNaye\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1Bs3vwN18GJLp8N94W8n31upm2tchVbN5Y\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1GdHvPcPaEnUAeSsQs8b9Dk7YF1o9DjzJh\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1NeaNrw1E1kzxSKEq86U6rqzsKmzTbgeMg\",\"type\":0},\"money\":{\"currency\":\"guarder\",\"value\":\"1000.00000000\"}},{\"lockScript\":{\"address\":\"1N7isETotYv2YB6KMCwxfTZFz1JWLbWo8P\",\"type\":0},\"money\":{\"currency\":\"guarder\",\"value\":\"1000.00000000\"}},{\"lockScript\":{\"address\":\"15XVxQEG4VPUEpa3me81weN3qZ618QcgHv\",\"type\":0},\"money\":{\"currency\":\"cas\",\"value\":\"9000000.00000000\"}}],\"transactionTime\":1534419961258,\"version\":1}],\"version\":1,\"voteVersion\":0,\"witnessSigPairs\":[]}',
                              '1');

-- ----------------------------
-- Records of t_block_index
-- ----------------------------
INSERT INTO `t_block_index` VALUES ('1', '2a31ccff4640b2eda18dd76a50c38928f77343fd997bb40db72a77e2770b12df', '1', '0',
                                    '1HJQyN7q4qsXczkzwQkeon3S6YMixk1v82');

-- ----------------------------
-- Records of t_dpos
-- ----------------------------
INSERT INTO `t_dpos` VALUES ('1',
                             '[\"1DarjxrXCHjvaTrGVS4TAbgbhTZZfXe67z\",\"1Bh4HNq6RabiQEAfzYqmKAaDVtCiaeeYHx\",\"1DSbnXkn7Y7thnqovrt8Rnin1epEv3XGUA\",\"1PRCzE9s7ZWVN5vC44oNjPP9J4PNUooX4f\",\"17jZ8oW1kozBY1sn7RfS7FytYDkCC3yReW\",\"1Bs3vwN18GJLp8N94W8n31upm2tchVbN5Y\",\"1GKM1a8Hs2qUwWVY14DSGBtapdqjCsfFyh\"]',
                             '2');

-- ----------------------------
-- Records of t_score
-- ----------------------------
INSERT INTO `t_score` VALUES ('1', '1GdHvPcPaEnUAeSsQs8b9Dk7YF1o9DjzJh', '0');
INSERT INTO `t_score` VALUES ('2', '1PRCzE9s7ZWVN5vC44oNjPP9J4PNUooX4f', '0');
INSERT INTO `t_score` VALUES ('3', '18yNvrpLddtrSHUYpXUTXos4wfp1woPqjJ', '0');
INSERT INTO `t_score` VALUES ('4', '1GKM1a8Hs2qUwWVY14DSGBtapdqjCsfFyh', '0');
INSERT INTO `t_score` VALUES ('5', '1Bh4HNq6RabiQEAfzYqmKAaDVtCiaeeYHx', '0');
INSERT INTO `t_score` VALUES ('6', '1aXD67WvFE1j3mWF48udwyGvGnPYcmt3A', '0');
INSERT INTO `t_score` VALUES ('7', '17jZ8oW1kozBY1sn7RfS7FytYDkCC3yReW', '0');
INSERT INTO `t_score` VALUES ('8', '1MA15kCw733Hptr2nL39HHR1ewyfKKQ2F6', '0');
INSERT INTO `t_score` VALUES ('9', '1DarjxrXCHjvaTrGVS4TAbgbhTZZfXe67z', '0');
INSERT INTO `t_score` VALUES ('10', '1EYFLntYVRvGR2pieqd18DgqEo2F9kNyG6', '0');
INSERT INTO `t_score` VALUES ('11', '15XVxQEG4VPUEpa3me81weN3qZ618QcgHv', '0');
INSERT INTO `t_score` VALUES ('12', '1EkuEN2YP494dKyzXteuGXLpAT844CNaye', '0');
INSERT INTO `t_score` VALUES ('13', '1Bs3vwN18GJLp8N94W8n31upm2tchVbN5Y', '0');
INSERT INTO `t_score` VALUES ('14', '1Q3SaoT3PDbdQWaTQCLJ9jM6ZgdnD9BEh1', '0');
INSERT INTO `t_score` VALUES ('15', '17ZNgdya9Xf3sUEQUCbFJuEBUETYKfnZg6', '0');
INSERT INTO `t_score` VALUES ('16', '1DSbnXkn7Y7thnqovrt8Rnin1epEv3XGUA', '0');

-- ----------------------------
-- Records of t_transaction_index
-- ----------------------------
INSERT INTO `t_transaction_index` VALUES ('1', '2a31ccff4640b2eda18dd76a50c38928f77343fd997bb40db72a77e2770b12df',
                                          'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d', '0');

-- ----------------------------
-- Records of t_utxo
-- ----------------------------
INSERT INTO `t_utxo` VALUES ('1', '2000.00000000', 'miner', '15XVxQEG4VPUEpa3me81weN3qZ618QcgHv', '0', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('2', '2000.00000000', 'miner', '17jZ8oW1kozBY1sn7RfS7FytYDkCC3yReW', '1', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('3', '2000.00000000', 'miner', '17ZNgdya9Xf3sUEQUCbFJuEBUETYKfnZg6', '2', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('4', '2000.00000000', 'miner', '18yNvrpLddtrSHUYpXUTXos4wfp1woPqjJ', '3', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('5', '2000.00000000', 'miner', '1DarjxrXCHjvaTrGVS4TAbgbhTZZfXe67z', '4', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('6', '2000.00000000', 'miner', '1GKM1a8Hs2qUwWVY14DSGBtapdqjCsfFyh', '5', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('7', '2000.00000000', 'miner', '1MA15kCw733Hptr2nL39HHR1ewyfKKQ2F6', '6', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('8', '2000.00000000', 'miner', '1DSbnXkn7Y7thnqovrt8Rnin1epEv3XGUA', '7', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('9', '2000.00000000', 'miner', '1PRCzE9s7ZWVN5vC44oNjPP9J4PNUooX4f', '8', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('10', '2000.00000000', 'miner', '1aXD67WvFE1j3mWF48udwyGvGnPYcmt3A', '9', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('11', '2000.00000000', 'miner', '1EYFLntYVRvGR2pieqd18DgqEo2F9kNyG6', '10', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('12', '2000.00000000', 'miner', '1Bh4HNq6RabiQEAfzYqmKAaDVtCiaeeYHx', '11', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('13', '2000.00000000', 'miner', '1Q3SaoT3PDbdQWaTQCLJ9jM6ZgdnD9BEh1', '12', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('14', '2000.00000000', 'miner', '1EkuEN2YP494dKyzXteuGXLpAT844CNaye', '13', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('15', '2000.00000000', 'miner', '1EkuEN2YP494dKyzXteuGXLpAT844CNaye', '14', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('16', '2000.00000000', 'miner', '1Bs3vwN18GJLp8N94W8n31upm2tchVbN5Y', '15', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('17', '2000.00000000', 'miner', '1GdHvPcPaEnUAeSsQs8b9Dk7YF1o9DjzJh', '16', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('18', '1000.00000000', 'guarder', '1NeaNrw1E1kzxSKEq86U6rqzsKmzTbgeMg', '17', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('19', '1000.00000000', 'guarder', '1N7isETotYv2YB6KMCwxfTZFz1JWLbWo8P', '18', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');
INSERT INTO `t_utxo` VALUES ('20', '9000000.00000000', 'cas', '15XVxQEG4VPUEpa3me81weN3qZ618QcgHv', '19', '0',
                             'eeb2b916b76bd6ca9bdd948e9bb1fc16dcb6f53b4318e984300374344389d59d');

-- ----------------------------
-- Records of t_witness
-- ----------------------------
INSERT INTO `t_witness`
VALUES ('1', '10.200.173.85', '8081', '0336b1001417e928e8aa58131faa1ee9aa1f209a565010d5be2aeebe5844b60d67', '8001');
INSERT INTO `t_witness`
VALUES ('2', '10.200.173.85', '8082', '038eaca84d5df0bbb0eca6d45d4d315e7652c76ab1d3cee43112c6b8627fc44f27', '8002');
INSERT INTO `t_witness`
VALUES ('3', '10.200.173.83', '8081', '03e0c1ff81435a4eac94873ea2236b7c36756d99cd67228a9359e43f6cc60d46bf', '8001');
INSERT INTO `t_witness`
VALUES ('4', '10.200.173.83', '8082', '024d2913d1390e5fcb74567291fe1cb3f7e53bac1fda5703e16df0b9df1fbc5e38', '8002');
INSERT INTO `t_witness`
VALUES ('5', '10.200.173.82', '8081', '0203e85eab2f5ccd3e6a458bea0ed772809dfa331778f9d57aa6aa35f1fb16922d', '8001');
INSERT INTO `t_witness`
VALUES ('6', '10.200.173.82', '8082', '0367a2279fc0910c3feca555461ddda7f9173f74da99e454fcc2f36d0bb4feff6a', '8002');
INSERT INTO `t_witness`
VALUES ('7', '10.200.173.81', '8081', '039f56d7f6dd4e5ffe4b8b4fb252f4ea1928d7940dab8b083f33797fec88c7c918', '8001');
INSERT INTO `t_witness`
VALUES ('8', '10.200.173.81', '8082', '03b5a3849e48d04119f7d2d14bb0376d96429e0b5bb2f7c3e21717be7a9c8c5153', '8002');
INSERT INTO `t_witness`
VALUES ('9', '10.200.173.80', '8081', '0216265887073a8ceaa993e07a8d45a37f8a3fc433fdd03b5a211e1fbcbd7beb05', '8001');
INSERT INTO `t_witness`
VALUES ('10', '10.200.173.84', '8081', '02c9e19c204e02e7488e1b87516e2aa94e111dd0d6fd9dab680532c3687dd4eb27', '8001');
INSERT INTO `t_witness`
VALUES ('11', '10.200.173.84', '8082', '03635727cd0f5e6851df2b2bf24fe3eb57c5548cf93c988d23e121d98e8c5d39e7', '8002');
