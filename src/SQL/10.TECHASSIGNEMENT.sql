CREATE TABLE IF NOT EXISTS `TechAssignement` (
  `ticket_id` INT(8) ZEROFILL NOT NULL,
  `assigned_tech` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`ticket_id`, `assigned_tech`),
  CONSTRAINT `FK9`
    FOREIGN KEY (`assigned_tech`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FK10`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `Ticket` (`ticket_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;