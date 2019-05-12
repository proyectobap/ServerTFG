import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class AccesoSQL {
	
	private static final String SURL = "jdbc:mysql://localhost/produccion_db";
    private static final String USU = "root";
    private static final String PASS = "oxgnub";
    
    private static Map<String, String> commands = null;
    
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
    	if (con.isClosed()) System.out.println("Error en la conexion");
    	else System.out.println("Conexion Exitosa");
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
    
    public JSONArray help() {
    	
    	return new JSONArray().put(commands);
    	    	
    }
    
    /*************************************************************************************/
    
    public boolean login(String username, String password) throws SQLException {

    	String query = "SELECT * FROM login WHERE login_name LIKE '?'";
    	
    	ps = con.prepareStatement(query);
    	ps.setString(1, username);
    	
    	rs = ps.executeQuery();
    	while (rs.next()) {
    		if (password.equals(rs.getString(1))) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /*************************************************************************************/
    
    public JSONObject loginList() throws SQLException {

    	String query = "SELECT * FROM user";
    	JSONObject login = new JSONObject();
    	ps = con.prepareStatement(query);
    	
    	rs = ps.executeQuery();
    	while (rs.next()) {
    		login.put("response", 200);
    		login.put("id", rs.getInt(1));
    		login.put("email", rs.getString(2));
    		login.put("name", rs.getString(3));
    		login.put("last_name", rs.getString(4));
    		login.put("user_type", rs.getInt(5));
    	}
    	return login;
    }
    
    /*************************************************************************************/

}
