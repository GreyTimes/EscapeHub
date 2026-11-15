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
Java entity classes.

Business rules and validation belong to the backend.

The frontend must display the state provided by the backend.
