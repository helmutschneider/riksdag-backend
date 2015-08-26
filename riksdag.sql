-- --------------------------------------------------------
-- Värd:                         127.0.0.1
-- Server version:               10.0.20-MariaDB-log - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             9.1.0.4867
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping structure for table riksdag.document
CREATE TABLE IF NOT EXISTS `document` (
  `document_id` int(11) NOT NULL AUTO_INCREMENT,
  `remote_id` varchar(50) NOT NULL,
  `published_at` datetime NOT NULL,
  `title` text NOT NULL,
  `url` text NOT NULL,
  `voting_id` int(11) NOT NULL,
  PRIMARY KEY (`document_id`),
  KEY `FK_document_voting` (`voting_id`),
  CONSTRAINT `FK_document_voting` FOREIGN KEY (`voting_id`) REFERENCES `voting` (`voting_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data exporting was unselected.


-- Dumping structure for table riksdag.person
CREATE TABLE IF NOT EXISTS `person` (
  `person_id` int(11) NOT NULL AUTO_INCREMENT,
  `remote_id` varchar(50) NOT NULL,
  `birth_year` year(4) NOT NULL,
  `gender` tinyint(4) NOT NULL,
  `first_name` text NOT NULL,
  `last_name` text NOT NULL,
  `party` varchar(50) NOT NULL,
  `location` text NOT NULL,
  `image_url` text NOT NULL,
  `status` tinyint(4) NOT NULL,
  `sync_id` int(11) NOT NULL,
  PRIMARY KEY (`person_id`),
  KEY `FK_person_sync` (`sync_id`),
  KEY `status_idx` (`status`),
  KEY `gender_idx` (`gender`),
  KEY `birth_year_idx` (`birth_year`),
  KEY `party_idx` (`party`),
  CONSTRAINT `FK_person_sync` FOREIGN KEY (`sync_id`) REFERENCES `sync` (`sync_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data exporting was unselected.


-- Dumping structure for table riksdag.sync
CREATE TABLE IF NOT EXISTS `sync` (
  `sync_id` int(11) NOT NULL AUTO_INCREMENT,
  `started_at` datetime NOT NULL,
  `completed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`sync_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data exporting was unselected.


-- Dumping structure for table riksdag.vote
CREATE TABLE IF NOT EXISTS `vote` (
  `vote_id` int(11) NOT NULL AUTO_INCREMENT,
  `person_id` int(11) NOT NULL,
  `voting_id` int(11) NOT NULL,
  `result` tinyint(4) NOT NULL,
  PRIMARY KEY (`vote_id`),
  KEY `person_id` (`person_id`),
  KEY `voting_ibfk_2` (`voting_id`),
  CONSTRAINT `vote_ibfk_1` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `vote_ibfk_2` FOREIGN KEY (`voting_id`) REFERENCES `voting` (`voting_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data exporting was unselected.


-- Dumping structure for table riksdag.voting
CREATE TABLE IF NOT EXISTS `voting` (
  `voting_id` int(11) NOT NULL AUTO_INCREMENT,
  `remote_id` varchar(50) NOT NULL,
  `date` datetime NOT NULL,
  `sync_id` int(11) NOT NULL,
  PRIMARY KEY (`voting_id`),
  KEY `sync_id` (`sync_id`),
  CONSTRAINT `voting_ibfk_1` FOREIGN KEY (`sync_id`) REFERENCES `sync` (`sync_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
