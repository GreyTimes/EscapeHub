# EscapeHub API Contract - Sprint 1

## 1. General conventions

Base URL:

    http://localhost:8080/api/v1

Communication protocol: HTTP REST.

Request and response format: JSON.

Property naming: camelCase.

Resource identifiers: UUID.

## 2. HTTP conventions

GET:
Retrieve resources.

POST:
Create resources.

PUT:
Replace or update resources.

PATCH:
Partially update resources.

DELETE:
Remove resources.

Feature endpoints must be documented by the developer responsible
for the corresponding module.

## 3. Health endpoint

GET /api/v1/health

Checks whether the backend HTTP application is running.

Successful response:

    {
      "status": "ok"
    }

## 4. Development user identification

During local development, the frontend may send:

    X-Dev-User-Id: <UUID>

The backend DevelopmentCurrentUserFilter reads this header
and stores its UUID in the request attribute:

    currentUserId

The mechanism is available only with the Spring profile:

    dev

A missing header is allowed for endpoints that do not require
a current user.

An invalid UUID produces HTTP 400.

IMPORTANT:

This mechanism is NOT authentication.

It must never be treated as a security mechanism or enabled
as a substitute for authentication in production.

## 5. CORS

Development frontend origin:

    http://localhost:5173

Development backend origin:

    http://localhost:8080

CORS is configured only for the development profile.

## 6. Versioning

All Sprint 1 API endpoints must use:

    /api/v1/

Future breaking API changes should use an appropriate version
strategy rather than silently changing existing contracts.

## 7. Cross-module conventions

Use UUIDs when referencing entities owned by other modules.

Avoid unnecessary direct dependencies on another module's
Java gr.escaphub.mission.definition.entity classes.

Business rules and validation belong to the backend.

The frontend must display the state provided by the backend.

## 8. Mission sessions - Solo / Team choice (#24)

Responsible developer: xaritos222

### 8.1 Start a mission

    POST /api/v1/missions/{missionId}/sessions

Requires a current user (X-Dev-User-Id in the dev profile).

Request body:

    {
      "mode": "SOLO",
      "missionVersionId": "<UUID>"
    }

mode is SOLO or TEAM.

SOLO creates a session directly, without a lobby, with the current
user as its only member.

TEAM is not started through this endpoint. Team sessions are started
by the team leader from the team lobby.

Successful response: 201 Created, with header
Location: /api/v1/sessions/{sessionId}

    {
      "id": "<UUID>",
      "missionVersionId": "<UUID>",
      "mode": "SOLO",
      "status": "ACTIVE",
      "teamId": null,
      "startedByUserId": "<UUID>",
      "startedAt": "2026-11-18T19:30:00Z"
    }

Errors (body: {"message": "..."}):

| Status | When |
|---|---|
| 400 | mode or missionVersionId missing, mode not SOLO/TEAM, or mode is TEAM |
| 401 | No current user |
| 409 | The user already takes part in an ACTIVE session |

### 8.2 View a session

    GET /api/v1/sessions/{sessionId}

Returns the same body as above. Only members of the session may view it.

| Status | When |
|---|---|
| 401 | No current user |
| 403 | The user is not a member of the session |
| 404 | The session does not exist |
