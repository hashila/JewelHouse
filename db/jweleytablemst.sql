-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.45 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.16.0.7229
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for jwelryhousemst
CREATE DATABASE IF NOT EXISTS `jwelryhousemst` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `jwelryhousemst`;

-- Dumping structure for table jwelryhousemst.item
CREATE TABLE IF NOT EXISTS `item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `metal_type` varchar(50) NOT NULL,
  `weight` decimal(19,2) NOT NULL,
  `making_charges` decimal(19,2) NOT NULL,
  `availability` char(1) NOT NULL,
  `status` char(1) NOT NULL,
  `image` longblob,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table jwelryhousemst.item: ~1 rows (approximately)

-- Dumping structure for table jwelryhousemst.item_tax
CREATE TABLE IF NOT EXISTS `item_tax` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tax_name` varchar(100) NOT NULL,
  `tax_percentage` decimal(5,2) NOT NULL,
  `item_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_item_tax_item_id` (`item_id`),
  CONSTRAINT `fk_item_tax_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table jwelryhousemst.item_tax: ~2 rows (approximately)

-- Dumping structure for table jwelryhousemst.metal
CREATE TABLE IF NOT EXISTS `metal` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table jwelryhousemst.metal: ~4 rows (approximately)
INSERT INTO `metal` (`id`, `name`, `code`) VALUES
	(1, 'Gold', 'XAU'),
	(2, 'Silver', 'XAG'),
	(3, 'Platinum', 'XPL'),
	(4, 'Nickel', 'NI');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
