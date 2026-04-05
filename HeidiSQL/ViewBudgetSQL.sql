-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.4.3 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for budgetbuddyproject
CREATE DATABASE IF NOT EXISTS `budgetbuddyproject` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `budgetbuddyproject`;

-- Dumping structure for table budgetbuddyproject.card
CREATE TABLE IF NOT EXISTS `card` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `card_num` varchar(16) NOT NULL DEFAULT '0',
  `card_pin` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0',
  `expiry_date` timestamp NOT NULL,
  `existing money_id` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index 1` (`card_num`),
  UNIQUE KEY `existing money_id` (`existing money_id`),
  CONSTRAINT `FK_card_cardholder` FOREIGN KEY (`card_num`) REFERENCES `cardholder` (`card_num`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_card_existingmoney` FOREIGN KEY (`existing money_id`) REFERENCES `existingmoney` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table budgetbuddyproject.card: ~4 rows (approximately)
INSERT INTO `card` (`id`, `card_num`, `card_pin`, `expiry_date`, `existing money_id`) VALUES
	(1, '2771443994108755', '0123', '2027-03-14 23:50:34', 1),
	(2, '6490165855421882', '0000', '2027-03-15 01:25:40', NULL),
	(3, '7058932443825937', '1234', '2027-03-15 01:58:10', NULL),
	(4, '3071396427656664', '2345', '2027-03-15 04:36:39', NULL);

-- Dumping structure for table budgetbuddyproject.cardholder
CREATE TABLE IF NOT EXISTS `cardholder` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `First_name` varchar(255) NOT NULL,
  `Last_name` varchar(255) NOT NULL,
  `card_pin` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `card_num` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE` (`card_num`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table budgetbuddyproject.cardholder: ~4 rows (approximately)
INSERT INTO `cardholder` (`id`, `First_name`, `Last_name`, `card_pin`, `card_num`) VALUES
	(1, 'Patricia', 'Winter', '0123', '2771443994108755'),
	(2, 'Alex Venise', 'Legarde', '0000', '6490165855421882'),
	(3, 'Hailey', 'Mendez', '1234', '7058932443825937'),
	(4, 'Leo', 'Mord', '2345', '3071396427656664');

-- Dumping structure for table budgetbuddyproject.categories
CREATE TABLE IF NOT EXISTS `categories` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `budget` double NOT NULL DEFAULT (0),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table budgetbuddyproject.categories: ~1 rows (approximately)
INSERT INTO `categories` (`id`, `name`, `budget`) VALUES
	(1, 'food', 200);

-- Dumping structure for table budgetbuddyproject.existingmoney
CREATE TABLE IF NOT EXISTS `existingmoney` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `balance` double NOT NULL DEFAULT (0),
  `deposit` double DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table budgetbuddyproject.existingmoney: ~1 rows (approximately)
INSERT INTO `existingmoney` (`id`, `balance`, `deposit`) VALUES
	(1, 500, 0);

-- Dumping structure for table budgetbuddyproject.expenses
CREATE TABLE IF NOT EXISTS `expenses` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_id` bigint unsigned DEFAULT NULL,
  `price` double NOT NULL DEFAULT (0),
  `task` varchar(255) NOT NULL DEFAULT '0',
  `date` timestamp NOT NULL,
  `status` enum('essentials','treats') NOT NULL,
  `card_id` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `card_id` (`card_id`),
  UNIQUE KEY `category_id` (`category_id`),
  CONSTRAINT `FK_expenses_card` FOREIGN KEY (`card_id`) REFERENCES `card` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `FK_expenses_categories` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table budgetbuddyproject.expenses: ~1 rows (approximately)
INSERT INTO `expenses` (`id`, `category_id`, `price`, `task`, `date`, `status`, `card_id`) VALUES
	(1, 1, 30, 'jollibee', '2026-03-14 23:51:54', 'treats', 1);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
