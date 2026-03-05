# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config in gradle.properties)
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug

# Clean build
./gradlew clean build
```

## Testing

```bash
# Run all unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.aptoide.uploader.LoginPresenterTest"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

Tests use **Spek** (Kotlin BDD framework) with Mockito. Test files are in `app/src/test/kotlin/`.

## Architecture

**MVP (Model-View-Presenter)** with RxJava2 reactive streams throughout.

### Layers

- **View**: Fragment-based UI implementing `View` interface with lifecycle events (CREATE, DESTROY). Special `IntentView` for intent handling.
- **Presenter**: Manages UI logic and reactive subscriptions via `CompositeDisposable`. Disposed on DESTROY lifecycle event.
- **Model**: Manager classes (`UploadManager`, `InstalledAppsManager`, `AccountManager`) handle business logic.
- **Persistence**: Room database with DAOs (`InstalledDao`, `AutoUploadSelectsDao`).

### Key Packages

- `com.aptoide.uploader.apps.*` - App management, uploads, installation tracking
- `com.aptoide.uploader.account.*` - Authentication (native, Facebook, Google)
- `com.aptoide.uploader.security.*` - Token management, auth persistence
- `com.aptoide.uploader.analytics.*` - Flurry and Rakam analytics

### Multi-Module Structure

- `app` - Main application
- `aptoide-authentication-core` - Authentication API (Retrofit + Moshi)
- `aptoide-authentication-rx` - RxJava2 wrapper for authentication

## Upload Flow

The Aptoide Uploader uses a **MD5-first optimization** approach to avoid unnecessary file uploads when APKs already exist on the server.

**Complete technical documentation**: See [UPLOAD_FLOW.md](./UPLOAD_FLOW.md)

### Quick Reference

**Standard Flow:**
1. Create draft → Set MD5 → Set PENDING → Get status
2. If DUPLICATE (APK-103): Done! No file upload needed ✅
3. If NOT_EXISTENT (APK-5): Upload files → Set PENDING → Get status
4. If MISSING_ARGUMENTS (MARG-*): Add metadata → Set PENDING → Get status

**Key Implementation Details:**
- Always call `setPending()` BEFORE `uploadFiles()` (MD5-first!)
- Extract filename only, not full path: `new File(path).getName()`
- Override `RequestBody.contentLength()` for multipart uploads
- Validate files exist/readable/not-empty before upload

**Critical Files:**
- `UploadManager.java` - Main upload orchestration logic
- `RetrofitUploadService.java` - API call implementations (Retrofit)
- `UploadDraft.java` - Status enum mapping (APK-5 → NOT_EXISTENT, etc.)
- `NotificationPresenter.java` - Notification flow based on upload status

**Error Code Examples:**
- APK-103: Duplicate (already exists)
- APK-5: Not existent (need file upload)
- FILE-5: File missing on server
- MARG-*: Missing metadata

**API Base URL:** `https://ws75.aptoide.com/`

**Official Specification:** `Uploader_3.0_client.pdf` (in Downloads folder)

## Key Dependencies

- **Networking**: Retrofit2 2.5.0 with Moshi JSON
- **Reactive**: RxJava2, RxBinding, RxRelay
- **Database**: Room (AndroidX)
- **Images**: Glide 4.9.0
- **Auth**: Facebook SDK 7.0.0, Google Sign-In

## Configuration

Required properties in `gradle.properties` or `local.properties`:
- `STORE_FILE_BACKUP_UPLOADER`, `STORE_PASSWORD_BACKUP_UPLOADER`, `KEY_ALIAS_UPLOADER`, `KEY_PASSWORD_UPLOADER` (signing)
- `FLURRY_KEY_UPLOADER`, `FACEBOOK_APP_ID_UPLOADER`, `GMS_SERVER_ID_VANILLA`, `RAKAM_API_KEY_TEST` (API keys)

## Code Review Standards

- Unit tests required for: Kotlin code, presenter flows, mappers/validators with logic
- Database schema changes require migrations
- Jira tickets use format: AND-XXXX

### Room Database Change Checklist

When modifying any `@Entity` class (adding/removing/renaming fields or changing types):

1. **Add a migration** in `RoomMigrationProvider.kt` (e.g., `migration3to4`)
2. **Bump the version** in `AppUploadsDatabase.java` to match
3. **Register the migration** in `getAllMigrations()` so it's included in the migration array
4. **Never use `OnErrorNotImplementedException`** in RxJava error handlers for database operations — use `Log.e` + `FirebaseCrashlytics.recordException()` instead

---

## TPO Configuration

Workflows for bug reporting and ticket completion.

### Workflows

| Command | Description |
|---------|-------------|
| `report-bug <desc>` | Investigate code → Create Jira Bug with dev directive |
| `complete-ticket` | Summarize changes → Jira Review → PR to dev |

### Jira

- **Project Key**: AND
- **Ticket Prefix**: `[Uploader]`
- **Labels**: android, po, qa

### Branch Naming

```
Pattern: <type>/AND-<number>_<title-with-hyphens>
Types: feature, bugfix, task
Example: feature/AND-631_improve-upload-notification
```

### State File

Runtime context stored in `.claude/state/tpo-context.json`
