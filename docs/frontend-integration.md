# Frontend Integration Guide

Tài liệu này mô tả contract hiện có trong backend để frontend tích hợp. Các enum là chuỗi và phân biệt chữ hoa/thường.

## 1. Run backend

- Java: 21.
- Build tool: Maven Wrapper (`mvnw.cmd`).
- Port không được cấu hình riêng, nên Spring Boot dùng mặc định `8080`.
- Base URL local: `http://localhost:8080`.
- Datasource password hiện đang được hard-code trong `application.properties`, chưa dùng `${DB_PASSWORD}`. Secret này không được đưa vào tài liệu frontend, commit hoặc công khai; nên chuyển sang biến môi trường trong một thay đổi cấu hình riêng.

Chạy trên Windows:

```bat
mvnw.cmd spring-boot:run
```

Backend cần kết nối được PostgreSQL khi khởi động vì Hibernate đang dùng `ddl-auto=validate`.

## 2. Common conventions

- Request/response JSON dùng `camelCase`.
- UUID được truyền dưới dạng chuỗi, ví dụ `6f6790a0-80bc-4f62-b556-4657ea4be609`.
- `OffsetDateTime` nên gửi theo ISO 8601 có offset, ví dụ `2026-08-11T10:30:00+07:00`.
- `LocalDate` dùng định dạng `yyyy-MM-dd`.
- Không có authentication trong các controller hiện tại.

Enum hợp lệ:

| Enum | Giá trị |
|---|---|
| `SourceType` | `ZALO`, `WEBSITE`, `EMAIL`, `MANUAL`, `OTHER` |
| `FeedbackStatus` | `PENDING_ANALYSIS`, `ANALYZED`, `IN_PROGRESS`, `RESOLVED`, `REJECTED`, `ANALYSIS_FAILED` |
| `SentimentType` | `POSITIVE`, `NEUTRAL`, `NEGATIVE` |
| `PriorityLevel` | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |
| `TrendInterval` | `DAY`, `MONTH` |

## 3. Feedback APIs

### List and filter feedback

`GET /api/feedback`

Query parameters:

| Parameter | Type | Ghi chú |
|---|---|---|
| `page` | integer | Mặc định `0`, nhỏ nhất `0` |
| `size` | integer | Mặc định `20`, từ `1` đến `100` |
| `sortBy` | string | Cho phép `createdAt`, `updatedAt`, `title`, `status`, `category`; giá trị khác dùng `createdAt` |
| `sortDirection` | string | `asc` hoặc `desc`; mặc định `desc` |
| `source` | `SourceType` | Lọc theo nguồn |
| `status` | `FeedbackStatus` | Lọc theo trạng thái |
| `category` | string | So khớp category không phân biệt hoa/thường |
| `sentiment` | `SentimentType` | Dựa trên analysis mới nhất |
| `priority` | `PriorityLevel` | Dựa trên analysis mới nhất |
| `keyword` | string | Tìm trong title hoặc content, không phân biệt hoa/thường |
| `fromDate` | `OffsetDateTime` | `createdAt >= fromDate` |
| `toDate` | `OffsetDateTime` | `createdAt <= toDate` |

Response là page object:

