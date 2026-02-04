package com.aptoide.uploader.apps

object UploadManagerTestFixtures {

    const val TEST_MD5 = "abc123def456"
    const val TEST_PACKAGE_NAME = "com.test.app"
    const val TEST_APK_PATH = "/data/app/test.apk"
    const val TEST_APP_NAME = "Test App"
    const val TEST_DRAFT_ID = 12345
    const val TEST_STORE_NAME = "TestStore"

    fun createInstalledApp(
        packageName: String = TEST_PACKAGE_NAME,
        name: String = TEST_APP_NAME,
        apkPath: String = TEST_APK_PATH,
        versionCode: Int = 1,
        versionName: String = "1.0"
    ): InstalledApp {
        return InstalledApp().apply {
            this.packageName = packageName
            this.name = name
            this.apkPath = apkPath
            this.versionCode = versionCode
            this.versionName = versionName
            this.packageAndVersionCode = "$packageName-$versionCode"
            this.setIsSystem(false)
        }
    }

    fun createUploadDraft(
        status: UploadDraft.Status = UploadDraft.Status.IN_QUEUE,
        md5: String = TEST_MD5,
        draftId: Int = TEST_DRAFT_ID,
        installedApp: InstalledApp = createInstalledApp(),
        metadata: Metadata? = null
    ): UploadDraft {
        return UploadDraft(status, installedApp, md5, draftId, metadata)
    }

    fun createMetadata(
        name: String = TEST_APP_NAME,
        category: String = "Games",
        description: String = "Test description",
        ageRating: String = "3",
        lang: String = "en"
    ): Metadata {
        return Metadata().apply {
            this.name = name
            this.category = category
            this.description = description
            this.ageRating = ageRating
            this.lang = lang
        }
    }

    fun createAppUploadStatus(
        md5: String = TEST_MD5,
        packageName: String = TEST_PACKAGE_NAME,
        status: AppUploadStatus.Status = AppUploadStatus.Status.UNKNOWN,
        versionCode: Int = 1
    ): AppUploadStatus {
        return AppUploadStatus(md5, packageName, status, versionCode)
    }
}
