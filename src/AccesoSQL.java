import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class AccesoSQL {
	
	private static final String SURL = "jdbc:mysql://localhost/produccion_db";
    private static final String USU = "pedro";
    private static final String PASS = "oxgnub";
    
    private static Map<String, String> commands = null;
    private JSONArray content;
    
    private Connection con;
    private PreparedStatement ps;
    private ResultSet rs;
    
    /*************************************************************************************/
    
    public AccesoSQL() throws SQLException {
    	
    	if (commands == null) {
    		commands = new HashMap<>();
    		
    		commands.put("help", "Muestra la lista de comandos");
    		commands.put("login (usuario, contraseña)", "Intenta proceso de login con credenciales");
    	}
    	
    	con = DriverManager.getConnection(SURL, USU, PASS);
    	if (con.isClosed()) System.out.println("X");
    	else System.out.print("O");
    }
    
    /*************************************************************************************/
    
    public boolean closeConnection() throws SQLException {
    	con.close();
    	if (con.isClosed()) {
    		System.out.println("Conexion cerrada");
    		return true;
    	} else {
    		System.out.println("No se pudo cerrar la conexion");
    		return false;
    	}
    }
    
    /*************************************************************************************/
    
    public JSONObject help() {
    	
    	content = new JSONArray();
    	content.put(commands);
    	return JsonTreatment.sendResponseCode(201, content);
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
    
    public JSONObject newTicket(JSONObject batch) throws SQLException {

    	String query = "INSERT INTO 'produccion_db'.'Ticket' ('desc', 'ticket_status_id', 'ticket_owner', 'ticket_object') VALUES (?,?,?,?);";
    	JSONObject response = new JSONObject();
    	ps = con.prepareStatement(query);
    	
    	ps.setString(1, batch.getString("description"));
        ps.setInt(2, batch.getInt("status"));
        ps.setInt(3, batch.getInt("owner"));
        ps.setInt(4, batch.getInt("object"));
        // Falta añadir la vinculación de los técnicos asignados.
    	int result = ps.executeUpdate();
    	
    	if (result == 1) {
    		response.put("response", 200);
    		response.put("content", "Se han añadido "+ result +" lineas a la base de datos");
    		return response;
    	} else {
    		response.put("response", 400);
    		response.put("content", "Se han añadido "+ result +" lineas a la base de datos");
    		return response;
    	}
    	
    }
    
    /*************************************************************************************/
    

}
