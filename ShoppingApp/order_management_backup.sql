-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: order_management
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `appeal`
--

DROP TABLE IF EXISTS `appeal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appeal` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `shop_name` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `content` text,
  `contact` varchar(100) DEFAULT NULL,
  `reply` text,
  `reply_status` varchar(20) DEFAULT 'PENDING',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `reply_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appeal`
--

LOCK TABLES `appeal` WRITE;
/*!40000 ALTER TABLE `appeal` DISABLE KEYS */;
INSERT INTO `appeal` VALUES (1,'琪琪','琪琪大酒店','BANNED','账号解封','我是冤枉的','13856548962','╮(╯-╰)╭','PROCESSED','2026-06-12 02:34:30','2026-06-12 02:45:23'),(2,'rujia','如家酒店','NORMAL','酒店解封','【酒店申诉】酒店ID:7，酒店名称:如家酒店\n禁用原因:违规经营\n\n申诉内容:没有违规操作，建议重新审核','13809524762','审核后，确认通过','PROCESSED','2026-06-13 06:42:36','2026-06-13 06:43:23'),(3,'rujia','如家酒店','NORMAL','酒店解封','【酒店申诉】酒店ID:7，酒店名称:如家酒店\n禁用原因:违规经营\n\n申诉内容:并无违规操作','13856479214','收到','PROCESSED','2026-06-13 06:50:55','2026-06-13 06:51:19'),(4,'如家','如家大酒店','BANNED','信息修改','我是ysn，赶紧给我解了，知道我是谁吗？','15986538213','gun','REJECTED','2026-06-14 06:41:48','2026-06-14 06:42:15'),(5,'琪琪','琪琪大酒店','BANNED','账号解封','解封','138456987559','ok','PROCESSED','2026-06-16 01:06:42','2026-06-16 01:07:07'),(6,'琪琪','琪琪大酒店','BANNED','账号解封','123','15917029371','ok','PROCESSED','2026-06-16 01:53:34','2026-06-18 03:15:14'),(7,'琪琪','琪琪大酒店','BANNED','账号解封','123','1591283712','好的','PROCESSED','2026-06-16 01:58:36','2026-06-16 02:02:04');
/*!40000 ALTER TABLE `appeal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attraction`
--

DROP TABLE IF EXISTS `attraction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attraction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '景点名称',
  `province` varchar(100) NOT NULL COMMENT '所在省份',
  `photo` varchar(500) DEFAULT NULL COMMENT '景点照片URL',
  `description` text COMMENT '景点简介详情',
  `score` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='景点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attraction`
--

LOCK TABLES `attraction` WRITE;
/*!40000 ALTER TABLE `attraction` DISABLE KEYS */;
INSERT INTO `attraction` VALUES (1,'九寨沟','四川省','/file/674fbe64-e004-42eb-905a-2167af6fde7f_6d200e1e-8148-47ec-8c57-8db3f26d4f14_四川九寨沟.jpg',NULL,5),(2,'洪崖洞','重庆市','/file/6e028fff-05fa-4b26-aec5-e4058319efb9_902c37dd-59e4-41da-9bb2-6df6499fd9f1_重庆洪崖洞.png',NULL,NULL),(3,'布达拉宫','西藏自治区','/file/c3f66b74-eba6-42e7-83f6-7b813a3accfa_41a0bea0-2e10-47e7-94a6-219442ffffcf_西藏布达拉宫.png',NULL,NULL),(6,'玉龙雪山','云南省','/file/cc8ba76b-45a3-4c60-bf69-897f59a3cbfc_457fc9e4-aefa-45c4-9473-8ab4bc8fc0c4_云南玉龙雪山.png',NULL,NULL),(7,'上海迪士尼乐园','上海市','/file/14e161d8-913a-4f54-8024-6ddfc9435490_16f408dd-7e04-4676-9643-4555959206d6_上海迪士尼乐园.png',NULL,NULL),(8,'长城','北京市','/file/d7446f90-7c8f-4858-b63f-9138ac7355a0_42fd328c-0c49-4330-92e6-1c974bdce920_北京长城.png',NULL,NULL);
/*!40000 ALTER TABLE `attraction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attraction_id` bigint DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `score` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (1,1,'非常美丽~good，good，真是beautiful啊！','2026-05-21 14:03:11.307000',5),(2,1,'好玩，漂亮','2026-05-21 15:48:02.566000',5),(3,1,'好好玩','2026-05-22 05:31:54.805000',5),(4,2,'好看','2026-05-22 05:33:57.298000',5),(5,2,'11','2026-05-22 06:02:30.744000',5),(6,9,'很好','2026-05-29 02:28:10.090000',5);
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flight`
--

DROP TABLE IF EXISTS `flight`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flight` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `flight_number` varchar(20) NOT NULL COMMENT '航班号',
  `airline` varchar(50) DEFAULT NULL COMMENT '航空公司',
  `depart_city` varchar(50) NOT NULL COMMENT '出发城市',
  `arrive_city` varchar(50) NOT NULL COMMENT '到达城市',
  `depart_time` datetime NOT NULL COMMENT '出发时间',
  `arrive_time` datetime NOT NULL COMMENT '到达时间',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `remaining_seats` int DEFAULT '100' COMMENT '剩余座位',
  `status` varchar(20) DEFAULT '有效' COMMENT '有效/已失效',
  PRIMARY KEY (`id`),
  KEY `idx_depart_arrive` (`depart_city`,`arrive_city`),
  KEY `idx_depart_time` (`depart_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flight`
--

LOCK TABLES `flight` WRITE;
/*!40000 ALTER TABLE `flight` DISABLE KEYS */;
/*!40000 ALTER TABLE `flight` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flight_order`
--

DROP TABLE IF EXISTS `flight_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flight_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `flight_id` bigint DEFAULT NULL,
  `flight_number` varchar(20) DEFAULT NULL COMMENT '航班号',
  `depart_city` varchar(50) DEFAULT NULL COMMENT '出发城市',
  `arrive_city` varchar(50) DEFAULT NULL COMMENT '到达城市',
  `depart_time` datetime DEFAULT NULL COMMENT '出发时间',
  `arrive_time` datetime DEFAULT NULL COMMENT '到达时间',
  `price` decimal(10,2) NOT NULL,
  `username` varchar(50) NOT NULL COMMENT '下单用户',
  `passenger_name` varchar(50) NOT NULL COMMENT '乘客姓名',
  `passenger_id_card` varchar(18) NOT NULL COMMENT '身份证号',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` varchar(20) DEFAULT '待支付' COMMENT '待支付/已支付/已完成/已取消/已失效',
  `create_time` datetime DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `cancel_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_flight_id` (`flight_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flight_order`
--

LOCK TABLES `flight_order` WRITE;
/*!40000 ALTER TABLE `flight_order` DISABLE KEYS */;
INSERT INTO `flight_order` VALUES (1,NULL,'CA1234','北京首都','上海虹桥','2026-06-07 16:00:00','2026-06-07 18:30:00',850.00,'ysn','杨诗诺','350111200501291602','13845678910','已出票','2026-06-08 01:34:59',NULL,NULL),(2,NULL,'MU5678','北京大兴','上海浦东','2026-06-07 22:00:00','2026-06-08 00:20:00',720.00,'ysn','苏巧云','350111200502182580','13915239871','已出票','2026-06-08 01:36:41',NULL,NULL),(3,NULL,'CA1234','北京首都','上海虹桥','2026-06-07 16:00:00','2026-06-07 18:30:00',850.00,'ysn','陈安琪','350111200507021589','13812581230','已取消','2026-06-08 01:41:23',NULL,'2026-06-08 01:44:53'),(4,NULL,'CA1234','北京首都','上海虹桥','2026-06-15 00:00:00','2026-06-15 02:30:00',850.00,'ysn','陈安琪','350111200501159831','13845678910','已支付','2026-06-08 01:45:46','2026-06-08 01:52:49',NULL),(5,NULL,'CA1234','北京首都','上海虹桥','2026-06-16 00:00:00','2026-06-16 02:30:00',850.00,'caq','陈安琪','350111200507020181','15910284712','已支付','2026-06-09 04:05:35','2026-06-09 04:10:21',NULL),(6,NULL,'HU7605','北京首都','上海虹桥','2026-06-14 07:35:00','2026-06-14 09:45:00',1019.00,'ysn','杨诗诺','350111200501121587','13809523547','已支付','2026-06-13 07:56:50','2026-06-13 07:57:03',NULL),(7,NULL,'CA1507','北京首都','上海虹桥','2026-06-15 23:30:00','2026-06-16 02:00:00',1268.00,'ysn','陈七','350182200507020129','15910238713','已支付','2026-06-14 06:19:33','2026-06-14 06:21:16',NULL),(8,NULL,'CA1234','北京首都','上海虹桥','2026-06-21 00:00:00','2026-06-21 02:30:00',1700.00,'ysn','杨思若','350111200504151236','13856189416','已取消','2026-06-14 09:03:59',NULL,'2026-06-14 09:10:50'),(9,NULL,'CA1234','北京首都','上海虹桥','2026-06-21 00:00:00','2026-06-21 02:30:00',850.00,'ysn','杨四日','350111200502031236','13856189641','已取消','2026-06-14 09:10:36',NULL,NULL),(10,NULL,'CA1234','北京首都','上海虹桥','2026-06-21 00:00:00','2026-06-21 02:30:00',850.00,'ysn','陈安全','350111200503030125','13956418965','已取消','2026-06-14 09:10:36',NULL,'2026-06-14 09:10:48'),(11,NULL,'CA1234','北京首都','上海虹桥','2026-06-22 00:00:00','2026-06-22 02:30:00',850.00,'ysn','严手动','350111200501020123','13605619856','已支付','2026-06-15 02:37:32','2026-06-15 02:37:54',NULL),(12,NULL,'CA1234','北京首都','上海虹桥','2026-06-22 00:00:00','2026-06-22 02:30:00',850.00,'ysn','亚丝娜','350111200501010155','13805894562','已支付','2026-06-15 02:41:30','2026-06-15 02:42:51',NULL);
/*!40000 ALTER TABLE `flight_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hotel`
--

DROP TABLE IF EXISTS `hotel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hotel` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '酒店名称',
  `star_level` int NOT NULL COMMENT '酒店星级',
  `price` double NOT NULL COMMENT '酒店定价',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图URL',
  `category` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `avg_rating` double DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `merchant_id` bigint DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_rooms` int DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `violation_reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='酒店表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hotel`
--

LOCK TABLES `hotel` WRITE;
/*!40000 ALTER TABLE `hotel` DISABLE KEYS */;
INSERT INTO `hotel` VALUES (7,'如家酒店',4,199,'/file/f0acf3e9-56fb-4cab-8a9a-0552f95a7fed_r5VQ4h6CFMDW5dce6b93a8cd36f1480dba781a25c3f9.jpg','商务型酒店','福建省福州市鼓楼区南街街道园垱街28号',4,26.082843903741153,119.29592287401374,4,'营业中',12,'2026-06-18 03:57:56.827000',NULL),(8,'香格里拉大酒店',5,299,'/file/ebaebb65-b2dc-483b-a339-318a113a8415_0Lfx54p8m2Tz7ab619cc636446422d1ec2f219bc15e9.png','连锁酒店','福建省福州市鼓楼区南街街道园垱街21号',5,26.083873066828442,119.29532324704803,5,'营业中',10,NULL,NULL),(9,'诺诺大酒店',5,1999,'/file/bd085b18-8369-4ab7-88d5-35dbef04e762_6IHoNkdZb2ZCc20563165a649286c79dae5954a5bd83.png','公寓型酒店','浙江省衢州市龙游县大街乡',5,28.833375347523205,119.26757876807858,6,'营业中',3,'2026-06-14 08:30:43.041000',NULL),(10,'琪琪大酒店',5,9999,'/file/f09d7114-6fd6-4ff9-8712-9269f91c80f7_sXyNV8HpYWOlc20563165a649286c79dae5954a5bd83.png','度假型酒店','福建省福州市闽侯县竹岐乡',0,26.109000961666094,119.17108934843498,7,'营业中',14,'2026-06-16 02:42:45.172000',NULL),(11,'诺诺小酒店',4,7799,'/file/7f236a14-7a61-4b06-856b-259d9002efd0_QaMME7WOSZBe5dce6b93a8cd36f1480dba781a25c3f9.jpg','商务型酒店','福建省福州市鼓楼区洪山镇柳河路18-南院',0,26.0815394805819,119.29492880935584,6,'营业中',5,'2026-06-16 03:15:47.082000',NULL);
/*!40000 ALTER TABLE `hotel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hotel_image`
--

DROP TABLE IF EXISTS `hotel_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hotel_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(500) NOT NULL COMMENT '详情图URL',
  `hotel_id` bigint NOT NULL COMMENT '关联酒店ID',
  PRIMARY KEY (`id`),
  KEY `hotel_id` (`hotel_id`),
  CONSTRAINT `hotel_image_ibfk_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='酒店详情图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hotel_image`
--

LOCK TABLES `hotel_image` WRITE;
/*!40000 ALTER TABLE `hotel_image` DISABLE KEYS */;
INSERT INTO `hotel_image` VALUES (22,'/file/2778c490-2059-4b9c-a3aa-aca7d2a82e5e_XwThqaMDCcKc7ab619cc636446422d1ec2f219bc15e9.png',7),(23,'/file/562b8638-a5e0-4ef3-82a5-c60f6df21cfd_OEM5Ile0toVE5d8a2dab52562252cc095cd6e01fa2b5.png',7),(24,'/file/540f6961-abe3-4a62-8ae7-7feb4b83a484_7sFufaAqYrXPc20563165a649286c79dae5954a5bd83.png',10),(25,'/file/d026e506-2bd5-41a2-8427-ed2212029c04_So86BbYKg6Bk5dce6b93a8cd36f1480dba781a25c3f9.jpg',10),(26,'/file/d02280bb-aaa0-4fa1-86b5-c44e58d379e1_1Pn2YxRmhqFf5d8a2dab52562252cc095cd6e01fa2b5.png',10),(27,'/file/4e63382e-b37d-47f7-bc0d-ba80cc7ce0f2_Kj9yIXDvA9ZLc20563165a649286c79dae5954a5bd83.png',11);
/*!40000 ALTER TABLE `hotel_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hotelcomment`
--

DROP TABLE IF EXISTS `hotelcomment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hotelcomment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `hotel_id` bigint DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `score` int DEFAULT NULL,
  `images` text,
  `status` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `reply` text,
  `reply_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hotelcomment`
--

LOCK TABLES `hotelcomment` WRITE;
/*!40000 ALTER TABLE `hotelcomment` DISABLE KEYS */;
INSERT INTO `hotelcomment` VALUES (7,'很好','2026-06-09 04:05:09.190000',9,18,5,NULL,'正常',NULL,'感谢您的支持！','2026-06-12 09:10:05.000000'),(8,'挺好的，老板人很好','2026-06-11 02:17:23.904000',7,14,5,NULL,'正常',NULL,'谢谢您的评论~','2026-06-14 14:45:39.000000'),(9,'环境挺好的','2026-06-12 01:18:17.905000',8,22,5,NULL,'正常','ysn','感谢您的支持！','2026-06-12 09:20:44.000000'),(10,'很好，老板很帅','2026-06-14 06:45:01.008000',7,19,3,NULL,'正常','ysn','很帅才三星😒','2026-06-14 14:45:58.000000');
/*!40000 ALTER TABLE `hotelcomment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hotelorder`
--

DROP TABLE IF EXISTS `hotelorder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hotelorder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_in` varchar(255) DEFAULT NULL,
  `check_out` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `hotel_id` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `cancel_reason` varchar(255) DEFAULT NULL,
  `contact_phone` varchar(255) DEFAULT NULL,
  `rating` int DEFAULT NULL,
  `room_count` int DEFAULT NULL,
  `room_type_id` bigint DEFAULT NULL,
  `room_type_name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `cancel_time` datetime(6) DEFAULT NULL,
  `confirm_time` datetime(6) DEFAULT NULL,
  `reject_reason` varchar(255) DEFAULT NULL,
  `merchant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hotelorder`
--

LOCK TABLES `hotelorder` WRITE;
/*!40000 ALTER TABLE `hotelorder` DISABLE KEYS */;
INSERT INTO `hotelorder` VALUES (13,'2026-06-09','2026-06-10','2026-06-08 02:52:24.224000',7,'如家酒店',123,NULL,'ysn','你好','13812345678',NULL,1,1,'大床房','已取消','2026-06-08 03:27:25.301000','2026-06-08 03:15:51.908000',NULL,4),(14,'2026-06-09','2026-06-10','2026-06-08 03:36:09.155000',7,'如家酒店',123,NULL,'ysn',NULL,'15923434134',NULL,1,1,'大床房','已完成',NULL,'2026-06-08 03:36:39.013000',NULL,4),(16,'2026-06-10','2026-06-11','2026-06-09 02:11:08.909000',7,'如家酒店',123,NULL,'rujia',NULL,'15932165653',NULL,1,1,'大床房','已完成',NULL,'2026-06-09 03:53:45.441000',NULL,4),(18,'2026-06-10','2026-06-11','2026-06-09 03:44:02.658000',9,'诺诺大酒店',8999,NULL,'caq',NULL,'15956746534',NULL,1,2,'总统房','已完成',NULL,'2026-06-09 03:44:46.130000',NULL,6),(19,'2026-06-13','2026-06-14','2026-06-12 00:42:27.841000',7,'如家酒店',230,NULL,'ysn',NULL,'13812595633',NULL,2,3,'双人间','已完成',NULL,'2026-06-12 00:43:16.291000',NULL,4),(20,'2026-06-13','2026-06-14','2026-06-12 01:02:11.641000',8,'香格里拉大酒店',125,NULL,'ysn',NULL,'13802594521',NULL,1,5,'双人间','已确认',NULL,'2026-06-12 01:03:06.825000',NULL,5),(21,'2026-06-13','2026-06-14','2026-06-12 01:14:27.034000',9,'诺诺大酒店',8999,NULL,'ysn','住不起','13856425562',NULL,1,2,'总统房','已取消','2026-06-16 03:18:33.279000','2026-06-12 01:14:57.178000',NULL,6),(22,'2026-06-12','2026-06-13','2026-06-12 01:16:08.561000',8,'香格里拉大酒店',105,NULL,'ysn',NULL,'13695484522',NULL,1,4,'单人间','已完成',NULL,'2026-06-12 01:17:42.199000',NULL,5),(23,'2026-06-15','2026-06-16','2026-06-14 06:13:38.815000',7,'如家酒店',246,NULL,'ysn',NULL,'15880567621',NULL,2,1,'大床房','已完成',NULL,'2026-06-14 06:29:33.786000',NULL,4),(24,'2026-06-16','2026-06-17','2026-06-15 02:36:35.191000',7,'如家酒店',123,NULL,'ysn',NULL,'13825614856',NULL,1,1,'大床房','已完成',NULL,'2026-06-18 01:28:44.658000',NULL,4),(25,'2026-06-16','2026-06-17','2026-06-15 02:49:43.918000',10,'琪琪大酒店',9888,NULL,'ysn',NULL,'13806828994',NULL,1,7,'总统套房','待确认',NULL,NULL,NULL,7),(26,'2026-06-17','2026-06-18','2026-06-16 02:37:18.860000',10,'琪琪大酒店',9888,NULL,'ysn',NULL,'15954165189',NULL,1,7,'总统套房','待支付',NULL,NULL,NULL,7),(27,'2026-06-17','2026-06-18','2026-06-16 03:17:47.001000',11,'诺诺小酒店',7799,NULL,'ysn',NULL,'15951654651',NULL,1,8,'总统房','已完成',NULL,'2026-06-16 03:18:35.161000',NULL,6),(28,'2026-06-19','2026-06-20','2026-06-18 00:45:38.243000',11,'诺诺小酒店',7799,NULL,'ysn','不要脸','13586451320',NULL,1,8,'总统房','已取消','2026-06-18 00:54:43.864000','2026-06-18 00:46:43.505000',NULL,6),(29,'2026-06-19','2026-06-20','2026-06-18 00:50:16.148000',11,'诺诺小酒店',7799,NULL,'ysn','test','13987465132',NULL,1,8,'总统房','已取消','2026-06-18 00:54:42.115000',NULL,NULL,6),(30,'2026-06-19','2026-06-20','2026-06-18 01:14:22.575000',11,'诺诺小酒店',7799,NULL,'ysn',NULL,'15868412348',NULL,1,8,'总统房','已确认',NULL,'2026-06-18 01:15:27.169000',NULL,6),(31,'2026-06-19','2026-06-20','2026-06-18 01:28:00.178000',7,'如家酒店',123,NULL,'ysn',NULL,'13576986451',NULL,1,1,'大床房','已确认',NULL,'2026-06-18 01:28:50.196000',NULL,4),(32,'2026-06-19','2026-06-20','2026-06-18 01:29:46.356000',7,'如家酒店',115,NULL,'ysn',NULL,'13848965121',NULL,1,3,'双人间','待确认',NULL,NULL,NULL,4),(33,'2026-06-19','2026-06-20','2026-06-18 01:38:50.929000',8,'香格里拉大酒店',105,NULL,'ysn',NULL,'13598956238',NULL,1,4,'单人间','待确认',NULL,NULL,NULL,5),(34,'2026-06-21','2026-06-22','2026-06-18 01:51:39.234000',8,'香格里拉大酒店',105,NULL,'ysn',NULL,'13687456985',NULL,1,4,'单人间','已确认',NULL,'2026-06-18 01:52:07.778000',NULL,5),(35,'2026-06-20','2026-06-21','2026-06-18 01:57:35.950000',11,'诺诺小酒店',7799,NULL,'ysn',NULL,'13985412698',NULL,1,8,'总统房','已确认',NULL,'2026-06-18 01:58:30.384000',NULL,6),(36,'2026-06-19','2026-06-20','2026-06-18 02:02:14.627000',10,'琪琪大酒店',9888,NULL,'caq',NULL,'13978946531',NULL,1,7,'总统套房','已确认',NULL,'2026-06-18 02:02:56.161000',NULL,7),(37,'2026-06-17','2026-06-20','2026-06-18 03:03:22.942000',7,'如家酒店',369,NULL,'ysn',NULL,'13948563415',NULL,1,1,'大床房','待支付',NULL,NULL,NULL,4);
/*!40000 ALTER TABLE `hotelorder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `hotel_id` bigint DEFAULT NULL,
  `merchant_id` varchar(50) DEFAULT NULL,
  `username` varchar(100) DEFAULT NULL,
  `content` text,
  `sender_role` varchar(20) DEFAULT NULL,
  `is_read` int DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,14,7,'4','ysn','你好','user',1,'2026-06-09 01:41:49'),(2,14,7,'4','ysn','您好！请问有什么可以帮您的？很高兴为您服务！','merchant',1,'2026-06-09 01:41:49'),(3,14,7,'4','ysn','我希望换房间','user',1,'2026-06-09 01:48:16'),(4,14,7,'4','ysn','感谢您的留言！如家酒店会尽快处理您的问题。祝您生活愉快！','merchant',1,'2026-06-09 01:48:16'),(5,16,7,'4','rujia','你好','user',1,'2026-06-09 02:11:39'),(6,16,7,'4','rujia','您好！如家酒店很高兴为您服务！请问有什么可以帮您的？','merchant',0,'2026-06-09 02:11:40'),(7,14,7,'4','ysn','你好','user',1,'2026-06-09 02:18:49'),(8,14,7,'4','ysn','您好！如家酒店很高兴为您服务！请问有什么可以帮您的？','merchant',1,'2026-06-09 02:18:50'),(9,18,9,'6','caq','好的，谢谢','user',1,'2026-06-09 03:44:21'),(10,18,9,'6','caq','不客气！诺诺大酒店祝您旅途愉快，有任何问题随时联系我们！😊','merchant',1,'2026-06-09 03:44:21'),(11,18,9,'6','caq','好的，收到！','merchant',1,'2026-06-09 03:44:55'),(12,20,8,'5','ysn','酒店地址在哪？','user',1,'2026-06-12 01:02:40'),(13,20,8,'5','ysn','香格里拉大酒店的地址请在订单详情中查看，您也可以打开地图导航。','merchant',1,'2026-06-12 01:02:40'),(14,23,7,'4','ysn','123','merchant',1,'2026-06-14 06:46:16'),(15,23,7,'4','ysn','i你好','merchant',1,'2026-06-14 06:46:23'),(16,23,7,'4','ysn','123','merchant',1,'2026-06-14 06:46:29'),(17,23,7,'4','ysn','好的，谢谢','user',1,'2026-06-15 02:44:22'),(18,23,7,'4','ysn','不客气！如家酒店祝您旅途愉快，有任何问题随时联系我们！😊','merchant',1,'2026-06-15 02:44:22'),(19,25,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-15 03:17:16'),(20,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-15 03:17:16'),(21,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-15 03:17:16'),(22,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-15 03:17:16'),(23,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-15 03:17:16'),(24,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:17:36'),(25,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:17:36'),(26,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:17:36'),(27,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:17:36'),(28,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:17:36'),(29,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:17:36'),(30,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:18:52'),(31,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:18:52'),(32,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:18:52'),(33,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:18:52'),(34,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:18:52'),(35,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:18:52'),(36,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:18:52'),(37,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:18:52'),(38,27,11,'6','ysn','请问有优惠吗？','user',1,'2026-06-16 03:19:01'),(39,27,11,'6','ysn','感谢您对诺诺小酒店的关注！我们目前有暑期特惠活动，部分房型低至8折。','merchant',1,'2026-06-16 03:19:02'),(40,27,11,'6','ysn','请问有优惠吗？','user',1,'2026-06-16 03:19:02'),(41,27,11,'6','ysn','感谢您对诺诺小酒店的关注！我们目前有暑期特惠活动，部分房型低至8折。','merchant',1,'2026-06-16 03:19:02'),(42,18,9,'6','caq','穷鬼','merchant',1,'2026-06-16 03:19:37'),(43,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:30:55'),(44,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:30:55'),(45,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:30:55'),(46,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:30:55'),(47,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:30:55'),(48,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:30:55'),(49,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:30:55'),(50,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:30:55'),(51,27,11,'6','ysn','啊啊啊','user',1,'2026-06-16 03:31:05'),(52,27,11,'6','ysn','感谢您的留言！诺诺小酒店会尽快处理您的问题。祝您生活愉快！','merchant',1,'2026-06-16 03:31:06'),(53,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:33:49'),(54,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:33:49'),(55,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:33:49'),(56,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:33:49'),(57,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:33:49'),(58,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:33:49'),(59,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:33:49'),(60,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:33:49'),(61,27,11,'6','ysn','好的，谢谢','user',1,'2026-06-16 03:34:41'),(62,27,11,'6','ysn','不客气！诺诺小酒店祝您旅途愉快，有任何问题随时联系我们！😊','merchant',1,'2026-06-16 03:34:42'),(63,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:47:08'),(64,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:47:08'),(65,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:47:08'),(66,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:47:08'),(67,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:47:08'),(68,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:47:08'),(69,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:47:08'),(70,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-16 03:47:08'),(71,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:38:49'),(72,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:38:49'),(73,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:38:49'),(74,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:38:49'),(75,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:38:49'),(76,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:38:49'),(77,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:38:49'),(78,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:38:49'),(79,27,11,'6','ysn','好的，谢谢','user',1,'2026-06-18 00:39:10'),(80,27,11,'6','ysn','不客气！诺诺小酒店祝您旅途愉快，有任何问题随时联系我们！😊','merchant',1,'2026-06-18 00:39:11'),(81,27,11,'6','ysn','好的，谢谢','user',1,'2026-06-18 00:39:11'),(82,27,11,'6','ysn','不客气！诺诺小酒店祝您旅途愉快，有任何问题随时联系我们！😊','merchant',1,'2026-06-18 00:39:12'),(83,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:40:40'),(84,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:40:40'),(85,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:40:40'),(86,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:40:40'),(87,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:40:40'),(88,27,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:40:40'),(89,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:40:40'),(90,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:40:40'),(91,28,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-19\n📅 退房日期：2026-06-20\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:45:41'),(92,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:47:11'),(93,28,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-19\n📅 退房日期：2026-06-20\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:47:11'),(94,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:47:11'),(95,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:47:11'),(96,28,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-19\n📅 退房日期：2026-06-20\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:47:11'),(97,26,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-17\n📅 退房日期：2026-06-18\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:47:11'),(98,22,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-12\n📅 退房日期：2026-06-13\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:47:11'),(99,24,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥123\n\n如有任何问题，请随时联系我们！祝您入住愉快！','merchant',1,'2026-06-18 00:47:11'),(100,29,11,'6','ysn','好的，收到！','merchant',1,'2026-06-18 00:54:35'),(129,22,8,'5','ysn','好的，谢谢','user',1,'2026-06-18 01:38:33'),(130,22,8,'5','ysn','不客气！香格里拉大酒店祝您旅途愉快，有任何问题随时联系我们！😊','merchant',1,'2026-06-18 01:38:33'),(131,33,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-19\n📅 退房日期：2026-06-20\n💰 订单金额：¥105\n\n您已支付成功，等待商家确认，如有任何问题，请随时联系我们！','merchant',1,'2026-06-18 01:39:04'),(132,34,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-21\n📅 退房日期：2026-06-22\n💰 订单金额：¥105\n\n如有任何问题，请随时联系我们，欢迎您的入住~','merchant',1,'2026-06-18 01:52:08'),(133,32,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-19\n📅 退房日期：2026-06-20\n💰 订单金额：¥115\n\n您已支付成功，等待商家确认，如有任何问题，请随时联系我们！','merchant',1,'2026-06-18 01:52:38'),(134,25,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥9888\n\n您已支付成功，等待商家确认，如有任何问题，请随时联系我们！','merchant',1,'2026-06-18 01:52:38'),(135,33,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-19\n📅 退房日期：2026-06-20\n💰 订单金额：¥105\n\n您已支付成功，等待商家确认，如有任何问题，请随时联系我们！','merchant',1,'2026-06-18 01:52:38'),(136,33,8,'5','ysn','🎉 欢迎预订香格里拉大酒店！\n\n📅 入住日期：2026-06-19\n📅 退房日期：2026-06-20\n💰 订单金额：¥105\n\n您已支付成功，等待商家确认，如有任何问题，请随时联系我们！','merchant',1,'2026-06-18 01:52:38'),(137,32,7,'4','ysn','🎉 欢迎预订如家酒店！\n\n📅 入住日期：2026-06-19\n📅 退房日期：2026-06-20\n💰 订单金额：¥115\n\n您已支付成功，等待商家确认，如有任何问题，请随时联系我们！','merchant',1,'2026-06-18 01:52:38'),(138,25,10,'7','ysn','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-16\n📅 退房日期：2026-06-17\n💰 订单金额：¥9888\n\n您已支付成功，等待商家确认，如有任何问题，请随时联系我们！','merchant',1,'2026-06-18 01:52:38'),(139,35,11,'6','ysn','🎉 欢迎预订诺诺小酒店！\n\n📅 入住日期：2026-06-20\n📅 退房日期：2026-06-21\n💰 订单金额：¥7799\n\n如有任何问题，请随时联系我们，欢迎您的入住~','merchant',1,'2026-06-18 01:58:30'),(140,36,10,'7','caq','🎉 欢迎预订琪琪大酒店！\n\n📅 入住日期：2026-06-19\n📅 退房日期：2026-06-20\n💰 订单金额：¥9888\n\n如有任何问题，请随时联系我们，欢迎您的入住~','merchant',1,'2026-06-18 02:02:56'),(141,16,7,'4','rujia','好的，收到！','merchant',0,'2026-06-18 02:32:16');
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `merchant_name` varchar(100) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL,
  `content` text,
  `related_id` varchar(50) DEFAULT NULL,
  `related_name` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'UNREAD',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `reply` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,4,'如家酒店','HOTEL_BANNED','酒店已被禁用','您的酒店「如家酒店」已被管理员禁用，原因：违规经营。请及时处理。','7','如家酒店','READ','2026-06-13 06:33:34',NULL),(2,4,'如家酒店','HOTEL_UNBANNED','酒店已恢复','您的酒店「如家酒店」已恢复正常营业。','7','如家酒店','READ','2026-06-13 06:50:13',NULL),(3,4,'如家酒店','HOTEL_BANNED','酒店已被禁用','您的酒店「如家酒店」已被管理员禁用，原因：违规经营。请及时处理。','7','如家酒店','READ','2026-06-13 06:50:22',NULL),(4,4,'如家酒店','APPEAL_REPLY','申诉已通过','您的申诉已通过审核，账号已恢复正常。','4','如家酒店','READ','2026-06-13 06:51:19','收到'),(5,4,'如家酒店','HOTEL_UNBANNED','酒店已恢复','您的酒店「如家酒店」已恢复正常营业。','7','如家酒店','READ','2026-06-13 06:51:32',NULL),(6,1,'ysn','USER_UNBANNED','账号已解禁','您的账号已恢复正常，可以继续发布评论和评价了。','1','ysn','READ','2026-06-13 07:17:18',NULL),(7,1,'ysn','USER_BANNED','账号已被禁言','您的账号已被管理员禁言，原因：违规言论。禁言期间无法发布评论和评价。','1','ysn','READ','2026-06-13 07:17:33',NULL),(8,1,'ysn','USER_UNBANNED','账号已解禁','您的账号已恢复正常，可以继续发布评论和评价了。','1','ysn','READ','2026-06-13 07:17:58',NULL),(9,1,'ysn','USER_BANNED','账号已被禁言','您的账号已被管理员禁言，原因：违规言论。禁言期间无法发布评论和评价。','1','ysn','READ','2026-06-13 07:18:09',NULL),(10,1,'ysn','USER_UNBANNED','账号已解禁','您的账号已恢复正常，可以继续发布评论和评价了。','1','ysn','READ','2026-06-13 07:25:02',NULL),(11,1,'ysn','USER_UNBANNED','账号已解禁','您的账号已恢复正常，可以继续发布评论和评价了。','1','ysn','READ','2026-06-13 07:25:02',NULL),(12,1,'ysn','USER_BANNED','账号已被禁言','您的账号已被管理员禁言1天，原因：违规。禁言期间无法发布评论和评价。','1','ysn','READ','2026-06-13 07:25:12',NULL),(13,6,'诺诺大酒店','HOTEL_BANNED','酒店已被禁用','您的酒店「诺诺大酒店」已被管理员禁用，原因：色情低俗。请及时处理。','9','诺诺大酒店','READ','2026-06-14 06:30:59',NULL),(14,1,'ysn','USER_UNBANNED','账号已解禁','您的账号已恢复正常，可以继续发布评论和评价了。','1','ysn','READ','2026-06-14 06:32:39',NULL),(15,1,'ysn','USER_UNBANNED','账号已解禁','您的账号已恢复正常，可以继续发布评论和评价了。','1','ysn','READ','2026-06-14 06:32:39',NULL),(16,4,'如家大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','4','如家大酒店','READ','2026-06-14 06:32:48',NULL),(17,4,'如家大酒店','APPEAL_REPLY','申诉未通过','您的申诉未被通过。','4','如家大酒店','READ','2026-06-14 06:42:15','gun'),(18,4,'如家大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','4','如家大酒店','READ','2026-06-14 06:45:10',NULL),(19,6,'诺诺大酒店','HOTEL_UNBANNED','酒店已恢复','您的酒店「诺诺大酒店」已恢复正常营业。','9','诺诺大酒店','READ','2026-06-14 08:30:43',NULL),(20,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','READ','2026-06-16 01:04:41',NULL),(21,7,'琪琪大酒店','APPEAL_REPLY','申诉已通过','您的申诉已通过审核，账号已恢复正常。','7','琪琪大酒店','READ','2026-06-16 01:07:07','ok'),(22,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','READ','2026-06-16 01:08:58',NULL),(23,7,'琪琪大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','7','琪琪大酒店','READ','2026-06-16 01:09:03',NULL),(24,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','READ','2026-06-16 01:09:14',NULL),(25,7,'琪琪大酒店','HOTEL_UNBANNED','酒店已恢复','您的酒店「琪琪大酒店」已恢复正常营业。','10','琪琪大酒店','READ','2026-06-16 01:09:29',NULL),(26,7,'琪琪大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','7','琪琪大酒店','READ','2026-06-16 01:09:38',NULL),(27,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','READ','2026-06-16 01:09:41',NULL),(28,7,'琪琪大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','7','琪琪大酒店','READ','2026-06-16 01:09:50',NULL),(29,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','UNREAD','2026-06-16 01:15:29',NULL),(30,7,'琪琪大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','7','琪琪大酒店','UNREAD','2026-06-16 01:32:03',NULL),(31,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','UNREAD','2026-06-16 01:32:12',NULL),(32,7,'琪琪大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','7','琪琪大酒店','UNREAD','2026-06-16 01:44:58',NULL),(33,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','UNREAD','2026-06-16 01:53:00',NULL),(34,7,'琪琪大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','7','琪琪大酒店','UNREAD','2026-06-16 01:53:41',NULL),(35,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','UNREAD','2026-06-16 01:58:07',NULL),(36,7,'琪琪大酒店','APPEAL_REPLY','申诉已通过','您的申诉已通过审核，账号已恢复正常。','7','琪琪大酒店','UNREAD','2026-06-16 02:02:04','好的'),(37,4,'如家大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','4','如家大酒店','READ','2026-06-16 02:13:44',NULL),(38,4,'如家大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','4','如家大酒店','READ','2026-06-16 02:13:48',NULL),(39,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','UNREAD','2026-06-16 02:13:51',NULL),(40,7,'琪琪大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','7','琪琪大酒店','UNREAD','2026-06-16 02:25:42',NULL),(41,7,'琪琪大酒店','MERCHANT_BANNED','账号已被禁用','您的商家账号已被管理员禁用，原因：违规经营。如有疑问，请通过申诉功能联系管理员。','7','琪琪大酒店','UNREAD','2026-06-16 02:25:52',NULL),(42,7,'琪琪大酒店','MERCHANT_UNBANNED','账号已恢复','您的商家账号已恢复正常使用，感谢您的配合。','7','琪琪大酒店','UNREAD','2026-06-16 02:42:45',NULL),(43,7,'琪琪大酒店','APPEAL_REPLY','申诉已通过','您的申诉已通过审核，账号已恢复正常。','7','琪琪大酒店','UNREAD','2026-06-18 03:15:14','ok'),(44,4,'如家大酒店','HOTEL_BANNED','酒店已被禁用','您的酒店「如家酒店」已被管理员禁用，原因：违规经营。请及时处理。','7','如家酒店','UNREAD','2026-06-18 03:57:45',NULL),(45,4,'如家大酒店','HOTEL_UNBANNED','酒店已恢复','您的酒店「如家酒店」已恢复正常营业。','7','如家酒店','UNREAD','2026-06-18 03:57:57',NULL);
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,1,'快餐',36.40,1);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `total_price` decimal(10,2) NOT NULL,
  `total_quantity` int NOT NULL,
  `order_date` datetime NOT NULL,
  `check_in` varchar(255) DEFAULT NULL,
  `check_out` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `hotel_id` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,36.40,1,'2026-05-11 08:27:22',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,0.00,0,'2026-05-22 02:14:20',NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room_type`
--

DROP TABLE IF EXISTS `room_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `available_count` int DEFAULT NULL,
  `bed_type` varchar(255) DEFAULT NULL,
  `breakfast` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `size` varchar(255) DEFAULT NULL,
  `total_count` int DEFAULT NULL,
  `type_name` varchar(255) DEFAULT NULL,
  `window_status` varchar(255) DEFAULT NULL,
  `hotel_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8sgnny12n0v74j6u7u94w7mxp` (`hotel_id`),
  CONSTRAINT `FK8sgnny12n0v74j6u7u94w7mxp` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room_type`
--

LOCK TABLES `room_type` WRITE;
/*!40000 ALTER TABLE `room_type` DISABLE KEYS */;
INSERT INTO `room_type` VALUES (1,2,'大床','含早',123,'50㎡',10,'大床房','有窗',7),(2,1,'双床','含早',8999,'200',2,'总统房','无窗',9),(3,7,'双床','不含早',115,'40㎡',10,'双人间','有窗',7),(4,17,'单人床','含早',105,'23㎡',20,'单人间','有窗',8),(5,9,'双床','不含早',125,'40㎡',10,'双人间','有窗',8),(6,10,'大床','不含早',120,'35㎡',10,'大床房','有窗',8),(7,3,'大床','含早',9888,'100㎡',6,'总统套房','有窗',10),(8,2,'双床','含早',7799,'2778',5,'总统房','有窗',11);
/*!40000 ALTER TABLE `room_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `score`
--

DROP TABLE IF EXISTS `score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `score` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attraction_id` bigint DEFAULT NULL,
  `score` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `score`
--

LOCK TABLES `score` WRITE;
/*!40000 ALTER TABLE `score` DISABLE KEYS */;
INSERT INTO `score` VALUES (1,1,1),(2,1,1),(3,1,5),(4,1,5),(5,1,5),(6,1,5),(7,1,5),(8,1,5),(9,1,5),(10,1,5),(11,1,5),(12,1,5),(13,1,5),(14,1,5),(15,1,5),(16,1,5),(17,1,5),(18,1,5),(19,1,5),(20,1,5),(21,1,5),(22,1,5),(23,1,5);
/*!40000 ALTER TABLE `score` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_notice`
--

DROP TABLE IF EXISTS `system_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收通知的用户名',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型：HOTEL_ORDER, FLIGHT_ORDER',
  `order_id` bigint NOT NULL COMMENT '关联的订单ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `emoji` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 0xF09F93A2 COMMENT '图标emoji',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_order_type` (`order_id`,`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_notice`
--

LOCK TABLES `system_notice` WRITE;
/*!40000 ALTER TABLE `system_notice` DISABLE KEYS */;
INSERT INTO `system_notice` VALUES (1,'ysn','HOTEL_ORDER',24,'酒店预订成功','您预订的如家酒店已确认！入住日期：2026-06-16，退房日期：2026-06-17，金额：¥123.00。祝您入住愉快！','🏨',1,'2026-06-15 02:36:54'),(2,'ysn','FLIGHT_ORDER',12,'机票订购成功','您预订的北京首都 → 上海虹桥机票已出票成功！航班号：CA1234，起飞时间：2026-06-22 08:00，金额：¥850.00。请提前2小时到达机场！','✈️',1,'2026-06-15 02:42:51'),(3,'ysn','HOTEL_ORDER',25,'酒店预订成功','您预订的琪琪大酒店已确认！入住日期：2026-06-16，退房日期：2026-06-17，金额：¥9888.00。祝您入住愉快！','🏨',1,'2026-06-15 02:49:55'),(4,'ysn','HOTEL_ORDER',27,'酒店预订成功','您预订的诺诺小酒店已确认！入住日期：2026-06-17，退房日期：2026-06-18，金额：¥7799.00。祝您入住愉快！','🏨',1,'2026-06-16 03:18:04'),(5,'ysn','HOTEL_ORDER',28,'酒店预订成功','您预订的诺诺小酒店已确认！入住日期：2026-06-19，退房日期：2026-06-20，金额：¥7799.00。祝您入住愉快！','🏨',1,'2026-06-18 00:46:14'),(6,'ysn','HOTEL_ORDER',29,'酒店预订成功','您预订的诺诺小酒店已确认！入住日期：2026-06-19，退房日期：2026-06-20，金额：¥7799.00。祝您入住愉快！','🏨',1,'2026-06-18 00:50:31'),(7,'ysn','HOTEL_ORDER',30,'酒店预订成功','您预订的诺诺小酒店已确认！入住日期：2026-06-19，退房日期：2026-06-20，金额：¥7799.00。祝您入住愉快！','🏨',1,'2026-06-18 01:14:43'),(8,'ysn','HOTEL_ORDER',31,'酒店预订成功','您预订的如家酒店已确认！入住日期：2026-06-19，退房日期：2026-06-20，金额：¥123.00。祝您入住愉快！','🏨',1,'2026-06-18 01:28:08'),(9,'ysn','HOTEL_ORDER',32,'酒店预订成功','您预订的如家酒店已确认！入住日期：2026-06-19，退房日期：2026-06-20，金额：¥115.00。祝您入住愉快！','🏨',1,'2026-06-18 01:29:56'),(10,'ysn','HOTEL_ORDER',33,'酒店预订成功','您预订的香格里拉大酒店已确认！入住日期：2026-06-19，退房日期：2026-06-20，金额：¥105.00。祝您入住愉快！','🏨',1,'2026-06-18 01:39:01'),(11,'ysn','HOTEL_ORDER',34,'酒店预订成功','您预订的香格里拉大酒店已确认！入住日期：2026-06-21，退房日期：2026-06-22，金额：¥105.00。祝您入住愉快！','🏨',1,'2026-06-18 01:51:47'),(12,'ysn','HOTEL_ORDER',35,'酒店预订成功','您预订的诺诺小酒店已确认！入住日期：2026-06-20，退房日期：2026-06-21，金额：¥7799.00。祝您入住愉快！','🏨',1,'2026-06-18 01:57:46'),(13,'caq','HOTEL_ORDER',36,'酒店预订成功','您预订的琪琪大酒店已确认！入住日期：2026-06-19，退房日期：2026-06-20，金额：¥9888.00。祝您入住愉快！','🏨',1,'2026-06-18 02:02:23');
/*!40000 ALTER TABLE `system_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `role` varchar(20) DEFAULT 'USER' COMMENT 'USER/MERCHANT',
  `status` varchar(20) DEFAULT 'NORMAL' COMMENT 'NORMAL/BANNED',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `shop_name` varchar(200) DEFAULT NULL COMMENT '商家名称',
  `shop_description` text COMMENT '商家简介',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `ban_expire_time` datetime(6) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `ban_reason` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `shop_address` varchar(255) DEFAULT NULL,
  `shop_phone` varchar(255) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'111','ysn','894f045e-56f5-4a55-957b-b579cba90177','USER','NORMAL','13809523654','1459858632@qq.com',NULL,NULL,'/file/e4b98d92-de16-497f-816c-896e040268fc_8NTqaMiYQ5kIa4e9d641308a07ed2108b8bc489e32e9.jpg',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-18 03:13:27.869000'),(2,'111','caq','7183b372-e926-44b4-99c0-07bdc1680e67','USER','NORMAL','15836547892',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-18 02:03:04.037000'),(3,'111','ldq',NULL,'USER','NORMAL','13658972369',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,'123','如家','26502c6b-a852-480a-89ec-9ce20dd8f91f','MERCHANT','NORMAL','12345678910',NULL,'如家大酒店',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-18 04:00:44.808000'),(5,'123','香格里拉酒店','1978b45c-b281-4788-bcd8-ad03f20ae425','MERCHANT','NORMAL','13809521364',NULL,'香格里拉酒店',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-18 01:51:59.061000'),(6,'123','诺诺','15a48690-8177-4235-8857-51d009973d03','MERCHANT','NORMAL','15980567632',NULL,'诺诺大酒店',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-18 04:10:14.817000'),(7,'123','琪琪','18b65f38-ef9c-4a7d-962e-bd9d96c69198','MERCHANT','NORMAL','13856428974',NULL,'琪琪大酒店',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-18 03:15:13.748000'),(8,'111','sqy',NULL,'USER','NORMAL','18965473256',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-23  8:45:17
