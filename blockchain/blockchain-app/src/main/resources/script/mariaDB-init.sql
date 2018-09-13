/*
SQLyog Professional v12.08 (64 bit)
MySQL - 10.1.20-MariaDB : Database - higgs
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE = '' */;

/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS */`higgs` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `higgs`;

/*Data for the table `t_block` */

INSERT INTO `t_block` (`id`, `block_hash`, `data`, `height`) VALUES
  (2, '359cdc900f58b8600c676a2949312468c5ee9740dd23334daf2db41b3a133555',
   '{\"blockTime\":1535462074176,\"height\":1,\"minerSigPair\":{\"pubKey\":\"02a7c81cd3fe3ff0c05b7a3f87642d9aefa1386d8e58b09f613b25587be098b3d4\",\"signature\":\"H3YtBlQXPTKx1WM7zY6Sc7vdDrSeE2dmNc/N0ldFYs11WJFL0DHMNQ8Wy1gq8ZrwanI3rxmoLkwQa2UNFAWVN5I=\"},\"prevBlockHash\":null,\"transactions\":[{\"extra\":null,\"inputs\":null,\"lockTime\":0,\"outputs\":[{\"lockScript\":{\"address\":\"15XVxQEG4VPUEpa3me81weN3qZ618QcgHv\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"17jZ8oW1kozBY1sn7RfS7FytYDkCC3yReW\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"17ZNgdya9Xf3sUEQUCbFJuEBUETYKfnZg6\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"18yNvrpLddtrSHUYpXUTXos4wfp1woPqjJ\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1DarjxrXCHjvaTrGVS4TAbgbhTZZfXe67z\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1GKM1a8Hs2qUwWVY14DSGBtapdqjCsfFyh\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1MA15kCw733Hptr2nL39HHR1ewyfKKQ2F6\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1DSbnXkn7Y7thnqovrt8Rnin1epEv3XGUA\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1PRCzE9s7ZWVN5vC44oNjPP9J4PNUooX4f\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1aXD67WvFE1j3mWF48udwyGvGnPYcmt3A\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1EYFLntYVRvGR2pieqd18DgqEo2F9kNyG6\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"18sCa3BMT2hVXjXHtdrKVnDTqkzwAyfyn6\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1Q3SaoT3PDbdQWaTQCLJ9jM6ZgdnD9BEh1\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1EkuEN2YP494dKyzXteuGXLpAT844CNaye\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1EkuEN2YP494dKyzXteuGXLpAT844CNaye\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1Bs3vwN18GJLp8N94W8n31upm2tchVbN5Y\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1GdHvPcPaEnUAeSsQs8b9Dk7YF1o9DjzJh\",\"type\":0},\"money\":{\"currency\":\"miner\",\"value\":\"2000.00000000\"}},{\"lockScript\":{\"address\":\"1NeaNrw1E1kzxSKEq86U6rqzsKmzTbgeMg\",\"type\":0},\"money\":{\"currency\":\"guarder\",\"value\":\"1000.00000000\"}},{\"lockScript\":{\"address\":\"1N7isETotYv2YB6KMCwxfTZFz1JWLbWo8P\",\"type\":0},\"money\":{\"currency\":\"guarder\",\"value\":\"1000.00000000\"}},{\"lockScript\":{\"address\":\"15XVxQEG4VPUEpa3me81weN3qZ618QcgHv\",\"type\":0},\"money\":{\"currency\":\"cas\",\"value\":\"9000000.00000000\"}}],\"transactionTime\":1535462074145,\"version\":1}],\"version\":1,\"voteVersion\":0,\"witnessSigPairs\":[]}',
   1);

/*Data for the table `t_block_index` */

INSERT INTO `t_block_index` (`id`, `block_hash`, `height`, `is_best`, `miner_address`) VALUES
  (2, '359cdc900f58b8600c676a2949312468c5ee9740dd23334daf2db41b3a133555', 1, 0, '1HJQyN7q4qsXczkzwQkeon3S6YMixk1v82');

