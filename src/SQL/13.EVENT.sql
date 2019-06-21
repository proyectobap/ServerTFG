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