package com.bluettoth.bluetoothflow

import android.app.Application
import android.content.Context
import com.bluettoth.bluetoothflow.utils.Injection
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Injection.provideContext(this)
    }
}