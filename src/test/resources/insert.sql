DROP TABLE IF EXISTS `homes`;
CREATE TABLE `homes` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `street` varchar(32) DEFAULT NULL,
  `city` varchar(32) NOT NULL,
  `houseNumber` int(11) DEFAULT NULL,
  `zipCode` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
);

INSERT INTO `homes` (`ID`, `street`, `city`, `houseNumber`, `zipCode`) VALUES
(1,	'Ice-cream',	'Prague',	1254,	10000),
(2,	'Hot Chocolate',	'Prague',	1111,	10000),
(3,	'Coconut Milk',	'Wien',	3,	45810);

DROP TABLE IF EXISTS `persons`;
CREATE TABLE `persons` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `firstName` varchar(32) NOT NULL,
  `surName` varchar(32) NOT NULL,
  `description` text,
  `homeID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  FOREIGN KEY (`homeID`) REFERENCES `homes` (`ID`)
);

INSERT INTO `persons` (`ID`, `firstName`, `surName`, `description`, `homeID`) VALUES
(1,	'Robinson',	'Crusoe',	'Lonely person',	NULL),
(2,	'Weird',	'Man',	'Quinte es it do bela, ik du je tyma neno.\r\n\r\nDoes anybody know, what that mean?',	1),
(3,	'Lucky',	'Peter',	'He won 10^6 CZK in lottery.',	2),
(4,	'Nicholas',	'Winton',	'The man I admire',	NULL),
(5,	'Jan',	'Nietsche',	NULL,	3),
(6,	'Crazy',	'Women',	'She loves Weird Man',	1);
