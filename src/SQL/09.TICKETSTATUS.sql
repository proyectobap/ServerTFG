CREATE TABLE IF NOT EXISTS `TicketStatus` (
  `ticket_status_id` INT(1) NOT NULL AUTO_INCREMENT,
  `desc` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`ticket_status_id`))
ENGINE = InnoDB;