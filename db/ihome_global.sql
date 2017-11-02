/*
SQLyog Professional v12.09 (64 bit)
MySQL - 5.1.73 : Database - ihome_global
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`ihome_global` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `ihome_global`;

/*Table structure for table `t_homeid` */

CREATE TABLE `t_homeid` (
  `homeid` varchar(15) NOT NULL,
  `password` varchar(35) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*Data for the table `t_homeid` */

insert  into `t_homeid`(`homeid`,`password`) values ('ihome_001','202cb962ac59075b964b07152d234b70'),('ihome_002','202cb962ac59075b964b07152d234b70');

/*Table structure for table `t_timing` */

CREATE TABLE `t_timing` (
  `homeid` varchar(15) NOT NULL,
  `frequency` int(11) NOT NULL,
  `hour` int(11) NOT NULL,
  `minute` int(11) NOT NULL,
  `msg` varchar(20) NOT NULL,
  `operation` varchar(35) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*Data for the table `t_timing` */

insert  into `t_timing`(`homeid`,`frequency`,`hour`,`minute`,`msg`,`operation`) values ('ihome_002',1,10,24,'10;01;00','客厅灯关'),('ihome_001',1,11,11,'10;01;00','客厅灯关');

/*Table structure for table `t_user` */

CREATE TABLE `t_user` (
  `email` varchar(25) NOT NULL,
  `homeid` varchar(15) NOT NULL,
  `password` varchar(35) NOT NULL,
  `phonenumber` varchar(12) DEFAULT NULL,
  `registertime` datetime NOT NULL,
  `name` varchar(35) NOT NULL,
  `wchatid` varchar(35) DEFAULT NULL,
  PRIMARY KEY (`email`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

/*Data for the table `t_user` */

insert  into `t_user`(`email`,`homeid`,`password`,`phonenumber`,`registertime`,`name`,`wchatid`) values ('123456','ihome_001','e10adc3949ba59abbe56e057f20f883e','','2016-07-21 10:54:04','吴绍岭','oT6MNw6WtBqTaU54Qi1UfMmgGSms');

/*Table structure for table `t_verify` */

CREATE TABLE `t_verify` (
  `email` varchar(25) NOT NULL,
  `verify` varchar(6) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*Data for the table `t_verify` */

insert  into `t_verify`(`email`,`verify`) values ('tc0201zxw@126.com','942050'),('782368075@qq.com','486369'),('1065506474@qq.com','208985'),('1187116140@qq.com','921605'),('1076351865@qq.com','260273');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
