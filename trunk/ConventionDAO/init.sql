/*
SQLyog Enterprise - MySQL GUI v6.5
MySQL - 5.0.67-community-nt : Database - daotest
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

create database if not exists `daotest`;

USE `daotest`;

/*Table structure for table `dao_test` */

DROP TABLE IF EXISTS `dao_test`;

CREATE TABLE `dao_test` (
  `test_id` varchar(32) NOT NULL,
  `testname` varchar(200) default NULL,
  `testint` int(10) default NULL,
  `testdate` date default NULL,
  `id` varchar(32) default NULL,
  PRIMARY KEY  (`test_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `gy_xj_student` */

DROP TABLE IF EXISTS `gy_xj_student`;

CREATE TABLE `gy_xj_student` (
  `student_id` int(32) NOT NULL auto_increment,
  `xh` varchar(100) default NULL,
  `pycc` varchar(100) default NULL,
  PRIMARY KEY  (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `jw_message` */

DROP TABLE IF EXISTS `jw_message`;

CREATE TABLE `jw_message` (
  `id` varchar(32) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `testboolean` */

DROP TABLE IF EXISTS `testboolean`;

CREATE TABLE `testboolean` (
  `id` int(2) NOT NULL auto_increment,
  `testboolean` tinyint(1) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `vw_gy_user` */

DROP TABLE IF EXISTS `vw_gy_user`;

/*!50001 DROP VIEW IF EXISTS `vw_gy_user` */;
/*!50001 DROP TABLE IF EXISTS `vw_gy_user` */;

/*!50001 CREATE TABLE `vw_gy_user` (
  `student_id` int(32) NOT NULL default '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 */;

/*View structure for view vw_gy_user */

/*!50001 DROP TABLE IF EXISTS `vw_gy_user` */;
/*!50001 DROP VIEW IF EXISTS `vw_gy_user` */;

/*!50001 CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_gy_user` AS (select `gy_xj_student`.`student_id` AS `student_id` from `gy_xj_student`) */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;

alter table `daotest`.`dao_test` add column `bigtext` text NULL after `id`;

CREATE TABLE `auto_generate_id` (                          
`id` int(10) NOT NULL,                                   
`name` varchar(20) DEFAULT NULL,                         
PRIMARY KEY (`id`)                                     
) ENGINE=InnoDB DEFAULT CHARSET=utf8  ;

CREATE TABLE `auto_generate_id2` (                          
`id` int(10) NOT NULL,                                   
`name` varchar(20) DEFAULT NULL,                         
PRIMARY KEY (`id`)                                     
) ENGINE=InnoDB DEFAULT CHARSET=utf8  ;