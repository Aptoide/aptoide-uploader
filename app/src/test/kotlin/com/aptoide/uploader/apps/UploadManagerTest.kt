package com.aptoide.uploader.apps

import com.aptoide.uploader.account.Account
import com.aptoide.uploader.apps.UploadManagerTestFixtures.TEST_MD5
import com.aptoide.uploader.apps.UploadManagerTestFixtures.createInstalledApp
import com.aptoide.uploader.apps.UploadManagerTestFixtures.createMetadata
import com.aptoide.uploader.apps.UploadManagerTestFixtures.createUploadDraft
import com.aptoide.uploader.apps.network.UploaderService
import com.aptoide.uploader.apps.persistence.AppUploadStatusPersistence
import com.aptoide.uploader.apps.persistence.DraftPersistence
import com.aptoide.uploader.testutil.RxSchedulerExtension
import com.aptoide.uploader.upload.AccountProvider
import com.aptoide.uploader.upload.BackgroundService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class UploadManagerTest : BehaviorSpec({

    listener(RxSchedulerExtension())

    // Mock dependencies
    val uploaderService = mockk<UploaderService>()
    val md5Calculator = mockk<Md5Calculator>()
    val backgroundService = mockk<BackgroundService>(relaxed = true)
    val accountProvider = mockk<AccountProvider>()
    val appUploadStatusManager = mockk<AppUploadStatusManager>()
    val appUploadStatusPersistence = mockk<AppUploadStatusPersistence>()
    val draftPersistence = mockk<DraftPersistence>()
    val uploadProgressManager = mockk<UploadProgressManager>()

    // Subjects for reactive streams
    lateinit var draftsSubject: BehaviorSubject<List<UploadDraft>>
    lateinit var accountSubject: BehaviorSubject<Account>

    // Subject under test
    lateinit var uploadManager: UploadManager

    beforeTest {
        clearAllMocks()

        // Initialize subjects
        draftsSubject = BehaviorSubject.createDefault(emptyList())
        accountSubject = BehaviorSubject.create()

        // Setup default mock behaviors
        every { draftPersistence.getDrafts() } returns draftsSubject
        every { draftPersistence.save(any()) } returns Completable.complete()
        every { draftPersistence.remove(any()) } returns Completable.complete()
        every { accountProvider.getAccount() } returns accountSubject
        every { appUploadStatusPersistence.getAppsUnknownUploadStatus() } returns Observable.just(emptyList())
        every { appUploadStatusPersistence.save(any()) } returns Completable.complete()
        every { appUploadStatusPersistence.saveAll(any()) } returns Completable.complete()
        every { appUploadStatusManager.getUncheckedApps() } returns Single.just(emptyList())
        every { backgroundService.enable() } just Runs
        every { backgroundService.disable() } just Runs

        // Create UploadManager instance
        uploadManager = UploadManager(
            uploaderService,
            md5Calculator,
            backgroundService,
            accountProvider,
            appUploadStatusManager,
            appUploadStatusPersistence,
            uploadProgressManager,
            draftPersistence
        )
    }

    // =========================================================================
    // upload(InstalledApp) Tests
    // =========================================================================

    given("the upload method with an InstalledApp") {

        `when`("MD5 calculation succeeds and draft creation succeeds") {
            then("should calculate MD5, start draft, and save to persistence") {
                // Arrange
                val installedApp = createInstalledApp()
                val expectedDraft = createUploadDraft(status = UploadDraft.Status.IN_QUEUE)

                every { md5Calculator.calculate(installedApp.apkPath) } returns Single.just(TEST_MD5)
                every { uploaderService.startUploadDraft(TEST_MD5, installedApp) } returns Single.just(expectedDraft)

                // Act
                val testObserver = uploadManager.upload(installedApp).test()

                // Assert
                testObserver.assertComplete()
                testObserver.assertNoErrors()

                verify(exactly = 1) { md5Calculator.calculate(installedApp.apkPath) }
                verify(exactly = 1) { uploaderService.startUploadDraft(TEST_MD5, installedApp) }
                verify(exactly = 1) { draftPersistence.save(expectedDraft) }
            }
        }

        `when`("MD5 calculation fails") {
            then("should propagate the error without calling uploader service") {
                // Arrange
                val installedApp = createInstalledApp()
                val error = RuntimeException("MD5 calculation failed")

                every { md5Calculator.calculate(installedApp.apkPath) } returns Single.error(error)

                // Act
                val testObserver = uploadManager.upload(installedApp).test()

                // Assert
                testObserver.assertError(error)
                verify(exactly = 0) { uploaderService.startUploadDraft(any(), any()) }
                verify(exactly = 0) { draftPersistence.save(any()) }
            }
        }

        `when`("uploader service fails to start draft") {
            then("should propagate the error without saving to persistence") {
                // Arrange
                val installedApp = createInstalledApp()
                val error = RuntimeException("Service unavailable")

                every { md5Calculator.calculate(installedApp.apkPath) } returns Single.just(TEST_MD5)
                every { uploaderService.startUploadDraft(TEST_MD5, installedApp) } returns Single.error(error)

                // Act
                val testObserver = uploadManager.upload(installedApp).test()

                // Assert
                testObserver.assertError(error)
                verify(exactly = 1) { md5Calculator.calculate(installedApp.apkPath) }
                verify(exactly = 0) { draftPersistence.save(any()) }
            }
        }
    }

    // =========================================================================
    // getDraftsInQueue() Tests
    // =========================================================================

    given("the getDraftsInQueue method") {

        `when`("persistence contains drafts with mixed statuses") {
            then("should return only drafts with IN_QUEUE status") {
                // Arrange
                val queuedDraft = createUploadDraft(
                    status = UploadDraft.Status.IN_QUEUE,
                    md5 = "md5-queued"
                )
                val progressDraft = createUploadDraft(
                    status = UploadDraft.Status.PROGRESS,
                    md5 = "md5-progress"
                )
                val completedDraft = createUploadDraft(
                    status = UploadDraft.Status.COMPLETED,
                    md5 = "md5-completed"
                )

                draftsSubject.onNext(listOf(queuedDraft, progressDraft, completedDraft))

                // Act
                val testObserver = uploadManager.getDraftsInQueue().test()

                // Assert
                testObserver.assertValue { drafts ->
                    drafts.size == 1 &&
                    drafts[0].md5 == "md5-queued" &&
                    drafts[0].status == UploadDraft.Status.IN_QUEUE
                }
            }
        }

        `when`("persistence contains multiple queued drafts") {
            then("should return all queued drafts") {
                // Arrange
                val queuedDraft1 = createUploadDraft(
                    status = UploadDraft.Status.IN_QUEUE,
                    md5 = "md5-queued-1"
                )
                val queuedDraft2 = createUploadDraft(
                    status = UploadDraft.Status.IN_QUEUE,
                    md5 = "md5-queued-2"
                )

                draftsSubject.onNext(listOf(queuedDraft1, queuedDraft2))

                // Act
                val testObserver = uploadManager.getDraftsInQueue().test()

                // Assert
                testObserver.assertValue { drafts ->
                    drafts.size == 2
                }
            }
        }

        `when`("persistence contains no queued drafts") {
            then("should return empty list") {
                // Arrange
                val progressDraft = createUploadDraft(status = UploadDraft.Status.PROGRESS)
                draftsSubject.onNext(listOf(progressDraft))

                // Act
                val testObserver = uploadManager.getDraftsInQueue().test()

                // Assert
                testObserver.assertValue { it.isEmpty() }
            }
        }

        `when`("persistence is empty") {
            then("should return empty list") {
                // Arrange
                draftsSubject.onNext(emptyList())

                // Act
                val testObserver = uploadManager.getDraftsInQueue().test()

                // Assert
                testObserver.assertValue { it.isEmpty() }
            }
        }
    }

    // =========================================================================
    // removeUploadFromPersistence() Tests
    // =========================================================================

    given("the removeUploadFromPersistence method") {

        `when`("called with a valid MD5") {
            then("should call persistence remove with correct MD5") {
                // Arrange
                every { draftPersistence.remove(TEST_MD5) } returns Completable.complete()

                // Act
                val testObserver = uploadManager.removeUploadFromPersistence(TEST_MD5).test()

                // Assert
                testObserver.assertComplete()
                verify(exactly = 1) { draftPersistence.remove(TEST_MD5) }
            }
        }

        `when`("persistence remove fails") {
            then("should propagate the error") {
                // Arrange
                val error = RuntimeException("Database error")
                every { draftPersistence.remove(TEST_MD5) } returns Completable.error(error)

                // Act
                val testObserver = uploadManager.removeUploadFromPersistence(TEST_MD5).test()

                // Assert
                testObserver.assertError(error)
            }
        }
    }

    // =========================================================================
    // handleNoMetadata() Tests
    // =========================================================================

    given("the handleNoMetadata method") {

        `when`("a draft with NO_META_DATA status and matching MD5 exists") {
            then("should update draft to META_DATA_ADDED and set metadata") {
                // Arrange
                val metadata = createMetadata()
                val noMetaDraft = createUploadDraft(
                    status = UploadDraft.Status.NO_META_DATA,
                    md5 = TEST_MD5
                )

                draftsSubject.onNext(listOf(noMetaDraft))

                val savedDraftSlot = slot<UploadDraft>()
                every { draftPersistence.save(capture(savedDraftSlot)) } returns Completable.complete()

                // Act
                val testObserver = uploadManager.handleNoMetadata(metadata, TEST_MD5).test()

                // Assert
                testObserver.assertComplete()
                testObserver.assertNoErrors()

                savedDraftSlot.captured.status shouldBe UploadDraft.Status.META_DATA_ADDED
                savedDraftSlot.captured.metadata shouldBe metadata
                savedDraftSlot.captured.md5 shouldBe TEST_MD5
            }
        }

        `when`("no draft with matching MD5 exists") {
            then("should complete without saving") {
                // Arrange
                val metadata = createMetadata()
                val differentDraft = createUploadDraft(
                    status = UploadDraft.Status.NO_META_DATA,
                    md5 = "different-md5"
                )

                draftsSubject.onNext(listOf(differentDraft))

                // Act
                val testObserver = uploadManager.handleNoMetadata(metadata, TEST_MD5).test()

                // Assert
                testObserver.assertComplete()
                // save should only be called for the different draft that doesn't match
                verify(exactly = 0) { draftPersistence.save(match { it.md5 == TEST_MD5 }) }
            }
        }

        `when`("draft exists but has different status than NO_META_DATA") {
            then("should complete without saving") {
                // Arrange
                val metadata = createMetadata()
                val progressDraft = createUploadDraft(
                    status = UploadDraft.Status.PROGRESS,
                    md5 = TEST_MD5
                )

                draftsSubject.onNext(listOf(progressDraft))

                // Act
                val testObserver = uploadManager.handleNoMetadata(metadata, TEST_MD5).test()

                // Assert
                testObserver.assertComplete()
                verify(exactly = 0) { draftPersistence.save(any()) }
            }
        }

        `when`("persistence is empty") {
            then("should complete without error") {
                // Arrange
                val metadata = createMetadata()
                draftsSubject.onNext(emptyList())

                // Act
                val testObserver = uploadManager.handleNoMetadata(metadata, TEST_MD5).test()

                // Assert
                testObserver.assertComplete()
                verify(exactly = 0) { draftPersistence.save(any()) }
            }
        }
    }

    // =========================================================================
    // getDrafts() Tests
    // =========================================================================

    given("the getDrafts method") {

        `when`("persistence has drafts") {
            then("should return observable from persistence") {
                // Arrange
                val draft1 = createUploadDraft(md5 = "md5-1")
                val draft2 = createUploadDraft(md5 = "md5-2")
                draftsSubject.onNext(listOf(draft1, draft2))

                // Act
                val testObserver = uploadManager.getDrafts().test()

                // Assert
                testObserver.assertValue { drafts ->
                    drafts.size == 2
                }
            }
        }

        `when`("persistence emits multiple updates") {
            then("should emit each update") {
                // Arrange
                val testObserver = uploadManager.getDrafts().test()

                // Act
                draftsSubject.onNext(listOf(createUploadDraft(md5 = "md5-1")))
                draftsSubject.onNext(listOf(createUploadDraft(md5 = "md5-1"), createUploadDraft(md5 = "md5-2")))

                // Assert
                testObserver.assertValueCount(3) // initial empty + 2 updates
            }
        }
    }

    // =========================================================================
    // Background Service Integration Tests (via start())
    // =========================================================================

    given("the start method affecting background service") {

        `when`("uploads are in progress") {
            then("should enable background service") {
                // Arrange
                val progressDraft = createUploadDraft(status = UploadDraft.Status.PROGRESS)
                val loggedOutAccount = mockk<Account> {
                    every { isLoggedIn() } returns false
                }

                accountSubject.onNext(loggedOutAccount)
                draftsSubject.onNext(listOf(progressDraft))

                // Act
                uploadManager.start()

                // Assert
                verify(atLeast = 1) { backgroundService.enable() }
            }
        }

        `when`("no uploads are in progress") {
            then("should disable background service") {
                // Arrange
                val completedDraft = createUploadDraft(status = UploadDraft.Status.COMPLETED)
                val loggedOutAccount = mockk<Account> {
                    every { isLoggedIn() } returns false
                }

                accountSubject.onNext(loggedOutAccount)
                draftsSubject.onNext(listOf(completedDraft))

                // Act
                uploadManager.start()

                // Assert
                verify(atLeast = 1) { backgroundService.disable() }
            }
        }

        `when`("drafts list is empty") {
            then("should disable background service") {
                // Arrange
                val loggedOutAccount = mockk<Account> {
                    every { isLoggedIn() } returns false
                }

                accountSubject.onNext(loggedOutAccount)
                draftsSubject.onNext(emptyList())

                // Act
                uploadManager.start()

                // Assert
                verify(atLeast = 1) { backgroundService.disable() }
            }
        }
    }

    // =========================================================================
    // Completed Status Handling Tests (via start())
    // =========================================================================

    given("completed status handling via start") {

        `when`("a draft becomes COMPLETED") {
            then("should save IN_STORE status to app upload persistence") {
                // Arrange
                val completedDraft = createUploadDraft(
                    status = UploadDraft.Status.COMPLETED,
                    md5 = TEST_MD5
                )
                val loggedOutAccount = mockk<Account> {
                    every { isLoggedIn() } returns false
                }

                val savedStatusSlot = slot<AppUploadStatus>()
                every { appUploadStatusPersistence.save(capture(savedStatusSlot)) } returns Completable.complete()

                accountSubject.onNext(loggedOutAccount)

                // Act
                uploadManager.start()
                draftsSubject.onNext(listOf(completedDraft))

                // Assert
                verify(atLeast = 1) { appUploadStatusPersistence.save(any()) }
                savedStatusSlot.captured.status shouldBe AppUploadStatus.Status.IN_STORE
            }
        }

        `when`("a draft becomes DUPLICATE") {
            then("should save IN_STORE status to app upload persistence") {
                // Arrange
                val duplicateDraft = createUploadDraft(
                    status = UploadDraft.Status.DUPLICATE,
                    md5 = TEST_MD5
                )
                val loggedOutAccount = mockk<Account> {
                    every { isLoggedIn() } returns false
                }

                val savedStatusSlot = slot<AppUploadStatus>()
                every { appUploadStatusPersistence.save(capture(savedStatusSlot)) } returns Completable.complete()

                accountSubject.onNext(loggedOutAccount)

                // Act
                uploadManager.start()
                draftsSubject.onNext(listOf(duplicateDraft))

                // Assert
                verify(atLeast = 1) { appUploadStatusPersistence.save(any()) }
                savedStatusSlot.captured.status shouldBe AppUploadStatus.Status.IN_STORE
            }
        }
    }
})
