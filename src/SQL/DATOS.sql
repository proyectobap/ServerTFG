INSERT INTO `produccion_db`.`UserType` (`desc`) VALUES ('Usuario sin login');
INSERT INTO `produccion_db`.`UserType` (`desc`) VALUES ('Usuario con login');
INSERT INTO `produccion_db`.`UserType` (`desc`) VALUES ('Técnico');
INSERT INTO `produccion_db`.`UserType` (`desc`) VALUES ('Supervisor');
INSERT INTO `produccion_db`.`UserType` (`desc`) VALUES ('Administrador');

INSERT INTO `produccion_db`.`ElementType` (`desc`) VALUES ('Hardware');
INSERT INTO `produccion_db`.`ElementType` (`desc`) VALUES ('Software');
INSERT INTO `produccion_db`.`ElementType` (`desc`) VALUES ('Borrado');

INSERT INTO `produccion_db`.`EventType` (`desc`) VALUES ('Seguimiento');
INSERT INTO `produccion_db`.`EventType` (`desc`) VALUES ('Tarea');
INSERT INTO `produccion_db`.`EventType` (`desc`) VALUES ('Documento');
INSERT INTO `produccion_db`.`EventType` (`desc`) VALUES ('Solución');

INSERT INTO `produccion_db`.`TicketStatus` (`desc`) VALUES ('Abierto');
INSERT INTO `produccion_db`.`TicketStatus` (`desc`) VALUES ('Asignado');
INSERT INTO `produccion_db`.`TicketStatus` (`desc`) VALUES ('En espera (Terceros)');
INSERT INTO `produccion_db`.`TicketStatus` (`desc`) VALUES ('En espera (Cliente)');
INSERT INTO `produccion_db`.`TicketStatus` (`desc`) VALUES ('Solucionado');
INSERT INTO `produccion_db`.`TicketStatus` (`desc`) VALUES ('Cerrado');

INSERT INTO `produccion_db`.`User` (`email`, `name`, `last_name`, `user_type`) VALUES ('example@example.com', 'admin', 'super', '5');
INSERT INTO `produccion_db`.`Login` (`login_name`, `shdw_passwd`, `user_id`) VALUES ('admin', 'pasword', '00000001');