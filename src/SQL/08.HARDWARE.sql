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