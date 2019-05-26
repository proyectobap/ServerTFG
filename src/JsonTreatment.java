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
	
	public static JSONObject nullResponse() {
		JSONObject response = new JSONObject();
		JSONArray content = new JSONArray();
		
		content.put(new JSONObject().put("content","comando no encontrado"));
		
		response.put("response", 400);
		response.put("content", content);
		
		return response;
	}

}
