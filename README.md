# Data Ingestion API - Spring Boot

A production-ready REST API for validating, transforming, and ingesting CSV data (stores, users, and store-user mappings) into a SQL database with intelligent error reporting and batch processing optimization.

## 📋 Project Overview

This Spring Boot application provides three endpoints to upload and process CSV files for a retail platform:

- **POST /api/upload/stores** - Upload store master data (100-500K rows)
- **POST /api/upload/users** - Upload user master data (30+ rows)
- **POST /api/upload/mappings** - Upload store-user mappings (150+ rows)

**Key Features:**
- ✅ Two-pass validation (format → reference validation)
- ✅ Smart error reporting (row number, column, specific reason)
- ✅ Get-or-create lookup tables (case-insensitive deduplication)
- ✅ Batch insert optimization (estimated 10-15 seconds for 500K rows)
- ✅ PARTIAL_SUCCESS policy (skip bad rows, ingest valid ones)
- ✅ Foreign key resolution with transactional integrity

## 🛠 Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 4.0.5 |
| Language | Java 21 |
| ORM | Spring Data JPA + Hibernate |
| Database (Dev) | H2 In-Memory |
| Database (Prod) | PostgreSQL |
| CSV Parsing | Apache Commons CSV 1.10 |
| Build Tool | Maven |
| Port | 9090 |

## 📦 Project Structure

```
src/main/java/com/infilect/assignment/
├── controller/
│   └── UploadController.java         (3 POST endpoints + health check)
├── service/
│   ├── StoreUploadService.java       (Stores CSV orchestration)
│   ├── UserUploadService.java        (Users CSV orchestration)
│   └── MappingUploadService.java     (Mappings CSV orchestration)
├── repository/
│   ├── StoreRepository.java
│   ├── UserRepository.java
│   ├── PermanentJourneyPlanRepository.java
│   └── 6 Lookup table repositories
├── entity/
│   ├── Store.java                   (Main entity)
│   ├── User.java                    (User entity with self-reference FK)
│   ├── PermanentJourneyPlan.java    (Store-user mapping)
│   └── 6 Lookup tables (Brand, Type, City, State, Country, Region)
├── dto/
│   ├── StoreCSVRow.java
│   ├── UserCSVRow.java
│   ├── MappingCSVRow.java
│   ├── UploadResponse.java
│   ├── RowError.java
│   └── ValidationError.java
└── validator/
    └── ValidationUtils.java         (Email regex, phone format, etc.)
```

## 🚀 Setup & Installation

### Prerequisites
- Java 21+
- Maven 3.8+
- Git

### Step 1: Clone Repository
```bash
git clone git@github.com:SwayamShahu/Assignmrnt.git
cd assignment
```

### Step 2: Build Project
```bash
mvn clean package -DskipTests
```

### Step 3: Run Application
```bash
java -jar target/assignment-0.0.1-SNAPSHOT.jar
```

Expected output:
```
Tomcat started on port(s): 9090 (http)
Started Application in X.XXX seconds
```

### Step 4: Verify Health
```bash
curl http://localhost:9090/api/upload/health
# Response: {"message":"Upload service is running","status":"UP"}
```

## 📝 API Endpoints

### 1. Upload Stores
**POST** `/api/upload/stores`

Upload CSV with columns: `store_id, name, title, store_type, brand, city, state, country, region, latitude, longitude, opened_date`

```bash
curl -X POST -F "file=@stores_master.csv" http://localhost:9090/api/upload/stores
```

**Response (PARTIAL_SUCCESS):**
```json
{
  "fileId": "file-1234567890",
  "status": "PARTIAL_SUCCESS",
  "totalRows": 100,
  "successRows": 94,
  "failureRows": 6,
  "processingTimeMs": 5200,
  "errors": [
    {
      "rowNumber": 7,
      "column": "store_id",
      "reason": "Duplicate store_id within file",
      "value": "STR-0004"
    },
    {
      "rowNumber": 50,
      "column": "latitude",
      "reason": "Invalid float format",
      "value": "abc"
    }
  ]
}
```

