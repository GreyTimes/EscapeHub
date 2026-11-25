# EscapeHub Demo Guide

This guide explains how to start EscapeHub and demonstrate the Blackwood Case mission in Solo Mode and Team Mode.

## 1. Requirements

Install the following:

- JDK 25
- Node.js 20.19+ or 22.12+
- npm
- Docker Desktop with Docker Compose

## 2. Environment Setup

Open PowerShell in the repository root.

If you do not already have a `.env` file, create one:

```powershell
Copy-Item .env.example .env
```

Make sure the root `.env` contains the following configuration (keep your existing working password):

```properties
POSTGRES_DB=escapehub
POSTGRES_USER=escapehub
POSTGRES_PASSWORD=your_local_password
POSTGRES_PORT=5432

VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_DEV_USER_ID=00000000-0000-4000-8000-000000000001
DEV_USER_DISPLAY_NAME=Local Developer
```

Do not commit the real `.env` file to GitHub.

## 3. Start PostgreSQL

From the repository root, run:

```powershell
docker compose up -d
```

Check its status:

```powershell
docker compose ps
```

Wait until PostgreSQL displays `healthy`.

PostgreSQL uses a persistent Docker volume. Normal container restarts do not delete stored data.

**Do not run `docker compose down -v` during the demo**, because this deletes the database volume.

## 4. Start the Backend

Open a PowerShell terminal in the repository root:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev"
```

The development profile automatically:

- Creates or updates the configured development user.
- Connects to PostgreSQL.
- Imports the Blackwood Case mission from its JSON definition.

Keep this terminal running.

## 5. Start the Frontend

Open a second PowerShell terminal:

```powershell
cd frontend
npm ci
npm run dev
```

Open the application:

`http://localhost:5173`

The application should display a connected backend status and the mission catalogue.

---

# Solo Mode Demonstration

## 6. Start a Mission

1. Open the mission catalogue.
2. Select **The Blackwood Case**.
3. Select **Solo**.
4. Wait for the Active Mission Session screen.

The player starts in Central Archive.

## 7. Explore the Map

The mission contains four rooms:

| Room | Description |
|---|---|
| Central Archive | Starting room |
| Old Library | Contains the Catalogue Card |
| Curator Office | Contains the Signed Note |
| Hidden Vault | Contains the final puzzle |

The connections are:

- Central Archive connects to Old Library.
- Central Archive connects to Curator Office.
- Old Library connects to Hidden Vault.

Players can move only to directly connected rooms.

## 8. Discover Evidence

There are two evidence entries:

**Old Library — Catalogue Card**

The card records that the Blackwood collection was catalogued in 1894.

**Curator Office — Signed Note**

The note provides the surname Blackwood.

To discover evidence:

1. Move into the room containing the evidence.
2. Find the Available Evidence section.
3. Select Examine.
4. Verify that the evidence disappears from the available list.
5. Check the Evidence Journal.

The journal records the evidence title, description, discovering player and timestamp.

## 9. Solve the Puzzles

The sample mission contains four puzzles:

| Room | Puzzle | Expected Answer |
|---|---|---|
| Central Archive | Archive Code | `B` |
| Old Library | Investigation Puzzle | `1894` |
| Curator Office | Curator Name | `Blackwood` |
| Hidden Vault | Final Puzzle | `1894-BLACKWOOD` |

The player can explore both investigation paths, collect the evidence and solve the corresponding puzzles.

The final answer combines the collection year and the curator's surname.

After submitting the correct final answer, the mission should be marked as completed.

---

# Team Mode Demonstration

## 10. Prepare the Second User

The development backend automatically creates the default development user specified in the root `.env`.

For a second user, first ensure the database contains this user.

Open the PostgreSQL console:

