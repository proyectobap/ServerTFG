import org.json.JSONArray;
import org.json.JSONObject;

public class JsonTreatment {
		
	public static JSONObject sendResponseCode(int code, JSONArray json) {
		JSONObject response = new JSONObject();
		
		response.put("response", code);
		response.put("content", json);
		
		return response;
	}
	
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
	
	public static JSONObject nullResponse() {
		JSONObject response = new JSONObject();
		JSONArray content = new JSONArray();
		
		content.put(new JSONObject().put("content","comando no encontrado"));
		
		response.put("response", 400);
		response.put("content", content);
		
		return response;
	}

}
