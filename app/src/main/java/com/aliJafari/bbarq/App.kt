package com.aliJafari.bbarq

import android.app.Application
import com.google.android.material.color.DynamicColors
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        val config = AppMetricaConfig.newConfigBuilder("8e651fd5-277a-45a6-852f-ecd23aefbb92").build()
        AppMetrica.activate(this, config)
    }
}