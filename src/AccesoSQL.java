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
    
    public AccesoSQL() throws SQLException {
    	
    	if (commands == null) {
    		commands = new JSONArray();
    		// Debug
    		commands.put(new JSONObject().put("help", "Muestra la lista de comandos (Debug)"));
    		commands.put(new JSONObject().put("login", "muestra lista de usuarios (Debug)"));
    		// Create
    		commands.put(new JSONObject().put("newTicket (title,description,status,owner,object)", "Inserta un nuevo Ticket"));
    		commands.put(new JSONObject().put("newUser (`email, name, last_name, user_type)", "Inserta un nuevo Usuario"));
    		// List
    		commands.put(new JSONObject().put("listTicket", "Muestra todos los Tickets"));
    		commands.put(new JSONObject().put("listticketstatus", "Lista los posibles estados de un ticket"));
    		commands.put(new JSONObject().put("listeventtype", "Lista los posibles tipos de eventos"));
    		commands.put(new JSONObject().put("listelementtype", "Lista los posibles tipos de elementos"));
    		commands.put(new JSONObject().put("listusertype", "Lista los posibles tipos de usuarios"));
    	}
    	
    	con = DriverManager.getConnection(SURL, USU, PASS);
    	if (con.isClosed()) System.out.println(Consola.RED+"X"+Consola.RESET);
    	else System.out.print("O");
    }
    
    /*************************************************************************************/
    
    public boolean closeConnection() throws SQLException {
    	con.close();
    	if (con.isClosed()) {
    		//System.out.println("Conexion cerrada");
    		return true;
    	} else {
    		//System.out.println("No se pudo cerrar la conexion");
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
    
    public JSONObject login(String username, String password) throws SQLException {

    	String query = "SELECT * FROM Login WHERE login_name LIKE '?'";
    	
    	JSONObject response = new JSONObject();
    	
    	ps = con.prepareStatement(query);
    	ps.setString(1, username);
    	
    	rs = ps.executeQuery();
    	while (rs.next()) {
    		if (password.equals(rs.getString(1))) {
    			response.put("response", 200);
    			response.put("content", "token");
    			return response;
    		}
    	}
    	
    	response.put("response", 400);
		response.put("content", "Ningún usuario coincide con esas credenciales");
    	return response;
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
        // Falta añadir la vinculación de los técnicos asignados.
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
        // Falta añadir la vinculación de los técnicos asignados.
    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		return JsonTreatment.sendResponseCode(200, "Se han añadido "+ result +" lineas a la base de datos");
    	} else {
    		return JsonTreatment.sendResponseCode(400, "Se han añadido "+ result +" lineas a la base de datos");
    	}
    	
    }
    
    /*************************************************************************************/
    
    
    

}
