CREATE TABLE IF NOT EXISTS `UserType` (
  `user_type_id` INT(1) NOT NULL AUTO_INCREMENT,
  `desc` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`user_type_id`))
ENGINE = InnoDB;