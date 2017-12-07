package com.aptoide.uploader.apps.view

import com.aptoide.uploader.apps.*
import com.aptoide.uploader.view.View
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.rxkotlin.toSingle
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
            val uploaderPersistence = MemoryUploaderPersistence(HashSet<Upload>())
            val md5Calculator = mock<Md5Calculator>()
            val uploadManager = UploadManager(uploadService, uploaderPersistence, md5Calculator)

            val presenter = NotificationPresenter(view, uploadManager)

            val app = InstalledApp("https://myicon.com/facebook", "Facebook", false,
                    "com.facebook.katana")

            whenever(view.lifecycleEvent).doReturn(lifecycleEvent)
            val md5 = "asdasdasd"
            whenever(md5Calculator.calculate(app)).doReturn(md5.toSingle())
            val upload = Upload(true, true, app, Upload.Status.COMPLETED)
            whenever(uploadService.getAppUpload(md5, "com.facebook.katana", "en", "FabioStore"))
                    .doReturn(upload.toSingle())

            presenter.present()
            lifecycleEvent.onNext(View.LifecycleEvent.CREATE)

            uploadManager.upload("FabioStore", "en", app).subscribe()


            verify(view).showCompletedUploadNotification(upload)
        }
    }
})
