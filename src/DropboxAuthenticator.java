import java.io.*;
import java.net.*;
import org.json.JSONObject;

// Handles OAuth2 authentication with Dropbox
public class DropboxAuthenticator {
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String authorizationCode;
    private String accessToken;
    private String refreshToken;
    
    private final String AUTH_URL;
    private final String TOKEN_URL;
    private final String SCOPES;
    
    public DropboxAuthenticator(String clientId, String clientSecret, String redirectUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        
        // Load URLs and scopes from config
        this.AUTH_URL = Config.get("dropbox.auth.url");
        this.TOKEN_URL = Config.get("dropbox.token.url");
        this.SCOPES = Config.get("dropbox.scopes");
    }
    
    // Generate the authorization URL that user needs to visit
    public String getAuthorizationUrl() {
        
        try {
            String authUrl = AUTH_URL + 
                "?client_id=" + URLEncoder.encode(clientId, "UTF-8") + 
                "&response_type=code" + 
                "&redirect_uri=" + URLEncoder.encode(redirectUrl, "UTF-8") + 
                "&scope=" + URLEncoder.encode(SCOPES, "UTF-8");
            
            System.out.println("\n========== STEP 1: Authorization ==========");
            System.out.println("Please visit this URL to authorize the application:");
            System.out.println(authUrl);
            System.out.println("\nAfter authorization, you'll be redirected with a code parameter.");
            System.out.println("Copy the 'code' value from the redirect URL.");
            
            return authUrl;
        } catch (UnsupportedEncodingException e) {
            System.out.println("Error encoding authorization URL: " + e.getMessage());
            return null;
        }
    }
    
    // Exchange the authorization code for an access token
    public boolean exchangeCodeForToken(String code) {
        try {
            this.authorizationCode = code;
            
            System.out.println("\n========== STEP 2: Token Exchange ==========");
            System.out.println("Exchanging authorization code for access token...");
            
            // Prepare the request parameters
            String params = "code=" + URLEncoder.encode(code, "UTF-8") + 
                          "&grant_type=authorization_code" + 
                          "&client_id=" + URLEncoder.encode(clientId, "UTF-8") + 
                          "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8") + 
                          "&redirect_uri=" + URLEncoder.encode(redirectUrl, "UTF-8");
            
            // Make the POST request
            URL url = new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(Config.getInt("api.timeout.connect"));
            conn.setReadTimeout(Config.getInt("api.timeout.read"));
            
            // Send the request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = params.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);
            
            if (responseCode == 200) {
                JSONObject json = new JSONObject(response);
                this.accessToken = json.getString("access_token");
                
                if (json.has("refresh_token")) {
                    this.refreshToken = json.getString("refresh_token");
                }
                
                System.out.println("Token obtained successfully!");
                System.out.println("Access Token: " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
                if (refreshToken != null) {
                    System.out.println("Refresh Token: " + refreshToken.substring(0, Math.min(20, refreshToken.length())) + "...");
                }
                
                return true;
            } else {
                System.out.println("Failed to obtain token. Response Code: " + responseCode);
                System.out.println("Response: " + response);
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error exchanging code for token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private String readResponse(HttpURLConnection conn) throws IOException {
        InputStream inputStream;
        
        if (conn.getResponseCode() >= 400) {
            inputStream = conn.getErrorStream();
        } else {
            inputStream = conn.getInputStream();
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine;
        
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        br.close();
        
        return response.toString();
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setAccessToken(String token) {
        this.accessToken = token;
    }
}
