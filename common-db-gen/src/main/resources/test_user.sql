CREATE TABLE `test_user` (
  `sid` int(11) NOT NULL AUTO_INCREMENT,
  `nick` varchar(50) DEFAULT NULL,
  `company` varchar(100) DEFAULT NULL,
  `state` tinyint default 0,
  PRIMARY KEY (`sid`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8