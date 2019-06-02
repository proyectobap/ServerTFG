CREATE TABLE IF NOT EXISTS `Login` (
  `login_id` INT(8) ZEROFILL NOT NULL AUTO_INCREMENT,
  `login_name` VARCHAR(15) NOT NULL,
  `shdw_passwd` VARCHAR(45) NOT NULL,
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`login_id`, `user_id`),
  CONSTRAINT `user`
    FOREIGN KEY (`user_id`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;