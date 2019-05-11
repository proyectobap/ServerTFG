CREATE TABLE IF NOT EXISTS `Ticket` (
  `ticket_id` INT(8) ZEROFILL NOT NULL AUTO_INCREMENT,
  `start_date` TIMESTAMP NOT NULL,
  `end_date` TIMESTAMP NULL,
  `mod_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` LONGTEXT NOT NULL,
  `ticket_status_id` INT(1) NOT NULL,
  `ticket_owner` INT(8) ZEROFILL NOT NULL,
  `ticket_object` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`ticket_id`),
  CONSTRAINT `ticket_status`
    FOREIGN KEY (`ticket_status_id`)
    REFERENCES `TicketStatus` (`ticket_status_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ticket_owner`
    FOREIGN KEY (`ticket_owner`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `client`
    FOREIGN KEY (`ticket_object`)
    REFERENCES `User` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;