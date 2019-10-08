package com.aptoide.uploader.apps.view

import org.jetbrains.spek.api.Spek
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class NotificationPresenterTest : Spek({
  //    describe("a notification presenter") {
//    it("should display duplicated uploaded app notification when an app is uploaded and it exists already on store") {
//        val lifecycleEvent = PublishSubject.create<View.LifecycleEvent>()
//
//        val view = mock<NotificationView>()
//        val serviceV3 = mock<RetrofitUploadService.ServiceV3>()
//        val serviceV7 = mock<RetrofitUploadService.ServiceV7>()
//        val uploadAccountProvider = mock<AccountProvider>()
//        val uploadService = RetrofitUploadService(serviceV7, serviceV3, uploadAccountProvider,
//            RetrofitUploadService.UploadType.APTOIDE_UPLOADER)
//        val appInfoService = mock<RemoteAppInformationService>()
//        val uploaderPersistence = MemoryUploaderPersistence(HashMap(), Schedulers.trampoline())
//        val md5Calculator = mock<Md5Calculator>()
//        val backgroundRunner = mock<BackgroundService>()
//        val getProposedMissingProposed = mock<Response<GetProposedResponse>>()
//        val getUploadDuplicatedResponse = mock<Response<UploadAppToRepoResponse>>()
//
//        val uploadManager = UploadManager(uploadService, uploaderPersistence, md5Calculator, backgroundRunner, uploadAccountProvider)
//
//        val presenter = NotificationPresenter(view, uploadManager)
//
//        val appPackageName = "com.facebook.katana"
//        val appIcon = "https://myicon.com/facebook"
//        val appName = "Facebook"
//        val appMd5 = "asdasdasd"
//        val appLanguage = "en"
//        val apkPath = "/Files/facebook.apk"
//        val storeName = "FabioStore"
//        val accessToken = "sdjahsd"
//
//        val app = InstalledApp(appIcon, appName, false, appPackageName, apkPath, 0, 123)
//        val upload = Upload(true, true, app, Upload.Status.PENDING, appMd5, storeName)
//
//        whenever(serviceV7.getProposed(appPackageName, appLanguage, false))
//            .doReturn(getProposedMissingProposed.toSingle().toObservable())
//        whenever(getProposedMissingProposed.body()).doReturn(GetProposedResponse(null,
//            mutableListOf(com.aptoide.uploader.account.network.Error("APK-103", "description"))))
//
//        whenever(view.lifecycleEvent)
//            .doReturn(lifecycleEvent)
//        whenever(appInfoService.getProposedAppInfo(appPackageName, appLanguage))
//            .doReturn(Single.error<RemoteProposedAppInfo>(ProposedAppInfoException()))
//        whenever(md5Calculator.calculate(app))
//            .doReturn(appMd5.toSingle())
//
//        val uploadAppToRepoResponse = UploadAppToRepoResponse()
//        uploadAppToRepoResponse.errors = mutableListOf(com.aptoide.uploader.account.network.Error("APK-103", "description"))
//        uploadAppToRepoResponse.status = Status.FAIL
//
//        whenever(getUploadDuplicatedResponse.body()).doReturn(uploadAppToRepoResponse)
//
//
//        whenever(serviceV3.uploadAppToRepo(any()))
//            .doReturn(getUploadDuplicatedResponse.toSingle().toObservable())
//        whenever(uploadAccountProvider.token).doReturn(accessToken.toSingle())
//        whenever(uploadAccountProvider.account).doReturn(Account(true, true, storeName).toSingle().toObservable())
//        uploadManager.start()
//        presenter.present()
//        lifecycleEvent.onNext(View.LifecycleEvent.CREATE)
//
//        uploaderPersistence.save(upload).subscribe()
//
//        verify(view).showDuplicateUploadNotification(upload.installedApp.name, upload.installedApp.packageName)
//    }
//}
})
