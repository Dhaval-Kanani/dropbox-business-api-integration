import java.io.*;
import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;

// Client for calling Dropbox Business APIs
public class DropboxAPIClient {
    private String accessToken;
    private final String API_BASE_URL;
    
    public DropboxAPIClient(String accessToken) {
        this.accessToken = accessToken;
        this.API_BASE_URL = Config.get("dropbox.api.base.url");
    }
    
    // Get basic team information
    public JSONObject getTeamInfo() {
        System.out.println("\n========== API 1: Get Team Information ==========");
        System.out.println("Endpoint: /team/get_info");
        System.out.println("Scope: team_info.read");
        
        String endpoint = API_BASE_URL + "/team/get_info";
        
        try {
            JSONObject response = makePostRequest(endpoint, null);
            
            System.out.println("\nSuccess!");
            System.out.println("Team ID: " + response.getString("team_id"));
            System.out.println("Team Name: " + response.getString("name"));
            
            if (response.has("plan")) {
                System.out.println("Plan: " + response.getString("plan"));
            }
            
            System.out.println("\nFull Response:\n" + response.toString(2));
            
            return response;
        } catch (Exception e) {
            System.out.println("Error fetching team info: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * API 2: List Team Members
     * Endpoint: /team/members/list_v2
     * Scope: members.read
     * @return JSONObject containing team members
     */
    public JSONObject listTeamMembers() {
        System.out.println("\n========== API 2: List Team Members ==========");
        System.out.println("Endpoint: /team/members/list_v2");
        System.out.println("Scope: members.read");
        
        String endpoint = API_BASE_URL + "/team/members/list_v2";
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("limit", Config.getInt("api.members.limit"));
            
            System.out.println("Request Body: " + requestBody.toString());
            
            JSONObject response = makePostRequest(endpoint, requestBody);
            
            JSONArray members = response.getJSONArray("members");
            System.out.println("\nSuccess!");
            System.out.println("Total Members Retrieved: " + members.length());
            System.out.println("\nMembers List:");
            System.out.println("─".repeat(60));
            
            for (int i = 0; i < members.length(); i++) {
                JSONObject member = members.getJSONObject(i);
                JSONObject profile = member.getJSONObject("profile");
                
                String email = profile.optString("email", "N/A");
                String displayName = profile.optString("display_name", "N/A");
                String status = member.optString("role", "N/A");
                
                System.out.println((i + 1) + ". Name: " + displayName);
                System.out.println("   Email: " + email);
                System.out.println("   Role: " + status);
                System.out.println("─".repeat(60));
            }
            
            System.out.println("\nFull Response:\n" + response.toString(2));
            
            return response;
        } catch (Exception e) {
            System.out.println("Error listing team members: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Get team audit log events
    public JSONObject getTeamAuditLog() {
        System.out.println("\n========== API 4: Get Team Audit Log (Sign-in Events) ==========");
        System.out.println("Endpoint: /team_log/get_events");
        System.out.println("Scope: events.read, sessions.list");
        
        String endpoint = API_BASE_URL + "/team_log/get_events";
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("limit", Config.getInt("api.events.limit"));
            
            System.out.println("Request Body: " + requestBody.toString());
            
            JSONObject response = makePostRequest(endpoint, requestBody);
            
            JSONArray events = response.getJSONArray("events");
            System.out.println("\nSuccess!");
            System.out.println("Total Events Retrieved: " + events.length());
            System.out.println("\nRecent Audit Events:");
            System.out.println("─".repeat(60));
            
            for (int i = 0; i < Math.min(10, events.length()); i++) {
                JSONObject event = events.getJSONObject(i);
                
                String eventType = event.optString("event_type", "N/A");
                String timestamp = event.optString("timestamp", "N/A");
                String actor = "N/A";
                
                if (event.has("actor")) {
                    actor = event.getJSONObject("actor").optString("email", "N/A");
                }
                
                System.out.println((i + 1) + ". Event Type: " + eventType);
                System.out.println("   Timestamp: " + timestamp);
                System.out.println("   Actor: " + actor);
                System.out.println("─".repeat(60));
            }
            
            System.out.println("\nFull Response:\n" + response.toString(2));
            
            return response;
        } catch (Exception e) {
            System.out.println("Error fetching audit log: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Helper method to make POST requests to Dropbox API
    private JSONObject makePostRequest(String endpoint, JSONObject requestBody) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setConnectTimeout(Config.getInt("api.timeout.connect"));
        conn.setReadTimeout(Config.getInt("api.timeout.read"));
        
        // Only set content type and write body if we have one
        if (requestBody != null) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }
        
        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);
        
        System.out.println("Response Code: " + responseCode);
        
        if (responseCode == 200) {
            return new JSONObject(response);
        } else if (responseCode == 401) {
            throw new Exception("Unauthorized - Check your access token");
        } else if (responseCode == 403) {
            throw new Exception("Forbidden - Insufficient permissions. Required scopes may be missing.");
        } else {
            System.out.println("API Error Response: " + response);
            throw new Exception("API request failed with status: " + responseCode);
        }
    }
    
    /**
     * Helper method to read HTTP response from connection
     * Handles both success and error streams
     * @param conn HttpURLConnection to read from
     * @return Response body as String
     * @throws IOException if reading fails
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        InputStream inputStream;
        
        // Try to read from error stream if response code indicates an error
        if (conn.getResponseCode() >= 400) {
            inputStream = conn.getErrorStream();
        } else {
            inputStream = conn.getInputStream();
        }
        
        if (inputStream == null) {
            return "";
        }
        
        BufferedReader br = new BufferedReader(
            new InputStreamReader(inputStream, "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine;
        
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        br.close();
        
        return response.toString();
    }
}
