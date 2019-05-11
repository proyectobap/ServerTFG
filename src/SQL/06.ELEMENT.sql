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