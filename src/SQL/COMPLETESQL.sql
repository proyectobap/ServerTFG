CREATE SCHEMA IF NOT EXISTS `produccion_db` DEFAULT CHARACTER SET utf8 ;
USE `produccion_db` ;

CREATE TABLE IF NOT EXISTS `UserType` (
  `user_type_id` INT(1) NOT NULL AUTO_INCREMENT,
  `desc` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`user_type_id`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `User` (
  `user_id` INT(8) ZEROFILL NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NULL,
  `name` VARCHAR(45) NOT NULL,
  `last_name` VARCHAR(45) NULL,
  `user_type` INT(1) NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `FK1`
    FOREIGN KEY (`user_type`)
    REFERENCES `UserType` (`user_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Login` (
  `login_id` INT(8) ZEROFILL NOT NULL AUTO_INCREMENT,
  `login_name` VARCHAR(15) NOT NULL UNIQUE,
  `shdw_passwd` VARCHAR(45) NOT NULL,
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`login_id`, `user_id`),
  CONSTRAINT `FK2`
    FOREIGN KEY (`user_id`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `ElementType` (
  `element_type_id` INT(1) NOT NULL AUTO_INCREMENT,
  `desc` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`element_type_id`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Element` (
  `element_id` INT(8) ZEROFILL NOT NULL AUTO_INCREMENT,
  `internal_name` VARCHAR(45) NOT NULL,
  `element_type` INT(1) NOT NULL,
  PRIMARY KEY (`element_id`),
  CONSTRAINT `FK3`
    FOREIGN KEY (`element_type`)
    REFERENCES `ElementType` (`element_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Software` (
  `element_id` INT(8) ZEROFILL NOT NULL,
  `developer` VARCHAR(45) NULL,
  `version` VARCHAR(45) NULL,
  PRIMARY KEY (`element_id`),
  CONSTRAINT `FK4`
    FOREIGN KEY (`element_id`)
    REFERENCES `Element` (`element_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Hardware` (
  `element_id` INT(8) ZEROFILL NOT NULL,
  `S/N` VARCHAR(45) NULL,
  `brand` VARCHAR(45) NULL,
  `model` VARCHAR(45) NULL,
  PRIMARY KEY (`element_id`),
  CONSTRAINT `FK5`
    FOREIGN KEY (`element_id`)
    REFERENCES `Element` (`element_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `TicketStatus` (
  `ticket_status_id` INT(1) NOT NULL AUTO_INCREMENT,
  `desc` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`ticket_status_id`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Ticket` (
  `ticket_id` INT(8) ZEROFILL NOT NULL AUTO_INCREMENT,
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `mod_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL,
  `title` VARCHAR(200) NOT NULL,
  `desc` LONGTEXT NOT NULL,
  `ticket_status_id` INT(1) NOT NULL,
  `ticket_owner` INT(8) ZEROFILL NOT NULL,
  `ticket_object` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`ticket_id`),
  CONSTRAINT `FK6`
    FOREIGN KEY (`ticket_status_id`)
    REFERENCES `TicketStatus` (`ticket_status_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK7`
    FOREIGN KEY (`ticket_owner`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK8`
    FOREIGN KEY (`ticket_object`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `TechAssignement` (
  `ticket_id` INT(8) ZEROFILL NOT NULL,
  `assigned_tech` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`ticket_id`, `assigned_tech`),
  CONSTRAINT `FK9`
    FOREIGN KEY (`assigned_tech`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK10`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `Ticket` (`ticket_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `ElementsAsign` (
  `ticket_id` INT(8) ZEROFILL NOT NULL,
  `element_id` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`ticket_id`, `element_id`),
  CONSTRAINT `FK11`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `Ticket` (`ticket_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK12`
    FOREIGN KEY (`element_id`)
    REFERENCES `Element` (`element_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `EventType` (
  `event_type_id` INT(1) NOT NULL AUTO_INCREMENT,
  `desc` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`event_type_id`)
  )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Event` (
  `event_id` INT(8) ZEROFILL NOT NULL AUTO_INCREMENT,
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `mod_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `event_desc` LONGTEXT NOT NULL,
  `ticket_id` INT(8) ZEROFILL NOT NULL,
  `event_type` INT(1) NOT NULL,
  PRIMARY KEY (`event_id`),
  CONSTRAINT `FK13`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `Ticket` (`ticket_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK14`
    FOREIGN KEY (`event_type`)
    REFERENCES `EventType` (`event_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS `Task` (
  `event_id` INT(8) ZEROFILL NOT NULL,
  `time` INT(7) NOT NULL DEFAULT 0,
  `is_done` BIT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`event_id`),
  CONSTRAINT `FK15`
    FOREIGN KEY (`event_id`)
    REFERENCES `Event` (`event_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;