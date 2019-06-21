import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class AccesoSQL {
	
	private static final String SURL = "jdbc:mysql://localhost/produccion_db";
    private static final String USU = "pedro";
    private static final String PASS = "oxgnub";
    
    private static JSONArray commands = null;
    private JSONArray content;
    private ClientListener cli = null;
    
    private Connection con;
    private PreparedStatement ps;
    private ResultSet rs;
    
    /*************************************************************************************/
    
    public AccesoSQL(ClientListener cli) {
    	this.cli = cli;
    	if (commands == null) {
    		commands = new JSONArray();
    		// Debug
    		commands.put(new JSONObject().put("help", "Muestra la lista de comandos (Debug)"));
    		commands.put(new JSONObject().put("testlogin", "muestra lista de usuarios (Debug)"));
    		// Create
    		commands.put(new JSONObject().put("newTicket (title,description,status,owner,object)", "Inserta un nuevo Ticket"));
    		commands.put(new JSONObject().put("newUser (email, name, last_name, user_type)", "Inserta un nuevo Usuario"));
    		// List
    		commands.put(new JSONObject().put("listTicket", "Muestra todos los Tickets"));
    		commands.put(new JSONObject().put("listticketstatus", "Lista los posibles estados de un ticket"));
    		commands.put(new JSONObject().put("listeventtype", "Lista los posibles tipos de eventos"));
    		commands.put(new JSONObject().put("listelementtype", "Lista los posibles tipos de elementos"));
    		commands.put(new JSONObject().put("listusertype", "Lista los posibles tipos de usuarios"));
    		// Login
    		commands.put(new JSONObject().put("login(login_name, shdw_passwd)", "Comprueba login de usuario"));
    	}
    }
    
    /*************************************************************************************/
    
    public boolean connect() throws SQLException {
    	
    	con = DriverManager.getConnection(SURL, USU, PASS);
    	return !con.isClosed();
    	
    }
    
    /*************************************************************************************/
    
    public boolean closeConnection() throws SQLException {
    	con.close();
    	if (con.isClosed()) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /*************************************************************************************/
    /*************** DEBUG ***************************************************************/
    /*************************************************************************************/
    
    public JSONObject help() {
    	
    	return JsonTreatment.sendResponseCode(404, commands);
    	
    }
    
    /*************************************************************************************/
    /*************** FINAL ***************************************************************/
    /*************************************************************************************/
    
    
    public JSONObject login(String[] credentials) throws SQLException {

    	content = new JSONArray();
    	String query = "SELECT * FROM Login WHERE login_name LIKE ?";
    	
    	ps = con.prepareStatement(query);
    	ps.setString(1, credentials[0]);
    	
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		
    		if (credentials[1].equals(rs.getString(3))) {
    			
    			cli.setLoginId(rs.getInt(1));
    			System.out.println(rs.getInt(1));
    			return JsonTreatment.sendResponseCode(200, "ok");
    			
    		}
    	}
    	content.put(new JSONObject().put("content", "Ning�n usuario coincide con esas credenciales"));
    	return JsonTreatment.sendResponseCode(400, content);
    }
    
    /*************************************************************************************/
    
    public JSONObject list(int x) throws SQLException{
    	
    	content = new JSONArray();
    	String query = null;
    	
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
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
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
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    public JSONObject userList() throws SQLException {

    	content = new JSONArray();
    	
    	String query = "SELECT * FROM User";
    	ps = con.prepareStatement(query);
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		
    		JSONObject responseB = new JSONObject();
    		responseB.put("id", rs.getInt(1));
    		responseB.put("email", rs.getString(2));
    		responseB.put("name", rs.getString(3));
    		responseB.put("last_name", rs.getString(4));
    		responseB.put("user_type", rs.getInt(5));
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    public JSONObject newTicket(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO Ticket (`title`, `desc`, `ticket_status_id`, `ticket_owner`, `ticket_object`) VALUES (?,?,?,?,?);";
    	
    	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
    	ps.setString(1, batch.getString("title"));
    	ps.setString(2, batch.getString("desc"));
        ps.setInt(3, batch.getInt("ticket_status_id"));
        ps.setInt(4, batch.getInt("ticket_owner"));
        ps.setInt(5, batch.getInt("ticket_object"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		ResultSet res = ps.getGeneratedKeys();
    		res.next();
    		System.out.println("Se ha creado el ticket n�mero "+res.getInt(1));
    		return JsonTreatment.sendResponseCode(200, res.getInt(1), "Se han a�adido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han a�adido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
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
    		System.out.println("Se ha creado el usuario n�mero "+res.getInt(1));
    		return JsonTreatment.sendResponseCode(200, res.getInt(1), "Se han a�adido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han a�adido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    public JSONObject modifyUserPassword(JSONObject batch, int userId) throws SQLException {

    	String query = "UPDATE Login SET shdw_passwd = ? WHERE  user_id = "+ userId;
    	
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
    
    public JSONObject modifyOwnUser(JSONObject batch, int userId) throws SQLException {

    	String query = 
    		"UPDATE Login SET email = ?, name = ?, last_name = ?, user_type = ? WHERE user_id = ("
    		+ "SELECT user_id FROM Login WHERE login_id = " + userId + ")";
    	
    	ps = con.prepareStatement(query);

    	ps.setString(1, batch.getString("email"));
    	ps.setString(2, batch.getString("name"));
        ps.setString(3, batch.getString("lastname"));
        ps.setInt(4, batch.getInt("user_type"));

    	int result = ps.executeUpdate();
    	
    	if (result >= 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han modificado "+ result +" lineas");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han modificado "+ result +" lineas");
    	}
    	
    }
    
    /*************************************************************************************/
    
    public JSONObject modifyUser(JSONObject batch) throws SQLException {

    	String query = 
    		"UPDATE Login SET email = ?, name = ?, last_name = ?, user_type = ? WHERE user_id = ?";
    	
    	ps = con.prepareStatement(query);

    	ps.setString(1, batch.getString("email"));
    	ps.setString(2, batch.getString("name"));
        ps.setString(3, batch.getString("lastname"));
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
    		System.out.println("Se ha creado el Login n�mero "+res.getInt(1));
    		return JsonTreatment.sendResponseCode(200, res.getInt(1), "Se han a�adido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han a�adido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    // 
    
    /*************************************************************************************/
    
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
    		System.out.println("Se ha creado el evento n�mero "+res.getInt(1));
    		return JsonTreatment.sendResponseCode(200, res.getInt(1), "Se han a�adido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han a�adido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    public JSONObject newTask(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO Task (`event_id`, `time`, `is_done`) VALUES (?, ?, ?)";
    	
    	ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    	
    	ps.setInt(1, batch.getInt("event_id"));
    	ps.setInt(2, batch.getInt("time"));
        ps.setBoolean(3, batch.getBoolean("is_done"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		ResultSet res = ps.getGeneratedKeys();
    		res.next();
    		System.out.println("Se ha creado la tarea n�mero "+res.getInt(1));
    		return JsonTreatment.sendResponseCode(200, res.getInt(1), "Se han a�adido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han a�adido "+ result +" lineas a la base de datos");
    	}
    	
    }

}
