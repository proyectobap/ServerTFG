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