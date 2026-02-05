package com.aptoide.uploader.testutil

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers

class RxSchedulerExtension : TestListener {

    override suspend fun beforeSpec(spec: Spec) {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
    }

    override suspend fun afterSpec(spec: Spec) {
        RxJavaPlugins.reset()
    }
}
