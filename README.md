# ArchivePlayer

NOTE: As of July 2026, ArchivePlayer is still under development, see the Roadmap for a list of features currently planned and being implemented.

ArchivePlayer transforms a local collection of MP3 files into a fully searchable, cloud-ready streaming application. Designed to mimic modern streaming platforms, it allows users to effortlessly index their media, search by track, artist, or album, manage custom playlists, and securely share their music library with a network of friends.

Link to Render-hosted project: **https://archiveplayer-frontend.onrender.com/** 

Note: Render may take a minute or two to spin up when using this link.

## Table of Contents
1.  [Prerequisites](#prerequisites)
2.  [Running the Application Locally with Docker Compose](#running-the-application-locally-with-docker-compose)
    *   [Setup](#setup)
    *   [Creating a .env file](#creating-a-env-file)
    *   [Running the Application](#running-the-application)
    *   [Accessing the Application](#accessing-the-application)
    *   [Stopping the Application](#stopping-the-application)
3.  [Project Structure](#project-structure)
4.	[Engineering Roadmap](#engineering-roadmap)
5.  [Troubleshooting](#troubleshooting)

## Core Architecture & Tech Stack

*   **Backend**: Spring Boot (Java) enterprise-grade REST API, managing media indexing, relationship mapping, and business logic.
*   **Frontend**: React-driven Single Page Application (SPA), delivering a responsive audio playback interface.
*   **Database**: PostgreSQL, utilizing a relational schema optimized for music metadata and user relationship structures.
*   **Quality Assurance**: JUnit tests covering critical functions.


## Prerequisites

Before you begin, ensure you have the following installed:

*   **Git**: For cloning the repository.
*   **Docker Desktop**: Includes Docker Engine and Docker Compose. Ensure it's running and configured to share the drive where your project resides (e.g., `C:` drive on Windows).

## Running the Application Locally with Docker Compose

This section guides you on how to run the entire application stack locally using Docker Compose for demonstration purposes.

### Setup

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd <your-project-root>
    ```
2.  **Ensure `sample-music` is in the backend directory:**
    The `sample-music` directory containing your MP3 files should be located directly inside the `ArchivePlayer-backend` folder. If it's not, move it there:
    ```bash
    mv sample-music ArchivePlayer-backend/
    ```
    Then, ensure Git tracks this change:
    ```bash
    cd ArchivePlayer-backend
    git add sample-music/
    git commit -m "Move sample-music into backend directory for Docker build context"
    git push origin main
    cd ..
    ```
### Creating a .env file

For local development, sensitive information like JWT secrets should be managed outside of `docker-compose.yml` using a `.env` file.

1.  Create a file named `.env` in the root of yourF project (next to `docker-compose.yml`).
2.  Add the following content to the `.env` file:
    ```
    APP_JWT_SECRET=your-super-secret-jwt-key-for-local-dev-at-least-64-chars-long-1234567890
    ```
    **Important:** Replace `your-super-secret-jwt-key-for-local-dev-at-least-64-chars-long-1234567890` with a strong, random string of at least 64 characters. You can generate one online or use a tool.

### Running the Application

Navigate to the root of your project (where `docker-compose.yml` is located) and run:
    ```bash
    docker-compose down --volumes --remove-orphans
    docker-compose build --no-cache
    docker-compose up
    ```
    This command will:
    *   Build the Docker images for your `backend` and `frontend` services.
    *   Start the `archive-db` (PostgreSQL) container.
    *   Start the `backend` (Spring Boot) container.
    *   Start the `frontend` (Nginx serving React) container.
    *   Mount your local `ArchivePlayer-backend/sample-music` directory into `/app/archive` inside the backend container.
    *   The Spring Boot `DataLoader` will then process these MP3s and seed the database.

### Accessing the Application

Once all services are up and running (check logs for `backend-1 | ... Started ArchivePlayerApplication ...` and `frontend-1 | ... nginx: configuration file ... test is successful`), open your web browser and navigate to:

[http://localhost:3000](http://localhost:3000)

You should see the login screen.

### Stopping the Application

To stop and remove all running containers, networks, and volumes created by Docker Compose:
```bash
docker-compose down --volumes --remove-orphans
```

## Project Structure

```

├── ArchivePlayer-backend/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── sample-music/         # Contains your MP3 files
├── ArchivePlayer-frontend/
│   ├── public/
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yml        # For local Docker development
└── README.md                 # This file
```

## Engineering Roadmap

Phase 1: Caching (Redis)

To optimize data retrieval latencies and mitigate cloud hosting constraints, a Redis caching layer is being integrated. This will significantly reduce database read loads for frequent queries like global search results and popular track metadata.

Phase 2: Analytics & User Metrics

The database currently tracks granular playback data (listen counts), but doesn't do anything with them. This phase will introduce a data-driven metrics engine to aggregate these metrics, enabling dynamic features like "Trending Weekly Tracks" and personalized user dashboard statistics.

Phase 3: Advanced Metadata Querying (Genre Indexing)

Updating the dataloader to read genre tags and expanding the PostgreSQL schema to support dynamic genre tagging and multi-faceted search filter mechanics, making it easier for users to discover new music.

Phase 4: Social Graph & Collaborative Features

Expanding the social functionality to allow for user messaging and shared playlists.

## Troubleshooting

*   **"Music root directory NOT FOUND"**:
    *   Ensure Docker Desktop is running.
    *   Verify that the `sample-music` directory is physically located at `ArchivePlayer-backend/sample-music/`.
    *   Check Docker Desktop's File Sharing settings (Settings -> Resources -> File Sharing) to ensure the drive containing your project (e.g., `C:` on Windows) is shared with Docker. Apply and restart Docker Desktop if changes are made.
    *   Run `docker-compose down --volumes --remove-orphans && docker-compose up --build` for a clean restart.
*   **CORS Errors**:
    *   Ensure `docker-compose.yml` has `ALLOWED_ORIGINS: http://localhost:3000,http://localhost:8080` for the backend.
    *   Ensure `ArchivePlayer-frontend/Dockerfile` correctly passes `VITE_API_URL` as a build arg and uses `envsubst` for `NGINX_BACKEND_HOST`.
    *   Ensure `ArchivePlayer-frontend/nginx.conf` has the `http` and `events` blocks, and the `/api/` proxy_pass.

---
