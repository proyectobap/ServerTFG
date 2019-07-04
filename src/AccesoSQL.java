import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class AccesoSQL {
	
	/*
	 * Clase encargada de gestionar las peticiones con la base de datos
	 */
	
	// Credenciales de acceso a la base de datos
	private static final String SURL = "jdbc:mysql://localhost/produccion_db";
    private static final String USU = "pedro";
    private static final String PASS = "oxgnub";
    
    // Este será el contenido de las respuestas del servidor al cliente
    private JSONArray content;
    // En esta referencia estará el socket con el cliente, para poder acceder a sus propiedades
    private ClientListener cli = null;
    
    // Objetos necesarios para el acceso a métodos SQL
    private Connection con = null;
    private PreparedStatement ps = null;
    private ResultSet rs = null;
    
    /*************************************************************************************/
    
    // A la hora de crear la conexión, se le pasará el socket con el cliente
    public AccesoSQL(ClientListener cli) {
    	this.cli = cli;
    }
    
    /*************************************************************************************/
    
    // Método para conectarse a la base de datos
    public boolean connect() throws SQLException {
    	
    	con = DriverManager.getConnection(SURL, USU, PASS);
    	// Devuelve verdadero si en el momento de la comprobación, la conexión no está cerrada
    	return !con.isClosed();
    	
    }
    
    /*************************************************************************************/
    
    // Cierra la conexión. Comprueba también si queda algun objeto abierto, y lo cierra
    public boolean closeConnection() throws SQLException {
    	
    	if (rs != null) {
    		rs.close();
    	}
    	
    	if (ps != null) {
    		ps.close();
    	}
    	
    	
    	con.close();
    	return con.isClosed();
    	
    }
    
    /*************************************************************************************/
    
    // Controla si los cambios se hacen instantaneamente
    public void setAutoCommit(boolean cond) throws SQLException {
    	con.setAutoCommit(cond);
    }
    
    /*************************************************************************************/
    
    // Revierte cambios, solo si el autocommit está desactivado
    public void rollback() throws SQLException {
    	con.rollback();
    }
    
    /*************************************************************************************/
    
    // Respuesta por defecto ante un comando no encontrado
    public JSONObject help() {
    	return JsonTreatment.sendResponseCode(404, "comando no encontrado");
    }
    
    /*************************************************************************************/
        
    // Consulta la tabla de Login para ver si las credenciales del usuario son correctas
    public JSONObject login(String[] credentials) throws SQLException {

    	content = new JSONArray();
    	String query = "SELECT * FROM Login WHERE login_name LIKE ?";
    	
    	ps = con.prepareStatement(query);
    	ps.setString(1, credentials[0]);
    	
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		
    		// Si encuentra coincidencia...
    		if (credentials[1].equals(rs.getString(3))) {

    			// Almacena la información del usuario para devolverla con la respuesta
    			int userId = rs.getInt(5);
    			int loginId = rs.getInt(1);
    			
    			// Con esta petición se busca devolver al usuario su nivel de permisos, para gestionar por el cliente
    			query = "SELECT user_type FROM User WHERE user_id = ?";
    			
    			// Se almacena información del usuario y se asocia a su socket
    			cli.setLoginId(loginId);
    			cli.setUserId(userId);
    			
    			ps = con.prepareStatement(query);
    			ps.setInt(1, userId);
    			
    			ResultSet resultSet = ps.executeQuery();
    			resultSet.next();
    			int permissionsId = resultSet.getInt(1);
    			resultSet.close();

    			// Devuelve la respuesta del login al cliente (Si el login es correcto)
    			return JsonTreatment.sendLoginCode(200, permissionsId, userId, loginId, "ok");
    			
    		}
    	}
    	
    	// Si el login es incorrecto devuelve un mensaje de error
    	content.put(new JSONObject().put("content", "Ningún usuario coincide con esas credenciales"));
    	return JsonTreatment.sendResponseCode(400, content);
    }
    
    /*************************************************************************************/
    
    // Este método devuelve la información de las tablas relativa a tipos y estados
    public JSONObject list(int x) throws SQLException{
    	
    	content = new JSONArray();
    	String query = null;
    	
    	// Para concentrar todas las posibilidades y debido a la similitud de la salida de todas las
    	// peticiones, se hace uso de un switch
    	switch (x) {
    	
    	case 0:
    		query = "SELECT * FROM ElementType";
    		break;
    	case 1:
    		query = "SELECT * FROM EventType";
    		break;
    	case 2:
    		query = "SELECT * FROM TicketStatus";
    		break;
    	case 3:
    		query = "SELECT * FROM UserType";
    		break;
    	
    	}
    	
    	ps = con.prepareStatement(query);
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		JSONObject responseB = new JSONObject();
    		responseB.put("id", rs.getInt(1));
    		responseB.put("desc", rs.getString(2));
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    	
    }
    
    /*************************************************************************************/
    
    // Método para dar la lista de tickets
    public JSONObject listarTickets() throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT * FROM Ticket";
    	ps = con.prepareStatement(query);
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		JSONObject responseB = new JSONObject();
    		responseB.put("ticket_id", rs.getInt(1));
    		responseB.put("create_time", rs.getDate(2));
    		responseB.put("mod_date", rs.getDate(3));
    		responseB.put("end_date", rs.getDate(4));
    		responseB.put("title", rs.getString(5));
    		responseB.put("desc", rs.getString(6));
    		responseB.put("ticket_status_id", rs.getInt(7));
    		responseB.put("ticket_owner", rs.getInt(8));
    		responseB.put("ticket_object", rs.getInt(9));
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    // Método para devolver la información relativa a un solo ticket, indicando como parámetro de entrada su id
    public JSONObject cogerTicket(JSONObject json) throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT * FROM ticket_view WHERE ticket_id = "+json.getInt("ticket_id");
    	ps = con.prepareStatement(query);
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		JSONObject responseB = new JSONObject();
    		responseB.put("ticket_id", rs.getInt(1));
    		responseB.put("create_time", rs.getDate(2));
    		responseB.put("mod_date", rs.getDate(3));
    		responseB.put("end_date", rs.getDate(4));
    		responseB.put("title", rs.getString(5));
    		responseB.put("desc", rs.getString(6));
    		responseB.put("ticket_status_id", rs.getInt(7));
    		responseB.put("ticket_owner", rs.getInt(8));
    		responseB.put("ticketOwner", rs.getString(9));
    		responseB.put("ticket_object", rs.getInt(10));
    		responseB.put("ticketObject", rs.getString(11));
    		if (responseB.getInt("ticket_id") == 0) {
    			break;
    		}
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    // Permite consultar la lista de tickets con filtros SQL.
    public JSONObject listarTicketsFiltro(JSONObject json) throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT * FROM Ticket "+json.getString("filter");
    	ps = con.prepareStatement(query);
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		JSONObject responseB = new JSONObject();
    		responseB.put("ticket_id", rs.getInt(1));
    		responseB.put("create_time", rs.getDate(2));
    		responseB.put("mod_date", rs.getDate(3));
    		responseB.put("end_date", rs.getDate(4));
    		responseB.put("title", rs.getString(5));
    		responseB.put("desc", rs.getString(6));
    		responseB.put("ticket_status_id", rs.getInt(7));
    		responseB.put("ticket_owner", rs.getInt(8));
    		responseB.put("ticket_object", rs.getInt(9));
    		// Se salta el ticket con id 0, ya que está predefinido para fines internos.
    		if (responseB.getInt("ticket_id") == 0) {
    			break;
    		}
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    // Devuelve la lista de usuarios
    public JSONObject userList() throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT * FROM User";
    	ps = con.prepareStatement(query);
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		
    		JSONObject responseB = new JSONObject();
    		responseB.put("user_id", rs.getInt(1));
    		responseB.put("email", rs.getString(2));
    		responseB.put("name", rs.getString(3));
    		responseB.put("last_name", rs.getString(4));
    		responseB.put("user_type", rs.getInt(5));
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    // Crea un ticket
    public JSONObject newTicket(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO Ticket (`title`, `desc`, `ticket_status_id`, `ticket_owner`, `ticket_object`) VALUES (?,?,?,?,?);";
    	
    	// Con el segundo valor, estamos indicando que recoja la id creada con la petición
    	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
    	ps.setString(1, batch.getString("title"));
    	ps.setString(2, batch.getString("desc"));
        ps.setInt(3, batch.getInt("ticket_status_id"));
        ps.setInt(4, batch.getInt("ticket_owner"));
        ps.setInt(5, batch.getInt("ticket_object"));

        // almacena la cantidad de registros afectados
    	int result = ps.executeUpdate();
    	
    	// A tenor del número de registros afectados calcula si la petición ha sido existosa
    	if (result == 1) {
    		// Almacenamos el ResultSet de la id generada...
    		ResultSet res = ps.getGeneratedKeys();
    		res.next();
    		System.out.println("Se ha creado el ticket número "+res.getInt(1));
    		// ... y la enviamos
    		return JsonTreatment.sendResponseCode(200, res.getInt(1), "Se han añadido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Crea un usuario y devuelve la id
    public JSONObject newUser(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO User (`email`, `name`, `last_name`, `user_type`) VALUES (?,?,?,?)";
    	
    	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
    	ps.setString(1, batch.getString("email"));
    	ps.setString(2, batch.getString("name"));
        ps.setString(3, batch.getString("lastname"));
        ps.setInt(4, batch.getInt("user_type"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		ResultSet res = ps.getGeneratedKeys();
    		res.next();
    		System.out.println("Se ha creado el usuario número "+res.getInt(1));
    		return JsonTreatment.sendResponseCode(200, res.getInt(1), "Se han añadido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Modifica la contraseña del usuario logeado. Esta información se obtuvo en el login.
    public JSONObject modifyOwnUserPassword(JSONObject batch, int userId) throws SQLException {

    	String query = "UPDATE Login SET shdw_passwd = ? WHERE  login_id = "+ userId;
    	
    	ps = con.prepareStatement(query);
    	
    	ps.setString(1, batch.getString("shdw_passwd"));

    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Modifica la contraseña de un usuario seleccionado
    public JSONObject modifyUserPassword(JSONObject batch) throws SQLException {

    	String query = "UPDATE Login SET shdw_passwd = ? WHERE  user_id = ?";
    	
    	ps = con.prepareStatement(query);
    	
    	ps.setString(1, batch.getString("shdw_passwd"));
    	ps.setInt(2, batch.getInt("user_id"));

    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Modifica la información del usuario logeado
    public JSONObject modifyOwnUser(JSONObject batch, int userId) throws SQLException {

    	String query = 
    		"UPDATE User SET `email` = ?, `name` = ?, `last_name` = ?, `user_type` = ? WHERE `user_id` = " + userId;
    	
    	ps = con.prepareStatement(query);

    	ps.setString(1, batch.getString("email"));
    	ps.setString(2, batch.getString("name"));
        ps.setString(3, batch.getString("last_name"));
        ps.setInt(4, batch.getInt("user_type"));

    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Modifica la información de un usuario seleccionado
    public JSONObject modifyUser(JSONObject batch) throws SQLException {

    	String query = 
    			"UPDATE User SET `email` = ?, `name` = ?, `last_name` = ?, `user_type` = ? WHERE user_id = ?";
    	
    	ps = con.prepareStatement(query);

    	ps.setString(1, batch.getString("email"));
    	ps.setString(2, batch.getString("name"));
        ps.setString(3, batch.getString("last_name"));
        ps.setInt(4, batch.getInt("user_type"));
        ps.setInt(5, batch.getInt("user_id"));

    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Crea un login asociado a un usuario
    public JSONObject newLogin(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO Login (`login_name`, `shdw_passwd`, `user_id`) VALUES (?,?,?)";
    	
    	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
    	ps.setString(1, batch.getString("login_name"));
    	ps.setString(2, batch.getString("shdw_passwd"));
        ps.setInt(3, batch.getInt("user_id"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		ResultSet res = ps.getGeneratedKeys();
    		res.next();
    		System.out.println("Se ha creado el Login número "+res.getInt(1));
    		return JsonTreatment.sendResponseCode(200, res.getInt(1), "Se han añadido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    // 
    
    /*************************************************************************************/
    
    // Crea un evento
    public JSONObject newEvent(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO Event (`event_desc`, `ticket_id`, `event_type`) VALUES (?,?,?)";
    	
    	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
    	ps.setString(1, batch.getString("event_desc"));
    	ps.setInt(2, batch.getInt("ticket_id"));
        ps.setInt(3, batch.getInt("event_type"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		ResultSet res = ps.getGeneratedKeys();
    		res.next();
    		System.out.println("Se ha creado el evento número "+res.getInt(1));
    		return JsonTreatment.sendResponseCode(200, res.getInt(1), "Se han añadido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Crea una nueva tarea
    public JSONObject newTask(JSONObject batch) throws SQLException {
    			
    	// Como la tarea es en realidad un evento con información adicional, se van a hacer
    	// dos peticiones, y por si falla algo en el proceso, se deshabilita el autocommit
    	// para poder revertir los cambios en caso de fallo 
    	setAutoCommit(false);
    	
    	String query = "INSERT INTO Event (`event_desc`, `ticket_id`, `event_type`) VALUES (?, ?, 2)";
    	
    	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
    	ps.setString(1, batch.getString("event_desc"));
    	ps.setInt(2, batch.getInt("ticket_id"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		
    		ResultSet res = ps.getGeneratedKeys();
    		res.next();
    		int event = res.getInt(1);
    		System.out.println("Se ha creado el evento número "+ event );
    		
    		// Segunda petición
    		query = "INSERT INTO Task (`event_id`, `time`, `is_done`) VALUES ("+ event +", ?, ?)";
        	
        	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        	
        	ps.setInt(1, batch.getInt("time"));
            ps.setBoolean(2, batch.getBoolean("is_done"));

        	result = ps.executeUpdate();
        	
        	if (result == 1) {
        		con.commit();
        		return JsonTreatment.sendResponseCode(200, event, "Se han añadido "+ result +" lineas a la base de datos");
        	} else {
        		con.rollback();
        		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
        	}
    		
    	} else {
    		
    		// En caso de fallo, se hace un rollback y se revierten los cambios
    		con.rollback();
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Devuelve la lista de eventos relativos a un ticket determinado
    public JSONObject eventList(JSONObject batch) throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT * FROM Event WHERE ticket_id = ?";
    	
    	ps = con.prepareStatement(query);
    	ps.setInt(1, batch.getInt("ticket_id"));
    	
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		
    		int tipoEvento = rs.getInt(6);
    		int noEvento = rs.getInt(1);
    		
    		JSONObject responseB = new JSONObject();
    		responseB.put("event_id", rs.getInt(1));
    		responseB.put("create_time", rs.getDate(2));
    		responseB.put("mod_date", rs.getDate(3));
    		responseB.put("event_desc", rs.getString(4));
    		responseB.put("event_type", tipoEvento);
    		
    		// Si el evento es una tarea, devuelve también la información adicional
    		if (tipoEvento == 2) {
    			
    			String queryAlt = "SELECT * FROM Task WHERE event_id = " + noEvento;
    	    	
    	    	ps = con.prepareStatement(queryAlt);
    	    	
    	    	ResultSet resultSet = ps.executeQuery();
    	    	
    	    	resultSet.next();
    	    	
    	    	responseB.put("time", resultSet.getInt(2));
    	    	responseB.put("is_done", resultSet.getBoolean(3));
    	    	
    		}
    		
    		content.put(responseB);
    		
    	}
    	
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    // Modifica un evento dada su id
    public JSONObject modifyEvent(JSONObject batch) throws SQLException {

    	String query = 
    		"UPDATE Event SET event_desc= ?, ticket_id= ?, event_type= ? WHERE event_id = ?";
    	
    	ps = con.prepareStatement(query);

    	ps.setString(1, batch.getString("event_desc"));
    	ps.setInt(2, batch.getInt("ticket_id"));
        ps.setInt(3, batch.getInt("event_type"));
        ps.setInt(4, batch.getInt("event_id"));

    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Modifica la información de una tarea, relativa a su id de evento
    public JSONObject modifyTask(JSONObject batch) throws SQLException {

    	String query = 
    		"UPDATE Task SET time= ?, is_done= ? WHERE event_id = ?";
    	
    	ps = con.prepareStatement(query);

    	ps.setInt(1, batch.getInt("time"));
    	ps.setBoolean(2, batch.getBoolean("is_done"));
        ps.setInt(3, batch.getInt("event_id"));

    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Modifica el ticket dada su id
    public JSONObject modifyTicket(JSONObject batch) throws SQLException {

    	String query = 
    		"UPDATE `Ticket` SET `title` = ?, `desc` = ?, `ticket_status_id` = ?, `ticket_owner` = ?, `ticket_object` = ? WHERE `ticket_id` = ?";
    	
    	ps = con.prepareStatement(query);

    	ps.setString(1, batch.getString("title"));
    	ps.setString(2, batch.getString("desc"));
        ps.setInt(3, batch.getInt("ticket_status_id"));
        ps.setInt(4, batch.getInt("ticket_owner"));
        ps.setInt(5, batch.getInt("ticket_object"));
        ps.setInt(6, batch.getInt("ticket_id"));

    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Cambia el estado del ticket a solucionado (5) y le aplica una fecha de finalización
    public JSONObject solveTicket(JSONObject batch) throws SQLException {

    	String query = 
    		"UPDATE Ticket SET ticket_status_id = 5, end_date = NOW()  WHERE ticket_id = ?";
    	
    	ps = con.prepareStatement(query);
    	
        ps.setInt(1, batch.getInt("ticket_id"));

    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Crea un elemento hardware
    public JSONObject newHardware(JSONObject batch) throws SQLException {
    	
    	// Al ser dos peticiones en una, ya que hardware es un elemento con propiedades específicas
    	// se deshabilita el autocommit
    	setAutoCommit(false);

    	String query = "INSERT INTO Element (`internal_name`, `element_type`) VALUES (?, 1)";
    	
    	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
    	ps.setString(1, batch.getString("internal_name"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		
    		ResultSet res = ps.getGeneratedKeys();
    		res.next();
    		int element = res.getInt(1);
    		System.out.println("Se ha creado el elemento número "+ element);
    		
    		query = "INSERT INTO Hardware (`element_id`, `S/N`, `brand`, `model`) VALUES ("+ element +", ?, ?, ?)";
        	
        	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        	
        	ps.setString(1, batch.getString("S/N"));
        	ps.setString(2, batch.getString("brand"));
        	ps.setString(3, batch.getString("model"));

        	result = ps.executeUpdate();
        	
        	if (result == 1) {
        		con.commit();
        		return JsonTreatment.sendResponseCode(200, element, "Se han añadido "+ result +" lineas a la base de datos");
        	} else {
        		con.rollback();
        		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
        	}
    		
    	} else {
    		con.rollback();
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Crea un elemento software
    public JSONObject newSoftware(JSONObject batch) throws SQLException {
    	
    	setAutoCommit(false);

    	String query = "INSERT INTO Element (`internal_name`, `element_type`) VALUES (?, 2)";
    	
    	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
    	ps.setString(1, batch.getString("internal_name"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		
    		ResultSet res = ps.getGeneratedKeys();
    		res.next();
    		int element = res.getInt(1);
    		System.out.println("Se ha creado el elemento número "+ element);
    		
    		query = "INSERT INTO Software (`element_id`, `developer`, `version`) VALUES ("+ element +", ?, ?)";
        	
        	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        	
        	ps.setString(1, batch.getString("developer"));
        	ps.setString(2, batch.getString("version"));

        	result = ps.executeUpdate();
        	
        	if (result == 1) {
        		con.commit();
        		return JsonTreatment.sendResponseCode(200, element, "Se han añadido "+ result +" lineas a la base de datos");
        	} else {
        		con.rollback();
        		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
        	}
    		
    	} else {
    		con.rollback();
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Devuelve la lista de elementos
    public JSONObject elementList() throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT * FROM Element";
    	ps = con.prepareStatement(query);
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		
    		JSONObject responseB = new JSONObject();
    		responseB.put("element_id", rs.getInt(1));
    		responseB.put("internal_name", rs.getString(2));
    		responseB.put("element_type", rs.getInt(3));
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    // Modifica el elemento con la id dada
    public JSONObject modifyElement(JSONObject batch) throws SQLException {

    	String query = 
    		"UPDATE Element SET internal_name = ?, element_type = ? WHERE element_id = ?";
    	
    	ps = con.prepareStatement(query);

    	ps.setString(1, batch.getString("internal_name"));
    	ps.setString(2, batch.getString("element_type"));
        ps.setInt(3, batch.getInt("element_id"));
        
    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // modifica un hardware dada la id de elemento
    public JSONObject modifyHardware(JSONObject batch) throws SQLException {

    	String query = 
    		"UPDATE Hardware SET S/N = ?, brand = ?, model = ? WHERE element_id = ?";
    	
    	ps = con.prepareStatement(query);

    	ps.setString(1, batch.getString("S/N"));
    	ps.setString(2, batch.getString("brand"));
    	ps.setString(3, batch.getString("model"));
    	ps.setInt(4, batch.getInt("element_id"));
        
    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // modifica un software dada la id de elemento
    public JSONObject modifySoftware(JSONObject batch) throws SQLException {

    	String query = 
    		"UPDATE Software SET developer = ?, version = ? WHERE element_id = ?";
    	
    	ps = con.prepareStatement(query);

    	ps.setString(1, batch.getString("developer"));
    	ps.setString(2, batch.getString("version"));
    	ps.setInt(4, batch.getInt("element_id"));
        
    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Asigna un elemento a un ticket
    public JSONObject assignElement(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO ElementsAsign (`ticket_id`, `element_id`) VALUES (?, ?)";
    	
    	ps = con.prepareStatement(query);
    	
    	ps.setInt(1, batch.getInt("ticket_id"));
    	ps.setInt(2, batch.getInt("element_id"));

    	int result = ps.executeUpdate();
        	
    	if (result == 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han añadido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Asigna un técnico a un ticket
    public JSONObject assignTech(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO TechAssignement (`ticket_id`, `assigned_tech`) VALUES (?, ?)";
    	
    	ps = con.prepareStatement(query);
    	
    	ps.setInt(1, batch.getInt("ticket_id"));
    	ps.setInt(2, batch.getInt("assigned_tech"));

    	int result = ps.executeUpdate();
        	
    	if (result == 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han añadido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Elimina una asignación de elemento
    public JSONObject deleteAssignedElement(JSONObject batch) throws SQLException {

    	String query = "DELETE FROM ElementsAsign WHERE  ticket_id = ? AND element_id = ?";
    	
    	ps = con.prepareStatement(query);
    	
    	ps.setInt(1, batch.getInt("ticket_id"));
    	ps.setInt(2, batch.getInt("element_id"));

    	int result = ps.executeUpdate();
        	
    	if (result == 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han eliminado "+ result +" lineas de la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han eliminado "+ result +" lineas de la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Elimina una asignación de técnico    
    public JSONObject deleteAssignedTech(JSONObject batch) throws SQLException {

    	String query = "DELETE FROM TechAssignement WHERE  ticket_id = ? AND assigned_tech = ?";
    	
    	ps = con.prepareStatement(query);
    	
    	ps.setInt(1, batch.getInt("ticket_id"));
    	ps.setInt(2, batch.getInt("assigned_tech"));

    	int result = ps.executeUpdate();
        	
    	if (result == 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han eliminado "+ result +" lineas de la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han eliminado "+ result +" lineas de la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    // Devuelve en detalle un elemento
    public JSONObject elementDetail(JSONObject batch) throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT * FROM Element WHERE element_id = ?";
    	ps = con.prepareStatement(query);
    	ps.setInt(1, batch.getInt("element_id"));
    	rs = ps.executeQuery();
    	
    	rs.next();
    		
		JSONObject responseB = new JSONObject();
		responseB.put("element_id", rs.getInt(1));
		responseB.put("internal_name", rs.getString(2));
		responseB.put("element_type", rs.getInt(3));
		
		// Ya sea hardware
		if (rs.getInt(3) == 1) {
			
			query = "SELECT * FROM Hardware WHERE element_id = ?";
			ps = con.prepareStatement(query);
			ps.setInt(1, batch.getInt("element_id"));
			rs = ps.executeQuery();
			rs.next();
			
			responseB.put("S/N", rs.getString(2));
			responseB.put("brand", rs.getString(3));
			responseB.put("model", rs.getString(4));
			
			
		// o software	
		} else if (rs.getInt(3) == 2){
			
			query = "SELECT * FROM Software WHERE element_id = ?";
			ps = con.prepareStatement(query);
			ps.setInt(1, batch.getInt("element_id"));
			rs = ps.executeQuery();
			rs.next();
			
			responseB.put("developer", rs.getString(2));
			responseB.put("version", rs.getString(3));
			
		}
		
		content.put(responseB);

    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    // Devuelve la relación de técnicos con un numero de ticket dado
    public JSONObject techRelation(JSONObject batch) throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT assigned_tech FROM TechAssignement WHERE ticket_id = ?";
    	ps = con.prepareStatement(query);
    	ps.setInt(1, batch.getInt("ticket_id"));
    	
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		
    		JSONObject responseB = new JSONObject();
    		responseB.put("assigned_tech", rs.getInt(1));
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    // Devuelve la relación de elementos con un numero de ticket dado
    public JSONObject elementRelation(JSONObject batch) throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT element_id FROM ElementsAsign WHERE ticket_id = ?";
    	ps = con.prepareStatement(query);
    	ps.setInt(1, batch.getInt("ticket_id"));
    	
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		
    		JSONObject responseB = new JSONObject();
    		responseB.put("element_id", rs.getInt(1));
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
}
