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
- Jira tickets use format: APP-XXXX
