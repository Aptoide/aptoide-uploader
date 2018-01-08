package com.aptoide.uploader.apps.view

import com.aptoide.uploader.apps.*
import com.aptoide.uploader.apps.network.UploaderService
import com.aptoide.uploader.apps.persistence.MemoryUploaderPersistence
import com.aptoide.uploader.upload.BackgroundService
import com.aptoide.uploader.view.View
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class NotificationPresenterTest : Spek({
    describe("a notification presenter") {
        it("should display successful uploaded app notification when an app is uploaded successfully") {
            val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()

            val view = mock<NotificationView>()
            val uploadService = mock<UploaderService>()
            val appInfoService = mock<RemoteAppInformationService>()
            val uploaderPersistence = MemoryUploaderPersistence(mutableMapOf(), Schedulers.trampoline())
            val md5Calculator = mock<Md5Calculator>()
            val backgroundRunner = mock<BackgroundService>()
            val uploadManager = UploadManager(uploadService, uploaderPersistence, md5Calculator, backgroundRunner)

            val presenter = NotificationPresenter(view, uploadManager)

            val appPackageName = "com.facebook.katana"
            val appIcon = "https://myicon.com/facebook"
            val appName = "Facebook"
            val appMd5 = "asdasdasd"
            val appLanguage = "en"
            val apkPath = "/Files/facebook.apk"
            val storeName = "FabioStore"

            val app = InstalledApp(appIcon, appName, false, appPackageName, apkPath, 0, 123)
            val upload = Upload(true, true, app, Upload.Status.COMPLETED)

            whenever(view.lifecycleEvent)
                    .doReturn(lifecycleEvent)
            whenever(appInfoService.getProposedAppInfo(appPackageName, appLanguage))
                    .doReturn(Single.error<RemoteProposedAppInfo>(ProposedAppInfoException()))
            whenever(md5Calculator.calculate(app))
                    .doReturn(appMd5.toSingle())
            whenever(uploadService.getAppUpload(appMd5, appLanguage, storeName, app))
                    .doReturn(upload.toSingle())

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)

            uploadManager.upload(storeName, appLanguage, app).subscribe()

            verify(view).showCompletedUploadNotification(upload)
        }
    }
})
