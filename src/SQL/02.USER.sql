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