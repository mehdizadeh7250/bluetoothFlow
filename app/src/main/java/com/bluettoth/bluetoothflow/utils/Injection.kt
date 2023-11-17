package com.bluettoth.bluetoothflow.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.bluettoth.bluetoothflow.bluetoothflow.BluetoothFlow
import com.bluettoth.bluetoothflow.domain.useCase.BtConnectUseCase
import com.bluettoth.bluetoothflow.domain.useCase.BtNavigationUseCase
import com.bluettoth.bluetoothflow.domain.useCase.DiscoverBtDevicesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@SuppressLint("StaticFieldLeak")
@FlowPreview
@ExperimentalCoroutinesApi
object Injection {

    private var context: Context? = null

    fun provideContext(context: Context) {
        this.context = context
    }

    private fun provideBtNavigationUseCases() = BtNavigationUseCase(
        BluetoothFlow.getInstance(
            context ?: throw NullPointerException("Context not provided")
        )
    )

    private fun provideBtDiscoveryUseCase() = DiscoverBtDevicesUseCase(
        BluetoothFlow.getInstance(
            context ?: throw NullPointerException("Context not provided")
        )
    )

    private fun provideBtConnectUseCase() = BtConnectUseCase(
        BluetoothFlow.getInstance(
            context ?: throw NullPointerException("Context not provided")
        )
    )

    /**
     * Provides the [ViewModelProvider.Factory] that is then used to get a reference to
     * [ViewModel] objects.
     */
    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return ViewModelFactory(
            provideBtNavigationUseCases(), provideBtDiscoveryUseCase(), provideBtConnectUseCase()
        )
    }
}
