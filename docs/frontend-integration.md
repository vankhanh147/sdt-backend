# Frontend API Contract

This document describes the API implemented in `sdt-backend` for frontend integration.

## Base URL and conventions

- Local base URL: `http://localhost:8080`
- All JSON uses `camelCase`.
- UUID values are strings.
- `OffsetDateTime` values use ISO 8601 with an offset, for example `2026-08-11T10:30:00+07:00`.
- `LocalDate` values use `yyyy-MM-dd`.
- Enum values are case-sensitive strings.
- There is currently no authentication endpoint or API authentication.
- The backend has no CORS configuration. When the frontend uses a different local origin, configure a development proxy for `/api` or add CORS in the backend.

### Enums

| Enum | Values |
| --- | --- |
| `SourceType` | `ZALO`, `WEBSITE`, `EMAIL`, `MANUAL`, `OTHER` |
| `FeedbackStatus` | `PENDING_ANALYSIS`, `ANALYZED`, `IN_PROGRESS`, `RESOLVED`, `REJECTED`, `ANALYSIS_FAILED` |
| `SentimentType` | `POSITIVE`, `NEUTRAL`, `NEGATIVE` |
| `PriorityLevel` | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |
| `RawProcessingStatus` | `NEW`, `PROCESSING`, `PROCESSED`, `FAILED` |
| `AnalysisStatus` | `PENDING`, `SUCCESS`, `FAILED` |
| `TrendInterval` | `DAY`, `MONTH` |

## Shared response shapes

```ts
type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

type ErrorResponse = {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors: Record<string, string> | null;
};

type FeedbackListItem = {
  id: string;
  title: string | null;
  content: string;
  authorName: string | null;
  location: string | null;
  category: string | null;
  status: FeedbackStatus;
  source: SourceType;
  receivedAt: string;
  sentiment: SentimentType | null;
  sentimentScore: number | null;
  priority: PriorityLevel | null;
  priorityScore: number | null;
  createdAt: string;
};

type RawFeedback = {
  id: string;
  source: SourceType;
  sourceRef: string;
  rawTitle: string | null;
  rawContent: string;
  rawAuthorName: string | null;
  rawAuthorContact: string | null;
  rawLocation: string | null;
  categoryHint: string | null;
  rawMetadata: Record<string, unknown> | null;
  receivedAt: string;
  processingStatus: RawProcessingStatus;
  processedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

type AnalysisResult = {
  id: string;
  sentiment: SentimentType | null;
  sentimentScore: number | null;
  category: string | null;
  categoryScore: number | null;
  matchedKeywords: string[] | null;
  priority: PriorityLevel | null;
  priorityScore: number | null;
  priorityReason: string | null;
  modelName: string | null;
  modelVersion: string | null;
  analysisStatus: AnalysisStatus;
  errorMessage: string | null;
  analyzedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

type FeedbackDetail = {
  id: string;
  title: string;
  content: string;
  authorName: string | null;
  authorContact: string | null;
  location: string | null;
  category: string | null;
  status: FeedbackStatus;
  createdAt: string;
  updatedAt: string;
  resolvedAt: string | null;
  rawFeedback: RawFeedback;
  latestAnalysis: AnalysisResult | null;
  analysisHistory: AnalysisResult[];
};

type Category = {
  id: string;
  code: string;
  name: string;
  description: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
};
```

`number` above represents JSON numbers, including Java `BigDecimal` and `long` values.

## Feedback APIs

### List feedback

`GET /api/feedback` → `200 PageResponse<FeedbackListItem>`

| Query parameter | Type | Default / constraint |
| --- | --- | --- |
| `page` | integer | `0`; must be at least `0` |
| `size` | integer | `20`; from `1` to `100` |
| `sortBy` | string | `createdAt`; allowed: `createdAt`, `updatedAt`, `title`, `status`, `category`; another value falls back to `createdAt` |
| `sortDirection` | `asc` \| `desc` | `desc`; another nonblank value returns `400` |
| `source` | `SourceType` | optional |
| `status` | `FeedbackStatus` | optional |
| `category` | string | case-insensitive exact match |
| `sentiment` | `SentimentType` | filters the latest analysis |
| `priority` | `PriorityLevel` | filters the latest analysis |
| `keyword` | string | case-insensitive title/content search |
| `fromDate` | `OffsetDateTime` | inclusive `createdAt` lower bound |
| `toDate` | `OffsetDateTime` | inclusive `createdAt` upper bound |

`fromDate` after `toDate` returns `400`.

### Get feedback detail

`GET /api/feedback/{id}` → `200 FeedbackDetail`

Returns `404` when the feedback does not exist. `latestAnalysis` is `null` when no analysis exists; `analysisHistory` is newest first.

### Update feedback

`PATCH /api/feedback/{id}` → `200 FeedbackDetail`

```ts
type FeedbackUpdateRequest = {
  title?: string;           // max 500, nonblank when supplied
  content?: string;         // nonblank when supplied
  authorName?: string;      // max 255, nonblank when supplied
  authorContact?: string;   // max 255, nonblank when supplied
  location?: string;        // max 500, nonblank when supplied
  category?: string;        // max 100, nonblank when supplied
  status?: FeedbackStatus;
};
```

Send at least one field. Strings are trimmed. Setting `status` to `RESOLVED` sets `resolvedAt`; changing from `RESOLVED` to another status clears it. `category` is a name string, not a category ID. Empty/blank updates return `400`; an unknown ID returns `404`.

