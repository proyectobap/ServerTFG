CREATE TABLE IF NOT EXISTS `EventType` (
  `event_type_id` INT(1) ZEROFILL NOT NULL AUTO_INCREMENT,
  `desc` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`event_type_id`)
  )
ENGINE = InnoDB;