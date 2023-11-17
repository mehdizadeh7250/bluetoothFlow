package com.bluettoth.bluetoothflow.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluettoth.bluetoothflow.domain.useCase.BtConnectUseCase
import com.bluettoth.bluetoothflow.domain.useCase.BtNavigationUseCase
import com.bluettoth.bluetoothflow.domain.useCase.DiscoverBtDevicesUseCase
import com.bluettoth.bluetoothflow.presentation.fragment.bluetoothFragment.BluetoothViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi

@FlowPreview
@ExperimentalCoroutinesApi
class ViewModelFactory(
    private val btNavigationUseCase: BtNavigationUseCase,
    private val btDiscoverUseCase: DiscoverBtDevicesUseCase,
    private val btConnectUseCase: BtConnectUseCase
) : ViewModelProvider.Factory {
    @OptIn(ObsoleteCoroutinesApi::class)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BluetoothViewModel(btNavigationUseCase, btDiscoverUseCase, btConnectUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}