ALTER VIEW `ticket_view` AS
SELECT 
Ticket.ticket_id,
Ticket.create_time,
Ticket.mod_date,
Ticket.end_date,
Ticket.title,
Ticket.desc,
Ticket.ticket_status_id,
Ticket.ticket_owner,
CONCAT(t1.name,' ',t1.last_name) AS ticketOwner,
Ticket.ticket_object,
CONCAT(t2.name,' ',t2.last_name) AS ticketObject
FROM Ticket
INNER JOIN User t1 ON Ticket.ticket_owner = t1.user_id
INNER JOIN User t2 ON Ticket.ticket_object = t2.user_id;