```json
{
  "content": [
    {
      "id": "uuid",
      "title": "string",
      "content": "string",
      "authorName": "string",
      "location": "string",
      "category": "string",
      "status": "ANALYZED",
      "source": "WEBSITE",
      "receivedAt": "2026-08-11T10:30:00+07:00",
      "sentiment": "POSITIVE",
      "sentimentScore": 0.95,
      "priority": "HIGH",
      "priorityScore": 80,
      "createdAt": "2026-08-11T10:31:00+07:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

Các field analysis có thể là `null` nếu feedback chưa có AnalysisResult.

### Feedback detail

`GET /api/feedback/{id}`

Response chính gồm `id`, `title`, `content`, `authorName`, `authorContact`, `location`, `category`, `status`, `createdAt`, `updatedAt`, `resolvedAt`, cùng:

- `rawFeedback`: dữ liệu nguồn, metadata và processing status.
- `latestAnalysis`: AnalysisResult mới nhất hoặc `null`.
- `analysisHistory`: danh sách AnalysisResult, mới nhất trước.

AnalysisResult gồm sentiment/category/priority và score tương ứng, `matchedKeywords`, thông tin model, trạng thái analysis và các timestamp.

### Update feedback

`PATCH /api/feedback/{id}`

Mọi field đều tùy chọn; chỉ gửi field cần đổi:

```json
{
  "title": "Tiêu đề mới",
  "content": "Nội dung mới",
  "authorName": "Nguyễn Văn A",
  "authorContact": "contact@example.com",
  "location": "Hà Nội",
  "category": "Giao thông",
  "status": "IN_PROGRESS"
}
```

Giới hạn: `title` 500, `authorName`/`authorContact` 255, `location` 500, `category` 100 ký tự. Response là `FeedbackDetailResponse`.

`Feedback.category` hiện là `String` và chưa liên kết khóa ngoại với bảng `Category`. Khi PATCH feedback, frontend gửi tên category, ví dụ `{ "category": "Giao thông" }`, không gửi `categoryId`.

### Delete feedback

`DELETE /api/feedback/{id}` trả `204 No Content` khi thành công.

### Ingest raw feedback

`POST /api/feedback/ingest` trả `201 Created`.

```json
{
  "source": "WEBSITE",
  "sourceRef": "external-id-001",
  "rawTitle": "Tiêu đề",
  "rawContent": "Nội dung phản ánh",
  "rawAuthorName": "Nguyễn Văn A",
  "rawAuthorContact": "contact@example.com",
  "rawLocation": "Hà Nội",
  "categoryHint": "Giao thông",
  "rawMetadata": { "channel": "web" },
  "receivedAt": "2026-08-11T10:30:00+07:00"
}
```

`source`, `sourceRef`, `rawContent`, `receivedAt` là bắt buộc. Response gồm `id`, `source`, `sourceRef`, `processingStatus`, `receivedAt`, `createdAt`.

## 4. Category APIs

| Method | Path | Mục đích | Response |
|---|---|---|---|
| `POST` | `/api/categories` | Tạo category | `201 CategoryResponse` |
| `GET` | `/api/categories?activeOnly=false` | Danh sách; đặt `true` để chỉ lấy active | `CategoryResponse[]` |
| `GET` | `/api/categories/{id}` | Chi tiết category | `CategoryResponse` |
| `PATCH` | `/api/categories/{id}` | Đổi name/description | `CategoryResponse` |
| `DELETE` | `/api/categories/{id}` | Deactivate category | `204` |
| `PATCH` | `/api/categories/{id}/status` | Bật/tắt category | `CategoryResponse` |

Tạo category:

```json
{ "code": "GIAO_THONG", "name": "Giao thông", "description": "Mô tả" }
```

`code` bắt buộc, tối đa 50 ký tự, chỉ gồm chữ Latin, số và `_`. `name` bắt buộc, tối đa 100; `description` tối đa 500.

Update thông tin: `{ "name": "Tên mới", "description": "Mô tả mới" }`.

Update trạng thái: `{ "active": true }`; `active` là bắt buộc.

`CategoryResponse` gồm `id`, `code`, `name`, `description`, `isActive`, `createdAt`, `updatedAt`.

## 5. Dashboard APIs

| Method | Path | Kết quả |
|---|---|---|
| `GET` | `/api/dashboard/stats` | Tổng feedback; count theo status, sentiment và priority |
| `GET` | `/api/dashboard/distribution` | Các mảng `sentiment`, `priority`, `category`, `source` |
| `GET` | `/api/dashboard/trend` | Chuỗi thời gian feedback |

`stats` response:

```json
{
  "totalFeedback": 30,
  "status": { "pendingAnalysis": 3, "analyzed": 27, "inProgress": 0, "resolved": 0, "rejected": 0, "analysisFailed": 0 },
  "sentiment": { "positive": 9, "neutral": 9, "negative": 9 },
  "priority": { "low": 9, "medium": 10, "high": 5, "urgent": 3 }
}
```

Mỗi item distribution có dạng `{ "key": "POSITIVE", "label": "POSITIVE", "count": 9 }`.

Trend nhận `fromDate`, `toDate` dạng `yyyy-MM-dd` và `interval=DAY|MONTH`. Mặc định là 30 ngày đến hôm nay, interval `DAY`, theo múi giờ `Asia/Bangkok`. Response gồm `fromDate`, `toDate`, `interval`, `points`; mỗi point là `{ "period": "2026-08-11", "count": 3 }`.

Giới hạn trend: tối đa 366 điểm ngày hoặc 120 điểm tháng.

## 6. Export CSV

`GET /api/export` nhận các filter `source`, `status`, `category`, `sentiment`, `priority`, `keyword`, `fromDate`, `toDate`. `page`, `size` và sort không điều khiển nội dung export.

Response là file CSV UTF-8 có BOM, **không phải JSON**. Browser có thể tải bằng blob:

```js
const response = await fetch(`${baseUrl}/api/export?status=ANALYZED`);
if (!response.ok) throw await response.json();
const blob = await response.blob();
```

Tên file lấy từ header `Content-Disposition`. Export tối đa 50.000 feedback; vượt giới hạn trả `413` theo error format bên dưới.

## 7. Error response

Lỗi JSON có cấu trúc:

```json
{
  "timestamp": "2026-08-11T10:30:00+07:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/feedback",
  "validationErrors": { "size": "must be less than or equal to 100" }
}
```

`validationErrors` có thể là `null`. Status cần xử lý: `400` request/filter sai, `404` không tìm thấy, `409` trùng hoặc vi phạm constraint, `413` export quá giới hạn, `500` lỗi không mong đợi. JSON/enum sai định dạng trả `400` với message `Malformed JSON or invalid field value`.

## 8. CORS and current limitations

- Code hiện tại không có `@CrossOrigin` hoặc CORS configuration. Frontend chạy ở origin khác (ví dụ dev server port 3000/5173) sẽ bị browser chặn nếu không dùng dev proxy hoặc backend chưa bổ sung CORS.
- Nên cấu hình dev proxy `/api` tới `http://localhost:8080` trong frontend khi phát triển local.
- `POST /api/feedback/{id}/analyze` **chưa được implement**; frontend không được gọi endpoint này.
- Chưa có endpoint phục vụ authentication.
- Export trả stream/file; chỉ parse JSON khi response export không thành công.
