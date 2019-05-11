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