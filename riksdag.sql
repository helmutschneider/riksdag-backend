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
  `document_id` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `published_at` datetime NOT NULL,
  `title` text NOT NULL COLLATE utf8mb4_unicode_ci,
  `voting_id` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `sync_id` int(11) NOT NULL,
  PRIMARY KEY (`document_id`, `voting_id`, `sync_id`),
  CONSTRAINT `FK_document_voting` FOREIGN KEY (`voting_id`, `sync_id`) REFERENCES `voting` (`voting_id`, `sync_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data exporting was unselected.


-- Dumping structure for table riksdag.person
CREATE TABLE IF NOT EXISTS `person` (
  `person_id` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `birth_year` int(11) NOT NULL,
  `gender` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `first_name` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `last_name` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `party` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `status` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `sync_id` int(11) NOT NULL,
  PRIMARY KEY (`person_id`, `sync_id`),
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
  `value` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `person_id` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `voting_id` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `regarding` varchar(255) NOT NULL,
  `sync_id` int(11) NOT NULL,
  PRIMARY KEY (`person_id`, `voting_id`, `sync_id`),
  CONSTRAINT `vote_ibfk_1` FOREIGN KEY (`voting_id`, `sync_id`) REFERENCES `voting` (`voting_id`, `sync_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `vote_ibfk_2` FOREIGN KEY (`person_id`, `sync_id`) REFERENCES `person` (`person_id`, `sync_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data exporting was unselected.


-- Dumping structure for table riksdag.voting
CREATE TABLE IF NOT EXISTS `voting` (
  `voting_id` varchar(255) NOT NULL COLLATE utf8mb4_unicode_ci,
  `date` datetime NOT NULL,
  `sync_id` int(11) NOT NULL,
  PRIMARY KEY (`voting_id`, `sync_id`),
  CONSTRAINT `voting_ibfk_1` FOREIGN KEY (`sync_id`) REFERENCES `sync` (`sync_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
