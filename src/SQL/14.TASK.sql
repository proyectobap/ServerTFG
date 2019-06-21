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