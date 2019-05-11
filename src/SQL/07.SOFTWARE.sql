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