---

### 2. Upload Users
**POST** `/api/upload/users`

Upload CSV with columns: `username, email, phone, user_type, supervisor_username`

Note: `user_type` must be one of: `1, 2, 3, 7`

```bash
curl -X POST -F "file=@users_master.csv" http://localhost:9090/api/upload/users
```

**Response Example:**
```json
{
  "fileId": "file-9876543210",
  "status": "PARTIAL_SUCCESS",
  "totalRows": 30,
  "successRows": 23,
  "failureRows": 7,
  "processingTimeMs": 42,
  "errors": [
    {
      "rowNumber": 5,
      "column": "email",
      "reason": "Invalid email format",
      "value": "notanemail"
    },
    {
      "rowNumber": 15,
      "column": "user_type",
      "reason": "Invalid enum value. Must be one of: 1, 2, 3, 7",
      "value": "5"
    },
    {
      "rowNumber": 22,
      "column": "supervisor_username",
      "reason": "Supervisor user not found in database",
      "value": "unknown_user"
    }
  ]
}
```

---

### 3. Upload Mappings
**POST** `/api/upload/mappings`

Upload CSV with columns: `username, store_id, date`

**Important:** Users and Stores must be uploaded first!

```bash
curl -X POST -F "file=@store_user_mapping.csv" http://localhost:9090/api/upload/mappings
```

**Response Example:**
```json
{
  "fileId": "file-5555555555",
  "status": "PARTIAL_SUCCESS",
  "totalRows": 150,
  "successRows": 95,
  "failureRows": 55,
  "processingTimeMs": 680,
  "errors": [
    {
      "rowNumber": 3,
      "column": "username",
      "reason": "User not found in database",
      "value": "unknown_user"
    },
    {
      "rowNumber": 45,
      "column": "store_id",
      "reason": "Store not found in database",
      "value": "UNKNOWN-123"
    },
    {
      "rowNumber": 78,
      "column": "date",
      "reason": "Duplicate mapping: (user_id, store_id, date) already exists",
      "value": "2026-04-02"
    }
  ]
}
```

---

### 4. Health Check
**GET** `/api/upload/health`

```bash
curl http://localhost:9090/api/upload/health
# Response: {"message":"Upload service is running","status":"UP"}
```

## ✅ Validation Rules

### Format Validation (Pass 1)
- ✅ Required fields present (no nulls/empty strings)
- ✅ Email format valid (regex: `[a-zA-Z0-9_\-\.]+@[a-zA-Z0-9_\-\.]+\.[a-zA-Z]{2,}`)
- ✅ Phone format valid (10 digits)
- ✅ Enum constraints satisfied (`userType` ∈ {1,2,3,7})
- ✅ Field length constraints (username ≤150, email ≤254, etc.)
- ✅ Duplicate detection within file (storeId, username)

### Reference Validation (Pass 2)
- ✅ Foreign key exists in database (user, store)
- ✅ Self-reference valid (supervisor_username exists)
- ✅ Unique constraint satisfied (user_id, store_id, date)

## 📊 Test Results

### Test File 1: stores_master.csv (100 rows)
```
Total: 100
Success: 94 ✓
Failure: 6 ✗
Time: 5.2 seconds

Errors caught:
  ✓ Duplicate store_id
  ✓ Missing required fields
  ✓ Invalid latitude/longitude
  ✓ Field length exceeded
```

### Test File 2: users_master.csv (30 rows)
```
Total: 30
Success: 23 ✓
Failure: 7 ✗
Time: 42 ms

Errors caught:
  ✓ Duplicate username
  ✓ Invalid email format
  ✓ Invalid userType (must be 1,2,3,7)
  ✓ Invalid phone format
  ✓ Supervisor not found
```

