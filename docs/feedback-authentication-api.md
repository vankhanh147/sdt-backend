# Feedback and Authentication API

This is the implemented contract for `sdt-backend`. The local base URL is `http://localhost:8080`; routes use the `/api` prefix. JSON uses `camelCase`, UUIDs are strings, and date-times are ISO-8601 values with an offset.

## Authentication

The API uses stateless JWT bearer tokens. Sign in, then send the returned token with every protected request:

```http
Authorization: Bearer <accessToken>
```

| Method | Path | Access | Success |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Public | `200 OK` |
| `GET` | `/api/auth/me` | Signed-in user | `200 OK` |

### Sign in

`POST /api/auth/login`

```json
{ "username": "admin", "password": "your-password" }
```

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "6f6790a0-80bc-4f62-b556-4657ea4be609",
    "username": "admin",
    "role": "ADMIN"
  }
}
```

`username` and `password` are required. Invalid credentials or an inactive account return `401`.

### Development seed account

For local development, run [002_seed_development_admin.sql](../scripts/auth/002_seed_development_admin.sql) after the app-user table migration. It creates this idempotent account:

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `Admin@123` | `ADMIN` |

Fresh databases created through `sdt-backend/compose.yaml` receive the same development account from `docker/postgres/init/003-auth.sql`. Never use this default password outside local development.

### Current user

`GET /api/auth/me` returns the user `id`, `username`, and `role`. Roles are `USER` and `ADMIN`. Missing, expired, or invalid tokens return `401`; a valid token without the required role returns `403`.

## Feedback routes

| Method | Path | Access | Success | Purpose |
| --- | --- | --- | --- | --- |
| `GET` | `/api/feedback` | Signed-in user | `200` | List/filter feedback |
| `POST` | `/api/feedback` | Signed-in user | `201` | Create manual feedback |
| `GET` | `/api/feedback/{id}` | Signed-in user | `200` | Get feedback detail |
| `PATCH` | `/api/feedback/{id}` | `ADMIN` | `200` | Update feedback |
| `DELETE` | `/api/feedback/{id}` | `ADMIN` | `204` | Delete feedback |
| `POST` | `/api/feedback/ingest` | `ADMIN` | `201` | Store raw external feedback |
| `GET` | `/api/feedback/{id}/attachments` | Signed-in user | `200` | List image attachments |
| `POST` | `/api/feedback/{id}/attachments` | Signed-in user | `201` | Upload an image (`file` form field) |
| `GET` | `/api/feedback/{id}/attachments/{attachmentId}/download` | Signed-in user | `200` | Download/view an image |
| `DELETE` | `/api/feedback/{id}/attachments/{attachmentId}` | `ADMIN` | `204` | Delete an image |

Enums are case-sensitive:

| Enum | Values |
| --- | --- |
| `SourceType` | `ZALO`, `WEBSITE`, `EMAIL`, `MANUAL`, `OTHER` |
| `FeedbackStatus` | `PENDING_ANALYSIS`, `ANALYZED`, `IN_PROGRESS`, `RESOLVED`, `REJECTED`, `ANALYSIS_FAILED` |
| `SentimentType` | `POSITIVE`, `NEUTRAL`, `NEGATIVE` |
| `PriorityLevel` | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |

### List feedback

`GET /api/feedback`

Optional query parameters: `page` (minimum `0`, default `0`), `size` (1–100, default `20`), `sortBy` (`createdAt`, `updatedAt`, `title`, `status`, or `category`), `sortDirection` (`asc` or `desc`), `source`, `status`, `category`, `sentiment`, `priority`, `keyword`, `fromDate`, and `toDate`.

`keyword` searches title/content. Dates are inclusive `createdAt` bounds, and `fromDate` cannot be later than `toDate`.

```json
{
  "content": [{
    "id": "6f6790a0-80bc-4f62-b556-4657ea4be609",
    "title": "Road damage",
    "content": "There is a large pothole.",
    "authorName": "Nguyen Van A",
    "location": "Ho Chi Minh City",
    "category": "Traffic",
    "status": "ANALYZED",
    "source": "WEBSITE",
    "receivedAt": "2026-08-20T10:30:00+07:00",
    "sentiment": "NEGATIVE",
    "sentimentScore": 0.94,
    "priority": "HIGH",
    "priorityScore": 89,
    "createdAt": "2026-08-20T10:31:00+07:00"
  }],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Create manual feedback

`POST /api/feedback`

```json
{
  "title": "Road damage",
  "content": "There is a large pothole near the intersection.",
  "authorName": "Nguyen Van A",
  "authorContact": "0900000000",
  "location": "Ho Chi Minh City",
  "category": "Traffic",
  "receivedAt": "2026-08-20T10:30:00+07:00"
}
```

Only `content` is required. Optional text fields are trimmed; blank optional values become `null`. If `receivedAt` is omitted, the current time is used. The response is `201` with the new feedback ID, `PENDING_ANALYSIS` status, and `MANUAL` source.

### Get, update, and delete

`GET /api/feedback/{id}` returns the feedback, its raw source, latest analysis, and analysis history.

`PATCH /api/feedback/{id}` accepts one or more of `title`, `content`, `authorName`, `authorContact`, `location`, `category`, and `status`. Supplied text values must be nonblank. Setting `status` to `RESOLVED` sets `resolvedAt`; changing it to another status clears it. The response is the updated detail.

`DELETE /api/feedback/{id}` permanently deletes the feedback and returns `204 No Content`.

### Ingest external feedback

`POST /api/feedback/ingest`

```json
{
  "source": "WEBSITE",
  "sourceRef": "web-20260820-001",
  "rawTitle": "Road damage",
  "rawContent": "There is a large pothole near the intersection.",
  "rawAuthorName": "Nguyen Van A",
  "rawAuthorContact": "0900000000",
  "rawLocation": "Ho Chi Minh City",
  "categoryHint": "Traffic",
  "rawMetadata": { "channel": "contact-form" },
  "receivedAt": "2026-08-20T10:30:00+07:00"
}
```

`source`, `sourceRef`, `rawContent`, and `receivedAt` are required. `(source, sourceRef)` is unique; duplicates return `409`. This route stores raw feedback with `NEW` processing status; processing it into feedback is a separate workflow.

## Errors and browser configuration

Errors use this shape:

```json
{
  "timestamp": "2026-08-20T10:30:00+07:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/feedback",
  "validationErrors": { "size": "must be less than or equal to 100" }
}
```

Typical statuses: `400` invalid request data, `401` missing/invalid credentials, `403` insufficient role, `404` unknown feedback, `409` duplicate/conflicting data, and `500` unexpected failure.

The frontend uses `NEXT_PUBLIC_API_BASE_URL` (default: `http://localhost:8080`). Set `CORS_ALLOWED_ORIGINS` to the comma-separated frontend origins, such as `http://localhost:3000,http://localhost:5173` for local development.
