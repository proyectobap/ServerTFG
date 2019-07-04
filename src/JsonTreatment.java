import org.json.JSONArray;
import org.json.JSONObject;

public class JsonTreatment {
	
	/*
	 * Clase para dar formato a la salida de informaci�n hacia los clientes.
	 * Como el resultado de las peticiones no tiene porque dar un solo elemento
	 * se devuelve siempre en formato JSONObject, con un JSONArray dentro que
	 * aglomera la informaci�n devuelta.
	 * 
	 * Hay tantas versiones de respuesta como posibilidades hay de formato de 
	 * respuesta.
	 * 
	 * Tambi�n se incluye la respuesta de login.
	 */
		
	// En este caso devuelve un c�digo de estado de la respuesta y el array vinculado.
	public static JSONObject sendResponseCode(int code, JSONArray json) {
		JSONObject response = new JSONObject();
		
		response.put("response", code);
		response.put("content", json);
		
		return response;
	}
	
	// Igual que el anterior, est� adaptado para respuestas simples que requieran
	// solo una linea de texto, como puede ser informaci�n sobre un error.
	public static JSONObject sendResponseCode(int code, String resp) {
		JSONObject response = new JSONObject();
		
		JSONObject answer = new JSONObject();
		JSONArray content = new JSONArray();
		
		answer.put("answer", resp);
		content.put(answer);
		
		response.put("response", code);
		response.put("content", content);
		
		return response;
	}
	
	// Igual que el m�todo anterior, pero orientado a creaci�n de registros.
	// Devuelve la id de la entrada creada en la BBDD.
	public static JSONObject sendResponseCode(int code, int id, String resp) {
		JSONObject response = new JSONObject();
		
		JSONObject answer = new JSONObject();
		JSONArray content = new JSONArray();
		
		answer.put("answer", resp);
		answer.put("id", id);
		content.put(answer);
		
		response.put("response", code);
		response.put("content", content);
		
		return response;
	}
	
	// Respuesta al proceso de login, que requiere de m�s campos de los habituales.
	public static JSONObject sendLoginCode(int code, int permissionsId, int userId,int loginId, String resp) {
		JSONObject response = new JSONObject();
		
		JSONObject answer = new JSONObject();
		JSONArray content = new JSONArray();
		
		answer.put("answer", resp);
		answer.put("user_type", permissionsId);
		answer.put("user_id", userId);
		answer.put("login_id", loginId);
		content.put(answer);
		
		response.put("response", code);
		response.put("content", content);
		
		return response;
	}
	
	// Respuesta automatizada en caso de no encontrar el comando requerido.
	public static JSONObject nullResponse() {
		JSONObject response = new JSONObject();
		JSONArray content = new JSONArray();
		
		content.put(new JSONObject().put("content","comando no encontrado"));
		
		response.put("response", 400);
		response.put("content", content);
		
		return response;
	}
	
	// Respuesta automatizada en caso de que un comando SQL de error. Env�a
	// el error al cliente con un c�digo 300.
	public static JSONObject sendSQLErrorCode(String resp) {
		JSONObject response = new JSONObject();
		
		JSONObject answer = new JSONObject();
		JSONArray content = new JSONArray();
		
		answer.put("answer", resp);
		content.put(answer);
		
		response.put("response", 300);
		response.put("content", content);
		
		return response;
	}

}
