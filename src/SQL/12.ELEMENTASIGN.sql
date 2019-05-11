CREATE TABLE IF NOT EXISTS `ElementsAsign` (
  `ticket_id` INT(8) ZEROFILL NOT NULL,
  `element_id` INT(8) ZEROFILL NOT NULL,
  PRIMARY KEY (`ticket_id`, `element_id`),
  CONSTRAINT `ticket`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `Ticket` (`ticket_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `element`
    FOREIGN KEY (`element_id`)
    REFERENCES `Element` (`element_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;