### Test File 3: store_user_mapping.csv (150 rows)
```
Total: 150
Success: 95 ✓
Failure: 55 ✗
Time: 680 ms

Errors caught:
  ✓ User not found (FK)
  ✓ Store not found (FK)
  ✓ Invalid date format
  ✓ Duplicate mapping
```

## ⚡ Performance Strategy for 500K Rows

**Challenge:** Process 500,000 rows fast without memory explosion

**Solution:**
1. **Stream CSV parsing** - Don't load entire file into memory
2. **Chunking** - Process 1,000 rows per batch
3. **Batch insert** - JDBC batch_size: 20 rows per database commit
4. **Connection pooling** - HikariCP (10 connections)
5. **Indexes** - Unique constraints on critical columns

**Expected Performance:**
- ⏱ **10-15 seconds** for 500K rows (estimate)
- 💾 **<500MB** heap memory
- 📈 **Scales linearly** with row count

## 🔄 Data Flow

```
CSV File (stores/users/mappings)
    ↓
Controller receives multipart form-data
    ↓
Service Layer Orchestrator:
  1. CSV Parser (Apache Commons CSV)
  2. Validation (Two-pass: format → FK)
  3. Get-or-Create lookups (case-insensitive)
  4. Batch Insert (JDBC batch_size=20)
  5. Build Response (status + errors)
    ↓
DB: H2 (dev) / PostgreSQL (prod)
    ↓
JSON Response (UploadResponse)
```

## 🗂 Documentation Files

- **APPROACH.md** - Detailed high-level architecture (corrected orchestration)
- **FLOWCHARTS.md** - 7 ASCII flowcharts showing validation, get-or-create, data flow
- **TESTING_GUIDE.md** - Step-by-step Postman/cURL examples
- **SUBMISSION_SUMMARY.md** - Quick reference guide
- **WRITTEN_APPROACH.md** - 1-page template format covering all requirements

## 💡 Key Design Decisions

| Decision | Chosen | Why |
|----------|--------|-----|
| **Failure Policy** | Skip bad rows | 94% data available vs 0% with all-or-nothing |
| **Batch Size** | 20 rows | Balance memory safety vs database round-trips |
| **Lookup Normalization** | Case-insensitive | Prevent "NEW YORK"/"new york" duplicates |
| **Validation Timing** | Two-pass | Catch format errors before costly DB lookups |
| **Error Format** | JSON response | API-first, extensible, real-time feedback |
| **ORM** | JPA/Hibernate | Type-safe, portable, reduces boilerplate |

## 🧪 Testing with cURL

### Upload test files:
```bash
# 1. Upload stores
curl -X POST -F "file=@test_stores.csv" http://localhost:9090/api/upload/stores | jq .

# 2. Upload users
curl -X POST -F "file=@test_users.csv" http://localhost:9090/api/upload/users | jq .

# 3. Upload mappings
curl -X POST -F "file=@test_mappings.csv" http://localhost:9090/api/upload/mappings | jq .
```

### Check database (H2 Console):
```bash
URL: http://localhost:9090/h2-console
JDBC URL: jdbc:h2:mem:testdb
User: sa
Password: (leave blank)
```

## 🔐 Security Considerations

- ✅ SQL injection prevention (JPA parameterized queries)
- ✅ File upload validation (multipart form-data only)
- ✅ Input sanitization (trim, case normalization)
- ✅ Transactional safety (atomic operations per batch)
- ✅ Data validation before persistence (all constraints checked)

## 📈 Future Enhancements

1. **Async Upload** - Background processing for large files
2. **Webhook Notifications** - Alert when upload completes
3. **Partial Retry API** - Re-submit only failed rows
4. **Pagination** - Return errors in pages (1000 at a time)
5. **Metrics Dashboard** - Track success rates by file type
6. **Audit Log** - Who uploaded what, when
7. **Rate Limiting** - Prevent abuse

## 📞 Contact & Support

- **Author:** Swayam Shahu
- **Gmail:** swayamshahu153@gmail.com

## 📄 License

MIT License - See LICENSE file for details

---
