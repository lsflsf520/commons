CREATE TABLE `test_user` (
  `sid` int(11) NOT NULL AUTO_INCREMENT,
  `nick` varchar(50) DEFAULT NULL,
  `age` tinyint(4) DEFAULT NULL,
  `pr_id` int(11) DEFAULT NULL COMMENT '省份ID',
  `city_id` int(11) DEFAULT NULL COMMENT '市ID',
  `cou_id` int(11) DEFAULT NULL COMMENT '县ID',
  `company` varchar(100) DEFAULT NULL,
  `descp` text,
  `status` enum('DELETED','NORMAL') DEFAULT NULL COMMENT 'NORMAL,DELETED',
  `create_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`sid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8