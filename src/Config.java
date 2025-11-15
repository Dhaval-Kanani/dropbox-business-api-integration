import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Configuration manager for loading app settings
public class Config {
    private static Properties properties = new Properties();
    private static boolean loaded = false;
    
    // Load configuration from properties file
    public static void load() {
        if (loaded) return;
        
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
            loaded = true;
        } catch (IOException e) {
            System.out.println("Warning: Could not load config.properties, using defaults");
            loadDefaults();
        }
    }
    
    // Fallback to hardcoded defaults if config file not found
    private static void loadDefaults() {
        properties.setProperty("dropbox.client.id", "q29ad047xq1ww27");
        properties.setProperty("dropbox.client.secret", "0rqcq9mihewsauc");
        properties.setProperty("dropbox.redirect.url", "https://oauth.pstmn.io/v1/callback");
        properties.setProperty("dropbox.api.base.url", "https://api.dropboxapi.com/2");
        properties.setProperty("dropbox.auth.url", "https://www.dropbox.com/oauth2/authorize");
        properties.setProperty("dropbox.token.url", "https://api.dropboxapi.com/oauth2/token");
        properties.setProperty("dropbox.scopes", "team_info.read members.read events.read sessions.list");
        properties.setProperty("api.timeout.connect", "10000");
        properties.setProperty("api.timeout.read", "10000");
        properties.setProperty("api.members.limit", "100");
        properties.setProperty("api.events.limit", "50");
        loaded = true;
    }
    
    public static String get(String key) {
        if (!loaded) load();
        return properties.getProperty(key);
    }
    
    public static int getInt(String key) {
        if (!loaded) load();
        return Integer.parseInt(properties.getProperty(key));
    }
}