```powershell
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Execute:

```sql
INSERT INTO users (
    id,
    display_name,
    role,
    created_at
)
VALUES (
    '00000000-0000-4000-8000-000000000002',
    'Player Two',
    'PLAYER',
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO UPDATE
SET
    display_name = EXCLUDED.display_name,
    role = EXCLUDED.role;
```

Verify both users:

```sql
SELECT id, display_name, role
FROM users
WHERE id IN (
    '00000000-0000-4000-8000-000000000001',
    '00000000-0000-4000-8000-000000000002'
);
```

You should see Local Developer and Player Two.

Exit PostgreSQL:

```sql
\q
```

This database insertion is only necessary for the second development user. The first user is automatically created by the development initializer.

## 11. Start Player One

Player One uses the default configuration in the root `.env`.

Open the frontend normally:

```powershell
cd frontend
npm run dev
```

Open:

`http://localhost:5173`

## 12. Start Player Two

Open another PowerShell terminal.

Run:

```powershell
cd frontend

$env:VITE_API_BASE_URL = "http://localhost:8080/api/v1"
$env:VITE_DEV_USER_ID = "00000000-0000-4000-8000-000000000002"

npm run dev -- --host localhost --port 5174 --strictPort
```

Open the second frontend in an incognito browser window:

`http://localhost:5174`

Player One uses the UUID ending in `0001`.

Player Two uses the UUID ending in `0002`.

The frontend sends the development identity through the `X-Dev-User-Id` HTTP header.

**Important:** Both users must have different IDs. Setting the same ID in both browser instances means the backend treats them as the same user.

The development identity mechanism is for local testing only. It is not production authentication.

## 13. Create and Join a Team

**Player One — Team Leader**

1. Select The Blackwood Case.
2. Choose Team.
3. Select Create New Team.
4. Copy the invitation code displayed by EscapeHub.

**Player Two — Teammate**

1. Select The Blackwood Case.
2. Choose Team.
3. Enter the invitation code received from Player One.
4. Select Join Existing Team.

The application should automatically find the team's waiting lobby and add Player Two.

## 14. Start the Team Mission

1. Player One refreshes the lobby member list.
2. Verify that both players are displayed.
3. Player One selects Start Mission.
4. Both players enter the same team mission session.

Only the team leader can start the mission.

At least two participants are required.

## 15. Test Shared Evidence

1. Either player enters a room containing undiscovered evidence.
2. The player selects Examine.
3. The evidence is saved in the shared Evidence Journal.
4. Open the other player's browser.
5. Refresh the Evidence Journal.
6. Verify that the discovery is visible there.
7. Verify that the journal identifies the player who discovered it.

The current implementation uses manual refresh to retrieve updated information. Automatic real-time synchronization has not been implemented.

---

# Database Persistence Verification

PostgreSQL stores the application data in a persistent Docker volume.

Open the PostgreSQL console:

```powershell
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

List the database tables:

```sql
\dt
```

Check the users:

```sql
SELECT id, display_name, role
FROM users
ORDER BY display_name;
```

Check teams and their members:

```sql
SELECT
    t.name,
    t.join_code,
    tm.user_id,
    tm.role
FROM teams t
JOIN team_members tm ON tm.team_id = t.id
ORDER BY t.created_at, tm.joined_at;
```

Exit:

```sql
\q
```

To verify evidence persistence, note the active session ID and discover an evidence item.

Stop and restart the Spring Boot application using `Ctrl+C` and the normal startup command.

Then retrieve the Evidence Journal again:

```powershell
$sessionId = "YOUR_SESSION_ID"

$headers = @{
    "X-Dev-User-Id" = "00000000-0000-4000-8000-000000000001"
}

$journalUri = "http://localhost:8080/api/v1/sessions/$sessionId/evidence"

Invoke-RestMethod -Uri $journalUri -Headers $headers
```

The previously discovered evidence should still appear.

This verifies that the discovery was persisted in PostgreSQL rather than stored only in frontend state.

Do not delete the Docker volume during this test.

---

# Troubleshooting

## Backend cannot connect to PostgreSQL

Check the container:

```powershell
docker compose ps
```

Check the database settings in the root `.env`.

## Frontend cannot connect to the backend

Verify:

- Spring Boot is running on port 8080.
- `VITE_API_BASE_URL` points to `http://localhost:8080/api/v1`.
- The development CORS configuration allows the frontend origin.

## Player Two cannot join a team

Check:

- Player Two exists in the `users` table.
- The second frontend sends the UUID ending in `0002`.
- Player One and Player Two use different IDs.
- The invitation code is correct.
- Neither player has another active mission.

## Team mission cannot start

At least two participants must have joined the waiting lobby.

Only the team leader can start the mission.

## PostgreSQL data disappears

Avoid running:

```powershell
docker compose down -v
```

The `-v` option deletes the persistent database volume.

---

# Demo Completion Checklist

The demo is successful when:

- The Blackwood Case appears in the mission catalogue.
- A player can start a Solo Session.
- Players can explore the map and move between connected rooms.
- Evidence can be discovered and added to the journal.
- Puzzles can be solved and the mission completed.
- Two different users can create and join a team.
- The team leader can start a shared Team Session.
- Both players can access the shared Evidence Journal.
- The journal records who discovered each evidence item.
- The relevant data remains stored in PostgreSQL.
