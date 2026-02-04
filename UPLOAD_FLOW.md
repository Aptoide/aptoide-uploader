# Aptoide Uploader - Complete Upload Flow Documentation

## Overview

This document provides comprehensive technical documentation for the Aptoide Uploader v7 upload flow, based on the official API specification.

**Base URL:** `https://ws75.aptoide.com/` (also referenced as `http://ws75-primary.aptoide.com/`)

**Key Optimization:** The upload system uses an **MD5-first approach** to avoid unnecessary file uploads. When an APK with a given MD5 hash already exists in Aptoide, the file upload is skipped entirely.

**Architecture Rationale:**
> "Uploading user experience is painful because it is a synchronous process with a bunch of sub-processes. The goal is to change the upload flow in an asynchronous process by splitting the upload process by priorities and dependencies in multiple intermediate v7 endpoints to gather all the info needed before calling the actual uploader v3 webservice."
>
> — From Confluence: Uploader v7 Architecture Design

**Sources:**
- `Uploader_3.0_client.pdf` - Client flow diagrams
- Confluence: [Uploader v7](https://aptoide.atlassian.net/wiki/spaces/AN/pages/1228439682/Uploader+v7) - API specification
- Codebase: `UploadManager.java` and `RetrofitUploadService.java`

---

## Three Upload Scenarios

The upload flow handles three distinct scenarios depending on whether the APK already exists in Aptoide and/or Google Play.

### Scenario A: APK Already Exists in Aptoide (MD5 Match)

**This is the optimal scenario** where MD5-first optimization provides maximum benefit.

#### Flow Diagram
```
1. POST /api/7/uploader/draft/create
2. POST /api/7/uploader/draft/apk/set (MD5 only, NO file)
3. POST /api/7/uploader/draft/status/set (status=PENDING)
4. GET /api/7/uploader/draft/status/get
   → Response: status="FINISHED"
5. ✅ Upload Complete - NO FILE UPLOAD NEEDED!
```

#### Code Path
```java
UploadManager.upload()
  → createDraft()           // Step 1
  → setMd5()                // Step 2: Send MD5 hash only
  → setPending()            // Step 3: Mark as PENDING
  → getDraftStatus()        // Step 4: Server checks MD5
  → Returns: DUPLICATE status (APK-103)
  → handleDraftStatus()     // Returns draft as-is
  → Complete!               // NO uploadFiles() called
```

#### API Details

**Step 1: Create Draft**
```http
POST https://ws75.aptoide.com/api/7/uploader/draft/create
Content-Type: application/json

Request Body:
{
  "package_name": "com.example.app",
  "vercode": 42,
  "apk_md5sum": "abc123..." // Optional
}

Response (200 OK):
{
  "draft_id": 12345
}
```

**Step 2: Set MD5**
```http
POST https://ws75.aptoide.com/api/7/uploader/draft/apk/set
Content-Type: application/json

Request Body:
{
  "draft_id": 12345,
  "apk_md5sum": "abc123def456..."
}

Response (200 OK):
{
  "status": "OK"
}
```

**Step 3: Set Status to PENDING**
```http
POST https://ws75.aptoide.com/api/7/uploader/draft/status/set
Content-Type: application/json

Request Body:
{
  "draft_id": 12345,
  "status": "PENDING"
}

Response (200 OK):
{
  "status": "OK"
}
```

**Step 4: Get Draft Status**
```http
GET https://ws75.aptoide.com/api/7/uploader/draft/status/get/draft_id=12345

Response (200 OK):
{
  "status": "FINISHED"
}
```

**Result:** Upload completes immediately. Server recognizes MD5 and marks upload as finished without requiring file upload.

---

### Scenario B: APK Exists in Google but Not Aptoide

**Flow:** Server returns APK-5 error, triggering conditional file upload.

#### Flow Diagram
```
1. POST /api/7/uploader/draft/create
2. POST /api/7/uploader/draft/apk/set (MD5 only)
3. POST /api/7/uploader/draft/status/set (status=PENDING)
4. GET /api/7/uploader/draft/status/get
   → Response: error="md5 not existent", code="APK-5" (NOT_EXISTENT)
5. POST /api/7/uploader/draft/metadata/exists/draft_id=X
   → Check if metadata exists in Google Play
6a. If metadata exists:
   → GET /api/7/apks/groups/get (fetch categories)
   → Auto-fill metadata from Google Play
6b. If metadata doesn't exist:
   → GET /api/7/apks/groups/get (fetch categories)
   → Show empty form, user fills manually
7. POST /api/7/uploader/draft/apk/set (with FILE)
8. POST /api/7/uploader/draft/obb/set (if OBB files exist)
9. POST /api/7/uploader/draft/apk/split/set (if split APKs exist)
10. POST /api/7/uploader/draft/status/set (status=PENDING)
11. GET /api/7/uploader/draft/status/get
    → Response: status="FINISHED"
12. ✅ Upload Complete
```

#### Code Path
```java
UploadManager.upload()
  → createDraft()           // Step 1
  → setMd5()                // Step 2
  → setPending()            // Step 3
  → getDraftStatus()        // Step 4: Returns NOT_EXISTENT (APK-5)
  → handleDraftStatus()     // Detects NOT_EXISTENT
    → setDraft(STATUS_SET_DRAFT)
    → setDraftToProgress()  // Update local status
    → uploadFiles()         // Step 7-9: Upload APK, OBB, splits
      → uploadBaseApk()
      → uploadObbMain() (if exists)
      → uploadObbPatch() (if exists)
      → uploadSplit() (if splits exist)
    → setPending()          // Step 10
    → getDraftStatus()      // Step 11: Returns FINISHED
  → Complete!
```

#### API Details

**Steps 1-4:** Same as Scenario A

**Step 4 Response (Error):**
```http
GET https://ws75.aptoide.com/api/7/uploader/draft/status/get/draft_id=12345

Response (401 Unauthorized):
{
  "error": "md5 not existent",
  "code": "APK-5"
}
```

**Step 5: Check Metadata Exists**
```http
POST https://ws75.aptoide.com/api/7/uploader/draft/metadata/exists/draft_id=12345

Response (200 OK):
{
  "status": "OK",
  "has": true,      // Metadata exists somewhere
  "exists": false   // Metadata doesn't exist in Aptoide
}
```

**Step 6: Get Categories**
```http
GET https://ws75.aptoide.com/api/7/apks/groups/get

Response (200 OK):
{
  "categories": [
    {"id": 1, "name": "Games"},
    {"id": 2, "name": "Communication"},
    // ...
  ]
}
```

**Step 7: Upload APK File**
```http
POST https://ws75.aptoide.com/api/7/uploader/draft/apk/set
Content-Type: multipart/form-data

Request Body (multipart):
- draft_id: 12345
- apk_file: [binary APK data]
  filename: "base.apk" (NOT full path!)

Response (200 OK):
{
  "status": "OK"
}
```

**Step 8: Upload OBB Files (if present)**
```http
POST https://ws75.aptoide.com/api/7/uploader/draft/obb/set
Content-Type: multipart/form-data

Request Body (multipart):
- draft_id: 12345
- obb_main_file: [binary OBB data]
  filename: "main.123.com.example.app.obb"

Response (200 OK):
{
  "status": "OK"
}
```

```http
POST https://ws75.aptoide.com/api/7/uploader/draft/obb/set
Content-Type: multipart/form-data

Request Body (multipart):
- draft_id: 12345
- obb_patch_file: [binary OBB data]
  filename: "patch.123.com.example.app.obb"

Response (200 OK):
{
  "status": "OK"
}
```

**Step 9: Upload Split APKs (Android 5.0+)**
```http
POST https://ws75.aptoide.com/api/7/uploader/draft/apk/split/set
Content-Type: multipart/form-data

Request Body (multipart):
- draft_id: 12345
- file: [binary split APK data]
  filename: "split_config.arm64_v8a.apk"

Response (200 OK):
{
  "status": "OK"
}
```

**Steps 10-11:** Same as Scenario A steps 3-4

---

### Scenario C: APK Not in Aptoide or Google (No MD5)

**Flow:** Server returns DRAFT-2 error because MD5 wasn't set during draft creation.

#### Flow Diagram
```
1. POST /api/7/uploader/draft/create (WITHOUT apk_md5sum)
2. POST /api/7/uploader/draft/status/set (status=PENDING)
3. GET /api/7/uploader/draft/status/get
   → Response: 404 NOT FOUND, error="No apk_md5sum was defined yet", code="DRAFT-2"
4. POST /api/7/uploader/draft/apk/set (with FILE)
5. POST /api/7/uploader/draft/obb/set (if OBB files exist)
6. POST /api/7/uploader/draft/apk/split/set (if split APKs exist)
7. POST /api/7/uploader/draft/apk/set (with MD5)
8. POST /api/7/uploader/draft/status/set (status=PENDING)
9. GET /api/7/uploader/draft/status/get
   → Response: status="FINISHED"
10. ✅ Upload Complete
```

**Note:** This scenario is less common in the current implementation because `createDraft()` typically includes `apk_md5sum` in the request body.

---

## Complete API Endpoint Reference

### POST /api/7/uploader/draft/create
**Purpose:** Create a new upload draft

**Code Mapping:**
- `UploadManager.createDraft()` (line ~304-309)
- `RetrofitUploadService.createDraft()` (line ~63-75)

**Request:**
```json
{
  "package_name": "com.example.app",
  "vercode": 42,
  "apk_md5sum": "abc123..." // Optional
}
```

**Response (200 OK):**
```json
{
  "draft_id": 12345
}
```

**Errors:**
- 401: Authentication failure
- 500: Server error

---

### POST /api/7/uploader/draft/apk/set (MD5 Only)
**Purpose:** Set APK MD5 hash without uploading file (MD5-first optimization)

**Code Mapping:**
- `UploadManager.setMd5()` (line ~297-302)
- `RetrofitUploadService.setDraftMd5s()` (line ~204-220)
  - Android 5.0+: `setDraftMd5sAboveLollipop()` - includes split APK MD5s
  - Android <5.0: `setDraftMd5sBelowLollipop()` - base APK MD5 only

**Request (Android <5.0):**
```http
POST /api/7/uploader/draft/apk/set
Content-Type: application/x-www-form-urlencoded

draft_id=12345&apk_md5sum=abc123def456...
```

**Request (Android 5.0+):**
```json
{
  "access_token": "...",
  "draft_id": 12345,
  "splits": [
    {"name": "base", "md5sum": "abc123..."},
    {"name": "config.arm64_v8a", "md5sum": "def456..."}
  ],
  "apk_md5sum": "abc123..."
}
```

**Response (200 OK):**
```json
{
  "status": "OK"
}
```

**Errors:**
- 401: Authentication failure
- 400: Invalid MD5 format

---

### POST /api/7/uploader/draft/apk/set (File Upload)
**Purpose:** Upload APK file binary

**Code Mapping:**
- `UploadManager.uploadFiles()` (line ~246-251)
- `RetrofitUploadService.uploadBaseApk()` (line ~238-254)

**Request:**
```http
POST /api/7/uploader/draft/apk/set
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="draft_id"

12345
------WebKitFormBoundary
Content-Disposition: form-data; name="apk_file"; filename="base.apk"
Content-Type: application/vnd.android.package-archive

[binary APK data]
------WebKitFormBoundary--
```

**IMPORTANT:**
- Send **filename only** (e.g., "base.apk"), NOT full path ("/data/app/base.apk")
- Include `Content-Length` header (set via RequestBody.contentLength())
- Validate file exists, is readable, and not empty before upload

**Response (200 OK):**
```json
{
  "status": "OK"
}
```

**Errors:**
- **FILE-5**: File missing (server didn't receive file)
- **FILE-200, FILE-202, FILE-206**: File upload errors
- **APK-109**: Generic file processing error

---

### POST /api/7/uploader/draft/obb/set
**Purpose:** Upload OBB files (main or patch expansion files)

**Code Mapping:**
- `RetrofitUploadService.uploadObbMain()` (line ~274-290)
- `RetrofitUploadService.uploadObbPatch()` (line ~292-308)

**Request (Main OBB):**
```http
POST /api/7/uploader/draft/obb/set
Content-Type: multipart/form-data

draft_id=12345
obb_main_file=[binary data]
filename="main.42.com.example.app.obb"
```

**Request (Patch OBB):**
```http
POST /api/7/uploader/draft/obb/set
Content-Type: multipart/form-data

draft_id=12345
obb_patch_file=[binary data]
filename="patch.42.com.example.app.obb"
```

**Response (200 OK):**
```json
{
  "status": "OK"
}
```

---

### POST /api/7/uploader/draft/apk/split/set
**Purpose:** Upload split APK files (Android 5.0+ app bundles)

**Code Mapping:**
- `RetrofitUploadService.uploadSplit()` (line ~310-325)
- `UploadManager.uploadSplits()` - handles MISSING_SPLITS scenario

**Request:**
```http
POST /api/7/uploader/draft/apk/split/set
Content-Type: multipart/form-data

draft_id=12345
file=[binary split APK data]
filename="split_config.arm64_v8a.apk"
type=ABI  // Optional: ABI | DENSITY | LOCALE
```

**Alternative Request (MD5 only):**
```http
POST /api/7/uploader/draft/apk/split/set
Content-Type: application/x-www-form-urlencoded

draft_id=12345
md5sum=abc123...
type=ABI  // Required when using MD5: ABI | DENSITY | LOCALE
```

**Response (200 OK):**
```json
{
  "status": "OK"
}
```

**Split Types:**
- `ABI` - Architecture-specific splits (arm64-v8a, armeabi-v7a, x86, x86_64)
- `DENSITY` - Screen density splits (xxxhdpi, xxhdpi, xhdpi, hdpi, mdpi, ldpi)
- `LOCALE` - Language/locale splits (en, es, pt, etc.)

**Note:** Called multiple times for each split (base, config splits, feature splits)

---

### POST /api/7/uploader/draft/metadata/exists/draft_id=X
**Purpose:** Check if metadata exists in Google Play

**Code Mapping:**
- `UploadManager.hasApplicationMetadata()` (line ~313-317)
- `RetrofitUploadService.getAppMetadata()`

**Request:**
```http
POST /api/7/uploader/draft/metadata/exists/draft_id=12345
```

**Response (200 OK):**
```json
{
  "status": "OK",
  "has": true,      // Metadata found in Google Play
  "exists": false   // Metadata doesn't exist in Aptoide draft
}
```

**Mapping to UploadDraft.Status:**
- `has: true` → `UploadDraft.Status.SET_STATUS_TO_DRAFT` (metadata available, can auto-fill)
- `has: false` → `UploadDraft.Status.NO_META_DATA` (user must fill form)

---

### GET /api/7/apks/groups/get
**Purpose:** Fetch category list for metadata form

**Code Mapping:**
- Called from UI layer to populate category dropdown

**Request:**
```http
GET https://ws75.aptoide.com/api/7/apks/groups/get
```

**Response (200 OK):**
```json
{
  "categories": [
    {"id": 1, "name": "Games"},
    {"id": 2, "name": "Communication"},
    {"id": 3, "name": "Social"},
    // ...
  ]
}
```

---

### POST /api/7/uploader/draft/metadata/set
**Purpose:** Set application metadata (description, category, etc.)

**Code Mapping:**
- `UploadManager.setMetadata()` (line ~319-324)
- `RetrofitUploadService.setMetadata()`

**Request:**
```json
{
  "draft_id": 12345,
  "age_rating": 2,                         // Required: Age rating ID (see table below)
  "apk_name": "My Awesome App",            // Required: App display name
  "category": 21,                          // Required: Category ID (see table below)
  "description": "This is an awesome game...",  // Required: App description
  "email": "support@example.com",          // Optional: Support email
  "phone_number": "+1234567890",           // Optional: Support phone
  "website": "https://example.com"         // Optional: App website
}
```

**Age Rating Options:**

| ID | Name | Min Age | Title | PEGI |
|----|------|---------|-------|------|
| 1 | All | 0 | Everyone | PEGI-3 |
| 5 | Tween | 7 | Very Low Maturity | PEGI-7 |
| 2 | Pre-Teen | 12 | Low Maturity | PEGI-12 |
| 3 | Teen | 16 | Medium Maturity | PEGI-16 |
| 4 | Mature | 18 | High Maturity | PEGI-18 |

**Category Options (Major Categories):**

| ID | Name | Parent |
|----|------|--------|
| 1 | Applications | - |
| 2 | Games | - |

**Category Options (Application Subcategories):**

| ID | Name | Parent |
|----|------|--------|
| 3 | Comics | Applications |
| 4 | Communication | Applications |
| 5 | Entertainment | Applications |
| 6 | Finance | Applications |
| 7 | Health | Applications |
| 8 | Lifestyle | Applications |
| 9 | Multimedia | Applications |
| 10 | News & Weather | Applications |
| 11 | Productivity | Applications |
| 12 | Reference | Applications |
| 13 | Shopping | Applications |
| 14 | Social | Applications |
| 15 | Sports | Applications |
| 16 | Themes | Applications |
| 17 | Tools | Applications |
| 18 | Travel | Applications |
| 19 | Demo | Applications |
| 20 | Software Libraries | Applications |
| 26 | News & Magazines | Applications |
| 31 | Music & Audio | Applications |
| 39 | Photography | Applications |
| 40 | Personalization | Applications |
| 78 | Books & Reference | Applications |
| 86 | Health & Fitness | Applications |
| 89 | Media & Video | Applications |
| 95 | Education | Applications |
| 149 | Business | Applications |
| 310 | Weather | Applications |
| 415 | Travel & Local | Applications |
| 418 | Transportation | Applications |
| 459 | Medical | Applications |
| 736 | Libraries & Demo | Applications |
| 850 | Transport | Applications |
| 8784 | Video Players & Editors | Applications |
| 8792 | Maps & Navigation | Applications |
| 8859 | Auto & Vehicles | Applications |
| 8861 | Food & Drink | Applications |
| 8866 | House & Home | Applications |
| 8867 | Events | Applications |
| 8868 | Parenting | Applications |
| 8869 | Dating | Applications |
| 8871 | Art & Design | Applications |
| 8874 | Beauty | Applications |
| 8875 | Others | Applications |

**Category Options (Game Subcategories):**

| ID | Name | Parent |
|----|------|--------|
| 21 | Arcade & Action | Games |
| 22 | Brain & Puzzle | Games |
| 23 | Cards & Casino | Games |
| 24 | Casual | Games |
| 47 | Racing | Games |
| 293 | Sports Games | Games |
| 1668 | Puzzle | Games |
| 1669 | Casino | Games |
| 1670 | Action | Games |
| 1671 | Strategy | Games |
| 1672 | Family | Games |
| 1674 | Simulation | Games |
| 1675 | Adventure | Games |
| 1677 | Word | Games |
| 1690 | Arcade | Games |
| 1691 | Trivia | Games |
| 1692 | Card | Games |
| 1693 | Role Playing | Games |
| 1695 | Educational | Games |
| 1696 | Music | Games |
| 1697 | Board | Games |

**Response (200 OK):**
```json
{
  "status": "OK"
}
```

**Errors:**
- **MARG-5**: Required field missing (age_rating)
- **MARG-101**: Required field missing (apk_name)
- **MARG-102**: Required field missing (category)
- **MARG-103**: Required field missing (description)
- **MARG-205**: Invalid format/value

---

### POST /api/7/uploader/draft/status/set
**Purpose:** Set draft status (usually to PENDING for processing)

**Code Mapping:**
- `UploadManager.setPending()` (line ~290-295)
- `RetrofitUploadService.setDraftStatus()` (line ~77-88)

**Request:**
```http
POST /api/7/uploader/draft/status/set
Content-Type: application/x-www-form-urlencoded

draft_id=12345&status=PENDING
```

**Response (200 OK):**
```json
{
  "status": "OK"
}
```

**Possible Status Values:**
- `PENDING`: Submit for processing
- `DRAFT`: Save as draft

**Status Transition Rules:**

The server enforces specific status transitions:

| From Status | To Status | Allowed? | Notes |
|-------------|-----------|----------|-------|
| DRAFT | PENDING | ✅ Yes | Normal progression |
| PENDING | DRAFT | ❌ No | Cannot revert pending to draft |
| ERROR | DRAFT | ✅ Yes | Allows retry after error |
| EVICTED | DRAFT | ✅ Yes | Recover from eviction |
| Any ERROR | PENDING | ✅ Yes | Direct retry without DRAFT |
| FINISHED | Any | ❌ No | Upload complete, immutable |

---

### GET /api/7/uploader/draft/status/get/draft_id=X
**Purpose:** Get current draft processing status

**Code Mapping:**
- `UploadManager.getDraftStatus()` - calls RetrofitUploadService
- `RetrofitUploadService.getDraftStatus()` (line ~90-115)
- `RetrofitUploadService.mapUploadDraftResponse()` (line ~475-605)

**Request:**
```http
GET https://ws75.aptoide.com/api/7/uploader/draft/status/get/draft_id=12345
```

**Response (200 OK - Success):**
```json
{
  "status": "FINISHED"
}
```

**Response (200 OK - Processing):**
```json
{
  "status": "PROCESSING"
}
```

**Retry Logic:**
- If status is "PROCESSING": Retry with exponential backoff
- After 3 "PROCESSING" responses: Continue polling
- If status is "PENDING": Retry (server hasn't started processing yet)
- Implemented via `RetryWithDelay` class

**Response (401/404 - Error):**
```json
{
  "errors": [
    {
      "name": "APK-5",
      "description": "md5 not existent",
      "details": {}
    }
  ]
}
```

**Response (404 - Missing MD5):**
```json
{
  "errors": [
    {
      "name": "DRAFT-2",
      "description": "No apk_md5sum was defined yet.",
      "details": {}
    }
  ]
}
```

**All Possible Error Responses:**

The GET status endpoint can return various error codes depending on the upload state:

**Draft Not Ready:**
- `DRAFT-2`: "No apk_md5sum was defined yet" (404)

**APK Issues:**
- `APK-5`: "md5 not existent" (401) - MD5 not found, files needed
- `APK-101`: "This app can't be distributed through Aptoide" (401) - Intellectual property
- `APK-102`: "This app is infected" (401) - Malware detected
- `APK-103`: "This app is already in your store" (200) - Duplicate
- `APK-104`: "This app can only be uploaded by the publisher" (401) - Publisher only
- `APK-106`: "Invalid app signature" (401) - Signature mismatch
- `APK-107`: "Anti-spam rule triggered" (401) - Rate limiting
- `APK-109`: "Error processing APK file" (401) - Generic file error
- `APK-112`: "This app is Catappult certified" (401) - Special certification

**Metadata Issues:**
- `MARG-5`: "Missing age_rating" (401)
- `MARG-101`: "Missing apk_name" (401)
- `MARG-102`: "Missing category" (401)
- `MARG-103`: "Missing description" (401)
- `MARG-205`: "Invalid field format" (401)

**File Upload Issues:**
- `FILE-5`: "File missing" (401) - Server didn't receive file
- `FILE-200`: "File upload error" (401)
- `FILE-202`: "File upload error" (401)
- `FILE-206`: "File upload error" (401)

**Split APK Issues:**
- `SPLIT-1`: "Missing split APK files" (401)

**Success States:**
- `FINISHED`: Upload complete (200)
- `PROCESSING`: Still processing (200) - client should retry

---

## Error Codes Reference

### Success Codes
| Code | Description | Maps To | Action |
|------|-------------|---------|--------|
| 200 + "FINISHED" | Upload completed successfully | `UploadDraft.Status.COMPLETED` | Done! |

### Duplicate Detection
| Code | Description | Maps To | Action |
|------|-------------|---------|--------|
| **APK-103** | APK already exists in store (duplicate) | `UploadDraft.Status.DUPLICATE` | Complete without file upload |

### File Upload Required
| Code | Description | Maps To | Action |
|------|-------------|---------|--------|
| **APK-5** | MD5 not existent in server | `UploadDraft.Status.NOT_EXISTENT` | Upload files → setPending → getStatus |
| **DRAFT-2** | No MD5 defined yet | Triggers file upload | Upload files → set MD5 → setPending |

### Metadata Required
| Code | Description | Maps To | Action |
|------|-------------|---------|--------|
| **MARG-5** | Missing age_rating | `UploadDraft.Status.MISSING_ARGUMENTS` | Show metadata form |
| **MARG-101** | Missing apk_name | `UploadDraft.Status.MISSING_ARGUMENTS` | Show metadata form |
| **MARG-102** | Missing category | `UploadDraft.Status.MISSING_ARGUMENTS` | Show metadata form |
| **MARG-103** | Missing description | `UploadDraft.Status.MISSING_ARGUMENTS` | Show metadata form |
| **MARG-205** | Invalid field format | `UploadDraft.Status.MISSING_ARGUMENTS` | Show error, retry |

### Split APKs Issues
| Code | Description | Maps To | Action |
|------|-------------|---------|--------|
| **SPLIT-1** | Missing split APK files | `UploadDraft.Status.MISSING_SPLITS` | Upload missing splits |

### Upload Rejections
| Code | Description | Maps To | Action |
|------|-------------|---------|--------|
| **APK-101** | Intellectual property rights issue | `UploadDraft.Status.INTELLECTUAL_RIGHTS` | Contact support |
| **APK-102** | Infected/malware detected | `UploadDraft.Status.INFECTED` | Review APK |
| **APK-104** | Publisher-only app | `UploadDraft.Status.PUBLISHER_ONLY` | Must be original publisher |
| **APK-106** | Invalid signature | `UploadDraft.Status.INVALID_SIGNATURE` | Review signing |
| **APK-107** | Anti-spam rule triggered | `UploadDraft.Status.ANTI_SPAM_RULE` | Contact support |
| **APK-112** | Catappult certified | `UploadDraft.Status.CATAPPULT_CERTIFIED` | Contact support |

### File Upload Errors
| Code | Description | Maps To | Action |
|------|-------------|---------|--------|
| **FILE-5** | File missing on server | `UploadDraft.Status.UPLOAD_FAILED_RETRY` | Retry upload |
| **FILE-200** | File upload error | `UploadDraft.Status.UPLOAD_FAILED_RETRY` | Retry upload |
| **FILE-202** | File upload error | `UploadDraft.Status.UPLOAD_FAILED_RETRY` | Retry upload |
| **FILE-206** | File upload error | `UploadDraft.Status.UPLOAD_FAILED_RETRY` | Retry upload |
| **APK-109** | Generic file processing error | `UploadDraft.Status.UPLOAD_FAILED_RETRY` | Retry upload |

### Unknown Errors
| Code | Description | Maps To | Action |
|------|-------------|---------|--------|
| Other | Unknown server error | `UploadDraft.Status.UNKNOWN_ERROR` | Show generic error |
| Network timeout | Client-side error | `UploadDraft.Status.CLIENT_ERROR` | Retry |

---

## Code Implementation Mapping

### UploadManager.java

Main upload orchestration logic.

**Path:** `/Users/jdandradex/dev/android/aptoide-uploader/app/src/main/java/com/aptoide/uploader/apps/UploadManager.java`

| Method | Line | Purpose | API Endpoint |
|--------|------|---------|--------------|
| `upload()` | ~127-154 | Main orchestration | Calls all below methods |
| `createDraft()` | ~304-309 | Step 1: Create draft | POST /api/7/uploader/draft/create |
| `setMd5()` | ~297-302 | Step 2: Set MD5 hash | POST /api/7/uploader/draft/apk/set (MD5) |
| `setPending()` | ~290-295 | Step 3: Mark PENDING | POST /api/7/uploader/draft/status/set |
| `getDraftStatus()` | Calls service | Step 4: Check status | GET /api/7/uploader/draft/status/get |
| `handleDraftStatus()` | ~156-206 | Step 5: Handle errors | Conditional flow |
| `uploadFiles()` | ~246-251 | Conditional upload | POST /api/7/uploader/draft/apk/set (file) |
| `hasApplicationMetadata()` | ~313-317 | Check metadata | POST /api/7/uploader/draft/metadata/exists |
| `setMetadata()` | ~319-324 | Set metadata | POST /api/7/uploader/draft/metadata/set |
| `setDraftToProgress()` | ~284-288 | Update local status | Local state only |

**upload() Flow:**
```java
private Completable upload(UploadDraft draft) {
  return createDraft(draft)           // Step 1: POST /create
      .flatMap(this::setMd5)          // Step 2: POST /apk/set (MD5)
      .flatMap(this::setPending)      // Step 3: POST /status/set (PENDING)
      .flatMap(this::getDraftStatus)  // Step 4: GET /status/get
      .flatMap(this::handleDraftStatus) // Step 5: Handle response
      .flatMap(uploadDraft -> {
        // If NOT_EXISTENT/MISSING_ARGUMENTS/MISSING_SPLITS:
        // Call handleDraftStatus again to upload files/metadata
        if (uploadDraft.getStatus().equals(UploadDraft.Status.NOT_EXISTENT)
            || uploadDraft.getStatus().equals(UploadDraft.Status.MISSING_ARGUMENTS)
            || uploadDraft.getStatus().equals(UploadDraft.Status.MISSING_SPLITS)) {
          return handleDraftStatus(uploadDraft).flatMap(uploadDraft2 -> {
            if (uploadDraft2.getStatus().equals(UploadDraft.Status.MISSING_ARGUMENTS)) {
              return setDraft(uploadDraft2, UploadDraft.Status.UNKNOWN_ERROR);
            }
            return Single.just(uploadDraft2);
          });
        } else {
          return Single.just(uploadDraft);
        }
      })
      .toCompletable();
}
```

**handleDraftStatus() Flow:**
```java
private Single<UploadDraft> handleDraftStatus(UploadDraft uploadDraft) {
  if (uploadDraft.getStatus().equals(UploadDraft.Status.MISSING_ARGUMENTS)) {
    // Handle metadata required
    return hasApplicationMetadata(uploadDraft)
        .flatMap(handleHasMetadataResult())
        .flatMap(metadataDraft -> setDraft(metadataDraft, UploadDraft.Status.STATUS_SET_DRAFT))
        .flatMap(uploadedDraft -> setPending(uploadedDraft)
            .flatMap(this::getDraftStatus));

  } else if (uploadDraft.getStatus().equals(UploadDraft.Status.NOT_EXISTENT)) {
    // Handle file upload required
    return setDraft(uploadDraft, UploadDraft.Status.STATUS_SET_DRAFT)
        .flatMap(this::setDraftToProgress)
        .flatMap(this::uploadFiles)           // Upload files here!
        .filter(waitingUploadConfirmation())
        .flatMapSingle(uploadedDraft -> setPending(uploadedDraft)
            .flatMap(this::getDraftStatus));

  } else if (uploadDraft.getStatus().equals(UploadDraft.Status.MISSING_SPLITS)) {
    // Handle split APKs required
    return setDraft(uploadDraft, UploadDraft.Status.STATUS_SET_DRAFT)
        .flatMap(this::setDraftToProgress)
        .flatMap(uploadDraft1 -> getSplitsNotExistentPaths(uploadDraft1.getSplitsToBeUploaded())
            .flatMap(splits -> uploaderService.uploadSplits(uploadDraft1, splits)
                .singleOrError()
                .filter(waitingUploadConfirmation())
                .flatMapSingle(uploadedDraft -> setPending(uploadedDraft)
                    .flatMap(this::getDraftStatus))));
  }

  // Default: Return draft as-is (DUPLICATE, COMPLETED, errors)
  return Single.just(uploadDraft);
}
```

---

### RetrofitUploadService.java

API call implementations using Retrofit.

**Path:** `/Users/jdandradex/dev/android/aptoide-uploader/app/src/main/java/com/aptoide/uploader/apps/network/RetrofitUploadService.java`

| Method | Line | Purpose | API Endpoint |
|--------|------|---------|--------------|
| `createDraft()` | ~63-75 | Create upload draft | POST /api/7/uploader/draft/create |
| `setDraftMd5s()` | ~204-220 | Set MD5 (dispatches to above/below Lollipop) | POST /api/7/uploader/draft/apk/set |
| `setDraftMd5sAboveLollipop()` | Called by above | Set MD5 + splits (Android 5.0+) | POST /api/7/uploader/draft/apk/set |
| `setDraftMd5sBelowLollipop()` | Called by above | Set MD5 only (Android <5.0) | POST /api/7/uploader/draft/apk/set |
| `uploadFiles()` | ~144-183 | Orchestrate file uploads | Multiple endpoints |
| `uploadBaseApk()` | ~238-254 | Upload APK file | POST /api/7/uploader/draft/apk/set |
| `uploadObbMain()` | ~274-290 | Upload main OBB | POST /api/7/uploader/draft/obb/set |
| `uploadObbPatch()` | ~292-308 | Upload patch OBB | POST /api/7/uploader/draft/obb/set |
| `uploadSplit()` | ~310-325 | Upload split APK | POST /api/7/uploader/draft/apk/split/set |
| `setDraftStatus()` | ~77-88 | Set draft status | POST /api/7/uploader/draft/status/set |
| `getDraftStatus()` | ~90-115 | Get draft status | GET /api/7/uploader/draft/status/get |
| `mapUploadDraftResponse()` | ~475-605 | Map error codes to Status | N/A (local mapping) |
| `createFileRequestBody()` | ~409-472 | Create multipart file body | N/A (helper) |

**Critical Implementation Details:**

**createFileRequestBody()** - Fixes for FILE-5:
```java
private RequestBody createFileRequestBody(String extension, String apkPath, String packageName) {
  File apk = new File(apkPath);

  // ✅ FIX 1: Validate file exists and is readable
  if (!apk.exists()) {
    throw new IllegalArgumentException("File does not exist: " + apkPath);
  }
  if (!apk.canRead()) {
    throw new IllegalArgumentException("File is not readable: " + apkPath);
  }

  final long fileSize = apk.length();

  // ✅ FIX 2: Validate file is not empty
  if (fileSize == 0) {
    throw new IllegalArgumentException("File is empty: " + apkPath);
  }

  return new RequestBody() {
    @Override public MediaType contentType() {
      String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
      if (mimeType == null) mimeType = "application/octet-stream";
      return MediaType.parse(mimeType);
    }

    // ✅ FIX 3: Override contentLength() for proper HTTP header
    @Override public long contentLength() {
      return fileSize;
    }

    @Override public void writeTo(BufferedSink sink) throws IOException {
      // Upload implementation with progress tracking
    }
  };
}
```

**uploadBaseApk()** - Correct filename extraction:
```java
private Observable<Response<GenericDraftResponse>> uploadBaseApk(UploadDraft draft) {
  return accountProvider.getAccount()
      .firstOrError()
      .flatMapObservable(aptoideAccount -> accountProvider.getToken()
          .toObservable()
          .flatMap(accessToken -> {
            String apkPath = draft.getInstalledApp().getApkPath();
            // ✅ FIX 4: Extract filename only, NOT full path
            String filename = new File(apkPath).getName();
            return serviceV7.uploadApkFile(
                getParamsSetApkFile(accessToken, draft.getDraftId()),
                MultipartBody.Part.createFormData("apk_file", filename,
                    createFileRequestBody("apk", apkPath, draft.getInstalledApp().getPackageName())));
          }))
      .doOnNext(response -> handleUploadError(draft, response));
}
```

---

## Best Practices

### ✅ DO: Always Use MD5-First Approach

**Correct Flow:**
```java
createDraft() → setMd5() → setPending() → getDraftStatus()
```

**Why:** Server checks MD5 first. If duplicate (APK-103), no file upload needed!

**Example:** Uploading an existing APK:
- Without MD5-first: ~50MB uploaded unnecessarily
- With MD5-first: 0 bytes uploaded, instant completion

### ✅ DO: Respect Server Error Responses

**Handle these conditionally:**
- `NOT_EXISTENT (APK-5)` → Upload files
- `MISSING_ARGUMENTS (MARG-*)` → Show metadata form
- `MISSING_SPLITS (SPLIT-1)` → Upload missing splits
- `DUPLICATE (APK-103)` → Complete immediately (no upload)

### ✅ DO: Validate Files Before Upload

**Required checks:**
```java
File file = new File(path);
if (!file.exists()) throw new IllegalArgumentException("File does not exist");
if (!file.canRead()) throw new IllegalArgumentException("File is not readable");
if (file.length() == 0) throw new IllegalArgumentException("File is empty");
```

### ✅ DO: Extract Filename Only (Not Full Path)

**Correct:**
```java
String filename = new File(apkPath).getName(); // "base.apk"
```

**Incorrect:**
```java
String filename = apkPath; // "/data/app/com.example.app/base.apk" - causes FILE-5!
```

### ✅ DO: Implement contentLength() in RequestBody

**Required for multipart uploads:**
```java
@Override public long contentLength() {
  return fileSize;
}
```

**Why:** OkHttp needs this to set the `Content-Length` HTTP header correctly.

### ✅ DO: Handle All Three Scenarios

**Test coverage must include:**
1. Duplicate APK (APK-103) - MD5 exists
2. New APK (APK-5) - MD5 doesn't exist, file upload required
3. Metadata required (MARG-*) - User must fill form

---

## Common Pitfalls

### ❌ DON'T: Upload Files Before setPending()

**Broken Flow:**
```java
createDraft() → setMd5() → uploadFiles() → setPending() → getDraftStatus()
                           ^^^^^^^^^^^
                           WRONG! Breaks MD5-first optimization
```

**Impact:**
- Uploads files even when APK already exists (APK-103)
- Wastes bandwidth and server resources
- Slower user experience

**Fix:** Call `setPending()` first, then conditionally upload files based on getDraftStatus() response.

### ❌ DON'T: Send Full File Path as Filename

**Broken Code:**
```java
String filename = "/data/app/com.example.app/base.apk";
MultipartBody.Part.createFormData("apk_file", filename, requestBody);
```

**Impact:** Server receives malformed multipart data → FILE-5 error

**Fix:**
```java
String filename = new File(apkPath).getName(); // "base.apk"
```

### ❌ DON'T: Skip contentLength() Override

**Broken Code:**
```java
return new RequestBody() {
  @Override public MediaType contentType() { ... }
  // Missing contentLength()!
  @Override public void writeTo(BufferedSink sink) { ... }
};
```

**Impact:** HTTP `Content-Length` header not set → incomplete uploads, FILE-* errors

**Fix:** Always override `contentLength()` to return file size.

### ❌ DON'T: Skip File Validation

**Broken Code:**
```java
File file = new File(path);
RequestBody body = createRequestBody(file); // Assume file exists
```

**Impact:**
- FileNotFoundException at runtime
- Crashes during upload
- Bad user experience

**Fix:** Always validate file exists, is readable, and not empty before creating RequestBody.

### ❌ DON'T: Ignore MISSING_ARGUMENTS Status

**Broken Code:**
```java
if (draft.getStatus() == UploadDraft.Status.NOT_EXISTENT) {
  uploadFiles();
}
// Ignores MISSING_ARGUMENTS!
```

**Impact:** Upload gets stuck waiting for metadata that never comes.

**Fix:** Handle `MISSING_ARGUMENTS` by showing metadata form and calling `setMetadata()`.

---

## Retry Logic and Error Handling

### getDraftStatus() Retry Mechanism

**Implementation:** `RetrofitUploadService.getDraftStatus()` line ~90-115

**Behavior:**
```java
return serviceV7.getStatus(accessToken, String.valueOf(draft.getDraftId()))
    .flatMap(response -> {
      if (response.body().getData().getStatus().equals("PENDING")
          || response.body().getData().getStatus().equals("PROCESSING")) {
        throw new NetworkErrorException(); // Triggers retry
      }
      return Observable.just(mapUploadDraftResponse(response, draft));
    })
    .retryWhen(new RetryWithDelay());
```

**Retry Rules:**
- If status is "PENDING": Retry (server hasn't started processing)
- If status is "PROCESSING": Retry (server is working on it)
- After 3 "PROCESSING" responses: Continue polling
- Uses exponential backoff via `RetryWithDelay`

### Error Handling Strategy

**Recoverable Errors** (retry automatically):
- FILE-5, FILE-200, FILE-202, FILE-206 → `UPLOAD_FAILED_RETRY`
- APK-109 → `UPLOAD_FAILED_RETRY`
- Network timeout → `CLIENT_ERROR`

**User Action Required**:
- MARG-* → Show metadata form
- APK-5 (NOT_EXISTENT) → Upload files
- SPLIT-1 → Upload missing splits

**Unrecoverable Errors** (show user error message):
- APK-101 (intellectual rights) → Contact support
- APK-102 (infected) → Review APK
- APK-104 (publisher only) → Must be original publisher
- APK-106 (invalid signature) → Review signing
- APK-107 (anti-spam) → Contact support
- APK-112 (catappult certified) → Contact support

---

## Server-Side Behavior

### Metadata Scraping from Google Play

When you set a draft to PENDING status without providing metadata, the server attempts to fetch metadata from Google Play automatically.

**Scraping Timeline:**
```
Attempt 1: Immediate (0 seconds)
Attempt 2: After 1 second
Attempt 3: After 10 seconds
Attempt 4: After 60 seconds
Total attempts: 4 tries over ~71 seconds
```

**What happens during scraping:**
1. Server checks if the package exists in Google Play
2. If found: Extracts app name, description, category, icon, screenshots
3. If not found after 4 attempts: Returns `MISSING_ARGUMENTS` error (MARG-*)
4. If found: Upload proceeds automatically to FINISHED

**Impact on upload flow:**
- If scraping succeeds: No user action needed, upload completes
- If scraping fails: User must manually provide metadata via form
- Client should show "Finalizing upload..." during this period

### Server-Side Retry Logic (Uploader v3)

The server has built-in retry logic when calling the underlying Uploader v3 API.

**Retry Mechanism:**
```
Upload v7 → Uploader v3 API
  ↓
  If MD5 already exists in v3:
    ↓
    Retry #1: Wait 1 second, try again
    ↓
    Retry #2: Wait 1 second, try again
    ↓
    Retry #3: Wait 1 second, try again
    ↓
    If still fails: Return error to client
```

**Total retries:** 3 attempts with 1-second delays

**Why this exists:**
- Race condition: Multiple clients uploading same APK simultaneously
- MD5 cache inconsistency between v7 and v3
- Eventual consistency in distributed system
- Gives v3 time to process and register the MD5

**Client impact:**
- You may see brief delays (1-3 seconds) during PROCESSING status
- These delays are normal and expected
- Don't implement additional retry logic on top of server retries

### Draft Expiration

Drafts in ERROR or EVICTED status can be reset to DRAFT to retry the upload.

**Status meanings:**
- `ERROR`: Upload failed, can be retried by setting status to DRAFT or PENDING
- `EVICTED`: Draft was evicted from processing queue (timeout/overload), can be retried
- `FINISHED`: Upload complete, cannot be modified

---

## Testing Checklist

### Scenario A: Duplicate APK
- [ ] Install an APK that's already in your Aptoide store (same version)
- [ ] Trigger upload
- [ ] Verify notification shows "This app is already in your store (APK-103)"
- [ ] Verify NO upload progress shown (no files uploaded)
- [ ] Check logs: Should see `getDraftStatus` return DUPLICATE
- [ ] Verify `uploadFiles()` is NOT called

### Scenario B: New APK
- [ ] Install a new APK not in your store
- [ ] Trigger upload
- [ ] Verify notification shows "Preparing upload..."
- [ ] Verify notification shows "Uploading... X% - base.apk" (with filename)
- [ ] Verify notification shows "Finalizing upload..."
- [ ] Verify notification shows "App successfully uploaded"
- [ ] Check logs: Should see NOT_EXISTENT → uploadFiles → setPending → COMPLETED

### Scenario C: Metadata Required
- [ ] Upload an APK that requires metadata
- [ ] Verify notification shows "We need more info! Please tap here"
- [ ] Tap notification, fill metadata form
- [ ] Submit metadata
- [ ] Verify upload completes successfully
- [ ] Check logs: Should see MISSING_ARGUMENTS → setMetadata → setPending → COMPLETED

### File Upload Edge Cases
- [ ] Upload APK with OBB files (main and/or patch)
- [ ] Upload App Bundle with split APKs (Android 5.0+)
- [ ] Verify no FILE-5 errors occur
- [ ] Verify filename extraction works (not sending full paths)
- [ ] Verify file validation prevents crashes

### Error Scenarios
- [ ] Test upload with invalid signature → APK-106
- [ ] Test upload of app already in store → APK-103
- [ ] Test upload without metadata → MARG-*
- [ ] Verify error messages are user-friendly
- [ ] Verify retry logic works for transient errors

---

## Related Files

### Source Files
- `UploadManager.java` - Main upload orchestration
- `RetrofitUploadService.java` - API call implementations
- `UploadDraft.java` - Draft model and Status enum
- `NotificationPresenter.java` - Notification flow handling
- `NotificationService.java` - Android notification display

### Configuration
- `ServiceV7.java` - Retrofit API interface definitions
- `RetryWithDelay.java` - Exponential backoff retry logic

### Documentation
- `CLAUDE.md` - Quick reference and codebase overview
- `Uploader_3.0_client.pdf` - Official API specification (in Downloads folder)

---

## Changelog

### v3.2 (Current)
- ✅ Restored MD5-first upload flow
- ✅ Fixed FILE-5 errors (filename, validation, contentLength)
- ✅ Added filename to upload progress notifications
- ✅ Added "Finalizing upload..." notification state

### v3.1
- Added Firebase Crashlytics and Analytics
- Modernized SDK to Android 14

### v3.0
- Initial upload API v7 implementation
- MD5-first optimization introduced

---

## Questions?

If you have questions about the upload flow:

1. **Check this document first** - It covers all API endpoints and scenarios
2. **Read the code** - Follow the "Code Mapping" sections to find implementations
3. **Review the PDF** - Original specification in Downloads folder
4. **Test thoroughly** - Use the Testing Checklist above

**Remember:** When in doubt, trust the MD5-first approach! Call `setPending()` before `uploadFiles()`.