### Delete feedback

`DELETE /api/feedback/{id}` → `204 No Content`

Returns `404` when the feedback does not exist.

### Ingest raw feedback

`POST /api/feedback/ingest` → `201 FeedbackIngestResponse`

```ts
type FeedbackIngestRequest = {
  source: SourceType;
  sourceRef: string;                // required, max 255
  rawTitle?: string | null;         // max 500
  rawContent: string;               // required
  rawAuthorName?: string | null;    // max 255
  rawAuthorContact?: string | null; // max 255
  rawLocation?: string | null;      // max 500
  categoryHint?: string | null;     // max 100
  rawMetadata?: Record<string, unknown> | null;
  receivedAt: string;
};

type FeedbackIngestResponse = {
  id: string;
  source: SourceType;
  sourceRef: string;
  processingStatus: RawProcessingStatus; // initially NEW
  receivedAt: string;
  createdAt: string;
};
```

`source`, `sourceRef`, `rawContent`, and `receivedAt` are required. The `source`/`sourceRef` pair must be unique; duplicates return `409`.

## Category APIs

| Method | Path | Request body | Success response |
| --- | --- | --- | --- |
| `POST` | `/api/categories` | `CategoryCreateRequest` | `201 Category` |
| `GET` | `/api/categories?activeOnly=false` | — | `200 Category[]` |
| `GET` | `/api/categories/{id}` | — | `200 Category` |
| `PATCH` | `/api/categories/{id}` | `CategoryUpdateRequest` | `200 Category` |
| `DELETE` | `/api/categories/{id}` | — | `204 No Content` |
| `PATCH` | `/api/categories/{id}/status` | `CategoryStatusUpdateRequest` | `200 Category` |

```ts
type CategoryCreateRequest = {
  code: string;                   // required, max 50, /^[A-Za-z0-9_]+$/
  name: string;                   // required, max 100
  description?: string | null;    // max 500
};

type CategoryUpdateRequest = {
  name?: string;                  // max 100, nonblank when supplied
  description?: string;           // max 500, nonblank when supplied
};

type CategoryStatusUpdateRequest = {
  active: boolean;
};
```

Codes are trimmed and stored uppercase. Names are trimmed. A new category is active by default. `DELETE` deactivates the category; it does not remove it. Category code/name conflicts return `409`; missing categories return `404`; empty or blank `PATCH` fields return `400`.

## Dashboard APIs

### Statistics

`GET /api/dashboard/stats` → `200`

```ts
type DashboardStats = {
  totalFeedback: number;
  status: {
    pendingAnalysis: number;
    analyzed: number;
    inProgress: number;
    resolved: number;
    rejected: number;
    analysisFailed: number;
  };
  sentiment: { positive: number; neutral: number; negative: number };
  priority: { low: number; medium: number; high: number; urgent: number };
};
```

### Distribution

`GET /api/dashboard/distribution` → `200`

```ts
type DistributionItem = { key: string; label: string; count: number };

type DashboardDistribution = {
  sentiment: DistributionItem[];
  priority: DistributionItem[];
  category: DistributionItem[];
  source: DistributionItem[];
};
```

Enum distributions include zero-count values. Their `key` and `label` are the enum name.

### Trend

`GET /api/dashboard/trend` → `200`

| Query parameter | Type | Default |
| --- | --- | --- |
| `fromDate` | `LocalDate` | 29 days before `toDate` |
| `toDate` | `LocalDate` | today in `Asia/Bangkok` |
| `interval` | `DAY` \| `MONTH` | `DAY` |

```ts
type DashboardTrend = {
  fromDate: string;
  toDate: string;
  interval: TrendInterval;
  points: Array<{ period: string; count: number }>;
};
```

Missing periods are returned with `count: 0`. The maximum range is 366 daily points or 120 monthly points. An invalid/reversed range returns `400`.

## CSV export

`GET /api/export` → `200 text/csv;charset=UTF-8`

Accepts the same filters as `GET /api/feedback`: `source`, `status`, `category`, `sentiment`, `priority`, `keyword`, `fromDate`, and `toDate`. Pagination and sorting parameters do not affect the exported rows.

- The response has `Content-Disposition: attachment; filename="feedback-export-YYYYMMDD-HHmmss.csv"`.
- The CSV is UTF-8 with a BOM and has `X-Content-Type-Options: nosniff`.
- The header order is: `id`, `title`, `content`, `authorName`, `authorContact`, `location`, `category`, `status`, `source`, `receivedAt`, `sentiment`, `sentimentScore`, `priority`, `priorityScore`, `createdAt`, `updatedAt`, `resolvedAt`.
- An export over 50,000 rows returns `413 ErrorResponse`.
- Treat a successful export as a `Blob`, not JSON.

```ts
const response = await fetch(`${baseUrl}/api/export?status=ANALYZED`);
if (!response.ok) throw (await response.json()) as ErrorResponse;
const csv = await response.blob();
```

## Error handling

Backend errors normally return `ErrorResponse`.

| Status | Meaning |
| --- | --- |
| `400` | Invalid JSON, UUID/enum/date parameter, request validation, empty/blank update, or invalid filter/range |
| `404` | Feedback or category not found |
| `409` | Duplicate category/feedback or database constraint conflict |
| `413` | Export exceeds 50,000 rows |
| `500` | Unexpected server error |

There is no implemented `POST /api/feedback/{id}/analyze` endpoint. Do not call it from the frontend.
