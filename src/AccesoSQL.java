import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class AccesoSQL {
	
	private static final String SURL = "jdbc:mysql://localhost/produccion_db";
    private static final String USU = "pedro";
    private static final String PASS = "oxgnub";
    
    private static JSONArray commands = null;
    private JSONArray content;
    
    private Connection con;
    private PreparedStatement ps;
    private ResultSet rs;
    
    /*************************************************************************************/
    
    public AccesoSQL() {
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
    	
    	return JsonTreatment.sendResponseCode(201, commands);
    	
    }
    
    /*************************************************************************************/
    
    public JSONObject login(String[] credentials) throws SQLException {

    	content = new JSONArray();
    	String query = "SELECT * FROM Login WHERE login_name LIKE ?";
    	String token = null; 
    	boolean error = false;
    	
    	ps = con.prepareStatement(query);
    	ps.setString(1, credentials[0]);
    	
    	rs = ps.executeQuery();
    	
    	while (rs.next()) {
    		
    		if (credentials[1].equals(rs.getString(3))) {
    			
    			try {
    				token = assignToken(rs.getInt(1));
    			} catch (Exception e) {
    				token = e.getMessage();
    				error = true;
    			}
    			
    			if (error) {
    				content.put(new JSONObject().put("content", "Error al generar el token: "+token));
    		    	return JsonTreatment.sendResponseCode(500, content);
    			}
    			
    			if (token.equals("400")) {
    				
    				content.put(new JSONObject().put("content", "Error indeterminado"));
    		    	return JsonTreatment.sendResponseCode(500, content);
    				
    			} else {
    			
    				content.put(new JSONObject().put("content", token));
        			return JsonTreatment.sendResponseCode(200, content);
    				
    			}
    			
    		}
    	}
    	content.put(new JSONObject().put("content", "Ningún usuario coincide con esas credenciales"));
    	return JsonTreatment.sendResponseCode(400, content);
    }
    
    /*************************************************************************************/
    
    public JSONObject loginList() throws SQLException {

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
    /*************** FINAL ***************************************************************/
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
    		responseB.put("id", rs.getInt(1));
    		responseB.put("start_date", rs.getDate(2));
    		responseB.put("end_date", rs.getDate(3));
    		responseB.put("mod_date", rs.getDate(4));
    		responseB.put("creation_date", rs.getDate(5));
    		responseB.put("title", rs.getString(6));
    		responseB.put("desc", rs.getString(7));
    		responseB.put("ticket_status", rs.getInt(8));
    		responseB.put("ticket_owner", rs.getInt(9));
    		responseB.put("ticket_object", rs.getInt(10));
    		content.put(responseB);
    	}
    	return JsonTreatment.sendResponseCode(200, content);
    }
    
    /*************************************************************************************/
    
    public JSONObject newTicket(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO Ticket (`title`, `desc`, `ticket_status_id`, `ticket_owner`, `ticket_object`) VALUES (?,?,?,?,?);";
    	
    	ps = con.prepareStatement(query);
    	
    	ps.setString(1, batch.getString("title"));
    	ps.setString(2, batch.getString("description"));
        ps.setInt(3, batch.getInt("status"));
        ps.setInt(4, batch.getInt("owner"));
        ps.setInt(5, batch.getInt("object"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han añadido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    public JSONObject newUser(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO User (`email`, `name`, `last_name`, `user_type`) VALUES (?,?,?,?)";
    	
    	ps = con.prepareStatement(query);
    	
    	ps.setString(1, batch.getString("email"));
    	ps.setString(2, batch.getString("name"));
        ps.setString(3, batch.getString("lastname"));
        ps.setInt(4, batch.getInt("user_type"));

    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han añadido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    public String assignToken(int userId) throws Exception {
    	String query = "INSERT INTO Token (`login_id`, `token_hash`) VALUES (?,?) ON DUPLICATE KEY UPDATE login_id=?, token_hash=?";
    	String token = TokenGenerator.generate();
    	
    	ps = con.prepareStatement(query);
    	
    	ps.setInt(1, userId);
    	ps.setString(2, token);
    	ps.setInt(3, userId);
    	ps.setString(4, token);
    	
    	int result = ps.executeUpdate();
    	
    	if (result < 1) {
    		return "400";
    	} else {
    		return token;
    	}
    	
    }
    

}
