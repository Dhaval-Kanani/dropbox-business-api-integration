# Dropbox Business API Integration

Java implementation for Dropbox Business APIs with OAuth2 authentication.

## Quick Start

1. **Setup Configuration**
   ```bash
   cp config.properties.template config.properties
   # Edit config.properties with your Dropbox app credentials
   ```

2. **Get the JSON library**
   - Download json-20231013.jar from Maven Central
   - Put it in the `lib/` folder

3. **Compile**
   ```bash
   .\compile.bat
   ```

4. **Run**
   ```bash
   java -cp "bin;lib/*" Main
   ```

## Configuration

Edit `config.properties`:
```properties
dropbox.client.id=YOUR_CLIENT_ID
dropbox.client.secret=YOUR_CLIENT_SECRET
dropbox.redirect.url=YOUR_REDIRECT_URL
```

All other settings (timeouts, limits, URLs) can also be configured here.

## What's Inside

This project implements 4 Dropbox Business APIs (using 3 unique endpoints):

1. **Team/Organization Name** (`/team/get_info`) - Gets team name
2. **Plan Type/License Info** (`/team/get_info`) - Gets license count and plan details
   - Note: APIs 1 & 2 use the same endpoint because `/team/get_info` returns both team name and license information in a single response
3. **List All Users** (`/team/members/list_v2`) - Shows all team members  
4. **Audit/Sign-in Events** (`/team_log/get_events`) - Retrieves activity logs

## How It Works

Uses OAuth2 Code Flow for authentication. You'll need to:
- Visit an authorization URL
- Approve the app
- Copy the code from the redirect
- Paste it back in the terminal

## Files

```
src/
  Config.java - Configuration manager
  Main.java - Entry point
  DropboxAuthenticator.java - Handles OAuth2
  DropboxAPIClient.java - Makes API calls
config.properties - App configuration (not in git)
config.properties.template - Configuration template
lib/
  json-20231013.jar
bin/
  (compiled .class files)
```

## Setup

Copy the template and add your credentials:
```bash
cp config.properties.template config.properties
```