/*Data for the table `t_dpos` */

INSERT INTO `t_dpos` (`id`, `addresses`, `sn`) VALUES (2,
                                                       '[\"18sCa3BMT2hVXjXHtdrKVnDTqkzwAyfyn6\",\"18yNvrpLddtrSHUYpXUTXos4wfp1woPqjJ\",\"17ZNgdya9Xf3sUEQUCbFJuEBUETYKfnZg6\",\"15XVxQEG4VPUEpa3me81weN3qZ618QcgHv\",\"1Bs3vwN18GJLp8N94W8n31upm2tchVbN5Y\",\"17jZ8oW1kozBY1sn7RfS7FytYDkCC3yReW\",\"1EYFLntYVRvGR2pieqd18DgqEo2F9kNyG6\"]',
                                                       2);

/*Data for the table `t_score` */

INSERT INTO `t_score` (`id`, `address`, `score`)
VALUES (17, '1GdHvPcPaEnUAeSsQs8b9Dk7YF1o9DjzJh', 0), (18, '1PRCzE9s7ZWVN5vC44oNjPP9J4PNUooX4f', 0),
  (19, '18sCa3BMT2hVXjXHtdrKVnDTqkzwAyfyn6', 0), (20, '18yNvrpLddtrSHUYpXUTXos4wfp1woPqjJ', 0),
  (21, '1GKM1a8Hs2qUwWVY14DSGBtapdqjCsfFyh', 0), (22, '1aXD67WvFE1j3mWF48udwyGvGnPYcmt3A', 0),
  (23, '17jZ8oW1kozBY1sn7RfS7FytYDkCC3yReW', 0), (24, '1MA15kCw733Hptr2nL39HHR1ewyfKKQ2F6', 0),
  (25, '1DarjxrXCHjvaTrGVS4TAbgbhTZZfXe67z', 0), (26, '1EYFLntYVRvGR2pieqd18DgqEo2F9kNyG6', 0),
  (27, '15XVxQEG4VPUEpa3me81weN3qZ618QcgHv', 0), (28, '1EkuEN2YP494dKyzXteuGXLpAT844CNaye', 0),
  (29, '1Bs3vwN18GJLp8N94W8n31upm2tchVbN5Y', 0), (30, '1Q3SaoT3PDbdQWaTQCLJ9jM6ZgdnD9BEh1', 0),
  (31, '17ZNgdya9Xf3sUEQUCbFJuEBUETYKfnZg6', 0), (32, '1DSbnXkn7Y7thnqovrt8Rnin1epEv3XGUA', 0);

/*Data for the table `t_transaction_index` */

