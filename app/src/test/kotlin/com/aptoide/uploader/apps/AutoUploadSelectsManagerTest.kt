package com.aptoide.uploader.apps

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.aptoide.uploader.apps.persistence.AutoUploadSelectsPersistence
import com.aptoide.uploader.testutil.RxSchedulerExtension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.Completable

class AutoUploadSelectsManagerTest : BehaviorSpec({

    listener(RxSchedulerExtension())

    val persistence = mockk<AutoUploadSelectsPersistence>()
    val packageManager = mockk<PackageManager>()

    lateinit var manager: AutoUploadSelectsManager

    beforeTest {
        clearAllMocks()
        manager = AutoUploadSelectsManager(persistence, packageManager)
    }

    given("insertAllInstalled") {

        `when`("user-installed apps are present") {
            then("should sync them with isSelectedAutoUpload = false") {
                // Arrange
                val userApp = ApplicationInfo().apply {
                    packageName = "com.test.userapp"
                    flags = 0
                }
                val userApp2 = ApplicationInfo().apply {
                    packageName = "com.test.userapp2"
                    flags = 0
                }

                every { packageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns
                    listOf(userApp, userApp2)
                every { packageManager.getPackageInfo("com.test.userapp", 0) } returns
                    PackageInfo().apply { packageName = "com.test.userapp" }
                every { packageManager.getPackageInfo("com.test.userapp2", 0) } returns
                    PackageInfo().apply { packageName = "com.test.userapp2" }

                val listSlot = slot<List<AutoUploadSelects>>()
                every { persistence.syncInstalled(capture(listSlot)) } returns Completable.complete()

                // Act
                val testObserver = manager.insertAllInstalled().test()

                // Assert
                testObserver.assertComplete()
                testObserver.assertNoErrors()
                listSlot.captured.size shouldBe 2
                listSlot.captured.all { !it.isSelectedAutoUpload } shouldBe true
                listSlot.captured.map { it.packageName } shouldBe
                    listOf("com.test.userapp", "com.test.userapp2")
            }
        }

        `when`("all apps are system apps") {
            then("should sync with empty list") {
                // Arrange
                val systemApp = ApplicationInfo().apply {
                    packageName = "com.android.system"
                    flags = ApplicationInfo.FLAG_SYSTEM
                }

                every { packageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns
                    listOf(systemApp)

                val listSlot = slot<List<AutoUploadSelects>>()
                every { persistence.syncInstalled(capture(listSlot)) } returns Completable.complete()

                // Act
                val testObserver = manager.insertAllInstalled().test()

                // Assert
                testObserver.assertComplete()
                listSlot.captured.size shouldBe 0
            }
        }

        `when`("mix of system and user apps") {
            then("should only sync user apps") {
                // Arrange
                val systemApp = ApplicationInfo().apply {
                    packageName = "com.android.system"
                    flags = ApplicationInfo.FLAG_SYSTEM
                }
                val userApp = ApplicationInfo().apply {
                    packageName = "com.test.userapp"
                    flags = 0
                }

                every { packageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns
                    listOf(systemApp, userApp)
                every { packageManager.getPackageInfo("com.test.userapp", 0) } returns
                    PackageInfo().apply { packageName = "com.test.userapp" }

                val listSlot = slot<List<AutoUploadSelects>>()
                every { persistence.syncInstalled(capture(listSlot)) } returns Completable.complete()

                // Act
                val testObserver = manager.insertAllInstalled().test()

                // Assert
                testObserver.assertComplete()
                listSlot.captured.size shouldBe 1
                listSlot.captured[0].packageName shouldBe "com.test.userapp"
            }
        }

        `when`("no apps installed") {
            then("should sync with empty list") {
                // Arrange
                every { packageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns
                    emptyList()

                val listSlot = slot<List<AutoUploadSelects>>()
                every { persistence.syncInstalled(capture(listSlot)) } returns Completable.complete()

                // Act
                val testObserver = manager.insertAllInstalled().test()

                // Assert
                testObserver.assertComplete()
                listSlot.captured.size shouldBe 0
            }
        }

        `when`("persistence fails") {
            then("should propagate the error") {
                // Arrange
                val userApp = ApplicationInfo().apply {
                    packageName = "com.test.userapp"
                    flags = 0
                }

                every { packageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns
                    listOf(userApp)
                every { packageManager.getPackageInfo("com.test.userapp", 0) } returns
                    PackageInfo().apply { packageName = "com.test.userapp" }

                val error = RuntimeException("db error")
                every { persistence.syncInstalled(any()) } returns Completable.error(error)

                // Act
                val testObserver = manager.insertAllInstalled().test()

                // Assert
                testObserver.assertError(error)
                verify(exactly = 1) { persistence.syncInstalled(any()) }
            }
        }
    }
})
