# REST API Message Path Map

This document maps every network interaction and the corresponding backend database operations initiated by the different screens and views within the application.

## 1. Login & Authentication
Handles user session lifecycle and account creation.

```mermaid
sequenceDiagram
    participant UI as LoginScreen
    participant API as Backend API
    participant DB as Database

    Note over UI, API: Path: Authentication (Login)
    UI->>API: POST /api/auth/login { username, password }
    API->>DB: SELECT * FROM users WHERE username = :username
    DB-->>API: User Record
    API->>DB: INSERT INTO sessions (user_id, token) VALUES (...)
    API-->>UI: 200 OK { id, username, sessionToken }

    Note over UI, API: Path: Account Registration (Sign Up)
    UI->>API: POST /api/auth/signup { username, password }
    API->>DB: INSERT INTO users (username, password) VALUES (...)
    DB-->>API: Created
    API-->>UI: 200 OK "Account successfully created!"
```

## 2. HomeScreen (Shell & Global Actions)
The shell handles global state, navigation, and core account-level resource management.

```mermaid
sequenceDiagram
    participant UI as HomeScreen
    participant API as Backend API
    participant DB as Database

    Note over UI, API: On Mount (Initialize Sidebar)
    UI->>API: GET /api/playlists/account/:userId
    API->>DB: SELECT * FROM playlists WHERE user_id = :userId
    DB-->>API: List of Playlists
    API-->>UI: 200 OK (Array of Playlists)

    Note over UI, API: User Action: Create Playlist
    UI->>API: POST /api/playlists?userId=:userId { name: "..." }
    API->>DB: INSERT INTO playlists (name, user_id) VALUES (...)
    DB-->>API: New Playlist Record
    API-->>UI: 200 OK (New Playlist Object)

    Note over UI, API: User Action: Logout
    UI->>API: POST /api/auth/logout?userId=:id&sessionToken=:token
    API->>DB: UPDATE sessions SET active = false WHERE token = :token
    API-->>UI: 200 OK
```

## 3. SearchPageView
Handles dynamic queries and the initial step of the "Add to Playlist" workflow.

```mermaid
sequenceDiagram
    participant UI as SearchPageView
    participant API as Backend API
    participant DB as Database

    Note over UI, API: User Action: Type in Search (Debounced)
    UI->>API: GET /api/search?query=:searchQuery
    API->>DB: SELECT * FROM artists, albums, songs WHERE name LIKE :query
    DB-->>API: Combined Search Results
    API-->>UI: 200 OK { artists: [], albums: [], songs: [] }

    Note over UI, API: User Action: Add Result to Playlist
    UI->>API: POST /api/playlists/:playlistId/songs/:songId
    API->>DB: INSERT INTO playlist_songs (playlist_id, song_id)
    DB-->>API: Success Confirmation
    API-->>UI: 200 OK
```

## 4. TrackListView (Artists & Albums)
A generic view that fetches song collections based on a specific entity type.

```mermaid
sequenceDiagram
    participant UI as TrackListView
    participant API as Backend API
    participant DB as Database

    Note over UI, API: On View Mount (Artist or Album)
    UI->>API: GET /api/:entityType/:id/songs
    API->>DB: SELECT * FROM songs WHERE :entityType_id = :id
    DB-->>API: Tracklist Data
    API-->>UI: 200 OK (Array of Songs)

    Note over UI, API: User Action: Add Song to Playlist
    UI->>API: POST /api/playlists/:playlistId/songs/:songId
    API->>DB: INSERT INTO playlist_songs (playlist_id, song_id)
    DB-->>API: Success Confirmation
    API-->>UI: 200 OK
```

## 5. PlaylistView
Manages detailed playlist interactions, including resource updates and deletions.

```mermaid
sequenceDiagram
    participant UI as PlaylistView
    participant API as Backend API
    participant DB as Database

    Note over UI, API: On View Mount
    UI->>API: GET /api/playlists/:playlistId
    API->>DB: SELECT p.*, s.* FROM playlists p JOIN playlist_songs ps...
    DB-->>API: Playlist Metadata + Song List
    API-->>UI: 200 OK { id, name, songs: [...] }

    Note over UI, API: User Action: Remove Song from Playlist
    UI->>API: DELETE /api/playlists/:id/songs/:songId
    API->>DB: DELETE FROM playlist_songs WHERE playlist_id = :id AND song_id = :songId
    DB-->>API: Success Confirmation
    API-->>UI: 200 OK

    Note over UI, API: User Action: Delete Entire Playlist
    UI->>API: DELETE /api/playlists/:playlistId
    API->>DB: DELETE FROM playlists WHERE id = :playlistId
    DB-->>API: Success Confirmation
    API-->>UI: 200 OK
```

## Summary of Data Patterns

| Action Type | HTTP Method | DB Operation | Purpose |
| :--- | :--- | :--- | :--- |
| **Create** | `POST` | `INSERT` | New records (users, playlists, song associations). |
| **Read** | `GET` | `SELECT` | Retrieving catalog items, search results, or user data. |
| **Update** | `POST` / `PATCH` | `UPDATE` | Modifying existing records (session invalidation). |
| **Delete** | `DELETE` | `DELETE` | Removing resources or associations. |

---
*Documentation generated for notify-frontend architecture.*