INSERT INTO `t_transaction_index` (`id`, `block_hash`, `transaction_hash`, `transaction_index`) VALUES
  (2, '359cdc900f58b8600c676a2949312468c5ee9740dd23334daf2db41b3a133555',
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995', 0);

/*Data for the table `t_utxo` */

INSERT INTO `t_utxo` (`id`, `amount`, `currency`, `lock_script`, `out_index`, `script_type`, `transaction_hash`) VALUES
  (22, '2000.00000000', 'miner', '15XVxQEG4VPUEpa3me81weN3qZ618QcgHv', 0, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (23, '2000.00000000', 'miner', '17jZ8oW1kozBY1sn7RfS7FytYDkCC3yReW', 1, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (24, '2000.00000000', 'miner', '17ZNgdya9Xf3sUEQUCbFJuEBUETYKfnZg6', 2, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (25, '2000.00000000', 'miner', '18yNvrpLddtrSHUYpXUTXos4wfp1woPqjJ', 3, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (26, '2000.00000000', 'miner', '1DarjxrXCHjvaTrGVS4TAbgbhTZZfXe67z', 4, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (27, '2000.00000000', 'miner', '1GKM1a8Hs2qUwWVY14DSGBtapdqjCsfFyh', 5, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (28, '2000.00000000', 'miner', '1MA15kCw733Hptr2nL39HHR1ewyfKKQ2F6', 6, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (29, '2000.00000000', 'miner', '1DSbnXkn7Y7thnqovrt8Rnin1epEv3XGUA', 7, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (30, '2000.00000000', 'miner', '1PRCzE9s7ZWVN5vC44oNjPP9J4PNUooX4f', 8, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (31, '2000.00000000', 'miner', '1aXD67WvFE1j3mWF48udwyGvGnPYcmt3A', 9, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (32, '2000.00000000', 'miner', '1EYFLntYVRvGR2pieqd18DgqEo2F9kNyG6', 10, 0, '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (33, '2000.00000000', 'miner', '18sCa3BMT2hVXjXHtdrKVnDTqkzwAyfyn6', 11, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (34, '2000.00000000', 'miner', '1Q3SaoT3PDbdQWaTQCLJ9jM6ZgdnD9BEh1', 12, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (35, '2000.00000000', 'miner', '1EkuEN2YP494dKyzXteuGXLpAT844CNaye', 13, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (36, '2000.00000000', 'miner', '1EkuEN2YP494dKyzXteuGXLpAT844CNaye', 14, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (37, '2000.00000000', 'miner', '1Bs3vwN18GJLp8N94W8n31upm2tchVbN5Y', 15, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (38, '2000.00000000', 'miner', '1GdHvPcPaEnUAeSsQs8b9Dk7YF1o9DjzJh', 16, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (39, '1000.00000000', 'guarder', '1NeaNrw1E1kzxSKEq86U6rqzsKmzTbgeMg', 17, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (40, '1000.00000000', 'guarder', '1N7isETotYv2YB6KMCwxfTZFz1JWLbWo8P', 18, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995'),
  (41, '9000000.00000000', 'cas', '15XVxQEG4VPUEpa3me81weN3qZ618QcgHv', 19, 0,
   '300f91ef9517175197744a249a168e37d4592ea0425274d1d8edf50fe26a8995');

/*Data for the table `t_witness` */

INSERT INTO `t_witness` (`id`, `address`, `http_port`, `pub_key`, `socket_port`)
VALUES (12, '10.200.173.85', 8081, '0336b1001417e928e8aa58131faa1ee9aa1f209a565010d5be2aeebe5844b60d67', 8001),
  (13, '10.200.173.85', 8082, '038eaca84d5df0bbb0eca6d45d4d315e7652c76ab1d3cee43112c6b8627fc44f27', 8002),
  (14, '10.200.173.83', 8081, '03e0c1ff81435a4eac94873ea2236b7c36756d99cd67228a9359e43f6cc60d46bf', 8001),
  (15, '10.200.173.83', 8082, '024d2913d1390e5fcb74567291fe1cb3f7e53bac1fda5703e16df0b9df1fbc5e38', 8002),
  (16, '10.200.173.82', 8081, '0203e85eab2f5ccd3e6a458bea0ed772809dfa331778f9d57aa6aa35f1fb16922d', 8001),
  (17, '10.200.173.82', 8082, '0367a2279fc0910c3feca555461ddda7f9173f74da99e454fcc2f36d0bb4feff6a', 8002),
  (18, '10.200.173.81', 8081, '039f56d7f6dd4e5ffe4b8b4fb252f4ea1928d7940dab8b083f33797fec88c7c918', 8001),
  (19, '10.200.173.81', 8082, '03b5a3849e48d04119f7d2d14bb0376d96429e0b5bb2f7c3e21717be7a9c8c5153', 8002),
  (20, '10.200.173.80', 8081, '0216265887073a8ceaa993e07a8d45a37f8a3fc433fdd03b5a211e1fbcbd7beb05', 8001),
  (21, '10.200.173.84', 8081, '02c9e19c204e02e7488e1b87516e2aa94e111dd0d6fd9dab680532c3687dd4eb27', 8001),
  (22, '10.200.173.84', 8082, '03635727cd0f5e6851df2b2bf24fe3eb57c5548cf93c988d23e121d98e8c5d39e7', 8002);

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;
