import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        // Load configuration
        Config.load();
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.println("Starting OAuth2 Authentication...\n");
            
            // Get credentials from config
            String clientId = Config.get("dropbox.client.id");
            String clientSecret = Config.get("dropbox.client.secret");
            String redirectUrl = Config.get("dropbox.redirect.url");
            
            DropboxAuthenticator auth = new DropboxAuthenticator(clientId, clientSecret, redirectUrl);
            
            // Display the authorization URL
            auth.getAuthorizationUrl();
            
            // Wait for user to authorize and paste the code
            System.out.println("\nEnter the authorization code from the redirect URL:");
            System.out.print("> ");
            String authCode = scanner.nextLine().trim();
            
            if (authCode.isEmpty()) {
                System.out.println("Error: Authorization code cannot be empty!");
                scanner.close();
                return;
            }
            
            // Exchange the code for an access token
            if (!auth.exchangeCodeForToken(authCode)) {
                System.out.println("Error: Authentication failed!");
                scanner.close();
                return;
            }
            
            // Now create the API client
            System.out.println("\nInitializing API Client...");
            DropboxAPIClient apiClient = new DropboxAPIClient(auth.getAccessToken());
            
            // Test the APIs
            System.out.println("\n--- Testing Dropbox APIs ---\n");
            apiClient.getTeamInfo();
            apiClient.listTeamMembers();
            apiClient.getTeamAuditLog();
            
            System.out.println("\n--- API Testing Complete ---\n");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
