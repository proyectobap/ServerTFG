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