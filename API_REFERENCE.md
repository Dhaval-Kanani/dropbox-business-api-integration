# Dropbox Business API Reference

Quick reference for the 4 APIs implemented in this project (using 3 unique endpoints).

## Base URL
```
https://api.dropboxapi.com/2
```

## Authentication
All requests need:
- `Authorization: Bearer {access_token}`
- `Content-Type: application/json`

---

## 1. Get Team/Organization Name

**Endpoint:** `POST /team/get_info`  
**Scope:** `team_info.read`

Gets team/organization name.

**Request:** null body

**Response (relevant fields):**
```json
{
  "team_id": "dbtid:xxx",
  "name": "Dhavalkanani90"
}
```

---

## 2. Get Plan Type / License Assigned

**Endpoint:** `POST /team/get_info`  
**Scope:** `team_info.read`

Gets license and plan information. 

**Note:** This uses the same endpoint as API 1 because `/team/get_info` returns comprehensive team information including both name and license details in a single response.

**Request:** null body

**Response (relevant fields):**
```json
{
  "team_id": "dbtid:xxx",
  "name": "Dhavalkanani90",
  "num_licensed_users": 5,
  "num_provisioned_users": 1,
  "num_used_licenses": 1
}
```

---

## 3. List All Users in Organization

**Endpoint:** `POST /team/members/list_v2`  
**Scope:** `members.read`

Lists all team members.

**Request:**
```json
{
  "limit": 100
}
```

**Response:**
```json
{
  "members": [
    {
      "profile": {
        "team_member_id": "dbmid:xxx",
        "email": "user@example.com",
        "name": {
          "display_name": "John Doe"
        },
        "status": "active"
      }
    }
  ],
  "has_more": false,
  "cursor": "..."
}
```

---

## 4. Fetch Sign-In / Audit Events

**Endpoint:** `POST /team_log/get_events`  
**Scope:** `events.read, sessions.list`

Gets audit log events including sign-ins.

**Request:**
```json
{
  "limit": 50
}
```

**Response:**
```json
{
  "events": [
    {
      "timestamp": "2025-11-15T10:30:48Z",
      "event_type": {
        ".tag": "member_change_status",
        "description": "Changed member status"
      },
      "actor": {
        ".tag": "admin",
        "admin": {
          "email": "admin@example.com"
        }
      }
    }
  ],
  "cursor": "...",
  "has_more": false
}
```

---

## OAuth2 Scopes

To use all 4 APIs, request these scopes:
```
team_info.read members.read events.read sessions.list
```

## Notes

- APIs 1 & 2 use the same endpoint (`/team/get_info`) because it returns both team name and license information in one call
- Team info API requires null body (not empty JSON)
- List members supports pagination via cursor
- Audit log events include member changes, logins, file access, etc.
```
members.read
```

### Description
List all members in the team, including their email, name, and account status.

### Request Parameters
```json
{
  "limit": 100,
  "cursor": null
}
```

- `limit` (optional): Maximum number of members to return (1-1000, default 100)
- `cursor` (optional): For pagination, returned in response as `cursor`

### Response Example
```json
{
  "members": [
    {
      "profile": {
        "team_member_id": "dbmid:AAAH_XXXXX",
        "email": "john@acme.com",
        "display_name": "John Doe",
        "secondary_emails": [],
        "member_folder_id": "id:a4ayc_80_OEAAAAAAAAAXw",
        "status": "active"
      },
      "role": "admin"
    },
    {
      "profile": {
        "team_member_id": "dbmid:AAAH_YYYYY",
        "email": "jane@acme.com",
        "display_name": "Jane Smith",
        "secondary_emails": [],
        "member_folder_id": "id:a4ayc_80_OEAAAAAAAAAYy",
        "status": "active"
      },
      "role": "member_limited"
    }
  ],
  "cursor": "ZltEtqoZFyCdkAAA",
  "has_more": true
}
```

### Key Fields
- `members[]`: Array of team members
- `profile.email`: Member email address
- `profile.display_name`: Member display name
- `profile.status`: Account status (active, suspended, etc.)
- `role`: Role in team (admin, member, etc.)
- `has_more`: Whether more results are available
- `cursor`: Token for pagination

### Pagination Example
```bash
# First call
curl -X POST https://api.dropboxapi.com/2/team/members/list \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"limit": 100}'

# Next page (use cursor from previous response)
curl -X POST https://api.dropboxapi.com/2/team/members/list \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"limit": 100, "cursor": "ZltEtqoZFyCdkAAA"}'
```

---

## API 3: Get Team Audit Log (Events)

### Endpoint
```
POST /team_audit/get_events
```

### Scope Required
```
audit_log.read
```

### Description
List audit log events for the team, including sign-in events, file access, and administrative actions.

### Request Parameters
```json
{
  "limit": 100,
  "cursor": null
}
```

- `limit` (optional): Maximum events to return (1-1000, default 100)
- `cursor` (optional): For pagination

### Response Example
```json
{
  "events": [
    {
      "timestamp": "2025-11-15T10:30:45Z",
      "event_type": "login",
      "actor": {
        "email": "john@acme.com",
        "display_name": "John Doe"
      },
      "origin": {
        "geo_location": {
          "city": "San Francisco",
          "region": "CA",
          "country": "US"
        },
        "ip_address": "192.0.2.1",
        "access_method": {
          "auth_method": "password",
          "device_key": "0123456789abcdef"
        }
      }
    },
    {
      "timestamp": "2025-11-15T09:15:22Z",
      "event_type": "sign_in",
      "actor": {
        "email": "jane@acme.com",
        "display_name": "Jane Smith"
      },
      "origin": {
        "ip_address": "203.0.113.45",
        "access_method": {
          "auth_method": "saml"
        }
      }
    }
  ],
  "cursor": "ZltEtqoZFyCdkAAA",
  "has_more": true
}
```

### Key Event Types
- `login`: User login event
- `sign_in`: Sign-in event
- `logout`: User logout
- `file_access`: File accessed
- `member_add`: Member added to team
- `member_remove`: Member removed from team
- `member_change_role`: Member role changed
- `team_policy_change`: Policy changed

### Key Fields
- `timestamp`: ISO 8601 timestamp of event
- `event_type`: Type of event
- `actor`: User who performed the action
- `actor.email`: User's email
- `origin.ip_address`: IP address of origin
- `origin.geo_location`: Geographic information
- `has_more`: Whether more results available
- `cursor`: Pagination token

### Curl Example
```bash
curl -X POST https://api.dropboxapi.com/2/team_audit/get_events \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"limit": 50}'
```

---

## Error Responses

### 401 Unauthorized
```json
{
  "error": {
    ".tag": "invalid_access_token",
    "reason": {
      ".tag": "expired_access_token"
    }
  }
}
```

### 403 Forbidden (Missing Scopes)
```json
{
  "error": {
    ".tag": "insufficient_scope",
    "required_scope": "team_info.read"
  }
}
```

### 429 Too Many Requests (Rate Limited)
```json
{
  "error": {
    ".tag": "rate_limited",
    "retry_after": 60
  }
}
```

---

## Rate Limits

- **API Calls**: Generally 100 requests per second per app
- **Audit Log**: May have separate rate limits
- **Recommended Wait**: 1-2 seconds between requests for production

---

## Pagination

For endpoints with potentially many results:

```
1. Make initial request with limit
2. Check response for has_more field
3. If has_more is true, use cursor for next request
4. Repeat until has_more is false or all data retrieved
```

---

**Last Updated**: November 2025  
**Dropbox API Version**: 2  
**Documentation**: https://www.dropbox.com/developers/documentation/http/documentation
