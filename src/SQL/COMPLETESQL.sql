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
  CONSTRAINT `type`
    FOREIGN KEY (`user_type`)
    REFERENCES `produccion_db`.`UserType` (`user_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Login` (
  `login_id` INT(8) ZEROFILL NOT NULL AUTO_INCREMENT,
  `login_name` VARCHAR(15) NOT NULL,
  `shdw_passwd` VARCHAR(45) NOT NULL,
  `photo` MEDIUMBLOB NULL,
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`login_id`, `user_id`),
  CONSTRAINT `user`
    FOREIGN KEY (`user_id`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Token` (
  `login_id` INT(8) ZEROFILL NOT NULL,
  `token_hash` VARCHAR(32) NOT NULL,
  `last_petition` TIMESTAMP NOT NULL,
  `active` BIT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`login_id`),
  CONSTRAINT `login`
    FOREIGN KEY (`login_id`)
    REFERENCES `Login` (`login_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `ElementType` (
  `element_type_id` INT(1) NOT NULL AUTO_INCREMENT,
  `desc` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`element_type_id`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Element` (
  `element_id` INT(8) ZEROFILL NOT NULL,
  `internal_name` VARCHAR(45) NOT NULL,
  `element_type` INT(1) NOT NULL,
  PRIMARY KEY (`element_id`),
  CONSTRAINT `type_id`
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
  CONSTRAINT `software`
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
  CONSTRAINT `hardware`
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
  `start_date` TIMESTAMP NOT NULL,
  `end_date` TIMESTAMP NULL,
  `mod_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` LONGTEXT NOT NULL,
  `ticket_status_id` INT(1) NOT NULL,
  `ticket_owner` INT(8) ZEROFILL NOT NULL,
  `ticket_object` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`ticket_id`),
  CONSTRAINT `ticket_status`
    FOREIGN KEY (`ticket_status_id`)
    REFERENCES `TicketStatus` (`ticket_status_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ticket_owner`
    FOREIGN KEY (`ticket_owner`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `client`
    FOREIGN KEY (`ticket_object`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `TechAssignement` (
  `ticket_id` INT(8) ZEROFILL NOT NULL,
  `assigned_tech` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`ticket_id`, `assigned_tech`),
  CONSTRAINT `tech`
    FOREIGN KEY (`assigned_tech`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ticketid`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `Ticket` (`ticket_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `ElementsAsign` (
  `ticket_id` INT(8) ZEROFILL NOT NULL,
  `element_id` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`ticket_id`, `element_id`),
  CONSTRAINT `ticket`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `Ticket` (`ticket_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `element`
    FOREIGN KEY (`element_id`)
    REFERENCES `Element` (`element_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `EventType` (
  `event_type_id` INT(1) ZEROFILL NOT NULL AUTO_INCREMENT,
  `desc` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`event_type_id`)
  )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Event` (
  `event_id` INT(8) ZEROFILL NOT NULL,
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ticket_id` INT(8) ZEROFILL NOT NULL,
  `event_type` INT(1) UNSIGNED NOT NULL,
  PRIMARY KEY (`event_id`),
  CONSTRAINT `FK1`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `Ticket` (`ticket_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `FK2`
    FOREIGN KEY (`event_type`)
    REFERENCES `EventType` (`event_type_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS `Task` (
  `event_id` INT(8) ZEROFILL NOT NULL,
  `desc` VARCHAR(512) NOT NULL,
  `time` INT(7) NOT NULL DEFAULT 0,
  `isDone` BIT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`event_id`),
  CONSTRAINT `FK3`
    FOREIGN KEY (`event_id`)
    REFERENCES `Event` (`event_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Document` (
  `event_id` INT(8) ZEROFILL NOT NULL,
  `document` LONGBLOB NOT NULL,
  PRIMARY KEY (`event_id`),
  CONSTRAINT `FK4`
    FOREIGN KEY (`event_id`)
    REFERENCES `Event` (`event_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Comment` (
  `event_id` INT(8) ZEROFILL NOT NULL,
  `desc` VARCHAR(512) NOT NULL,
  PRIMARY KEY (`event_id`),
  CONSTRAINT `FK5`
    FOREIGN KEY (`event_id`)
    REFERENCES `Event` (`event_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Solution` (
  `event_id` INT(8) ZEROFILL NOT NULL,
  `desc` VARCHAR(512) NOT NULL,
  PRIMARY KEY (`event_id`),
  CONSTRAINT `FK6`
    FOREIGN KEY (`event_id`)
    REFERENCES `Event` (`event_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;