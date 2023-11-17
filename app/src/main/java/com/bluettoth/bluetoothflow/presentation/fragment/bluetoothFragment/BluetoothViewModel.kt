package com.bluettoth.bluetoothflow.presentation.fragment.bluetoothFragment

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.*
import com.bluettoth.bluetoothflow.bluetoothflow.cancelIfActive
import com.bluettoth.bluetoothflow.domain.useCase.BtConnectUseCase
import com.bluettoth.bluetoothflow.domain.useCase.BtNavigationUseCase
import com.bluettoth.bluetoothflow.domain.useCase.DiscoverBtDevicesUseCase
import com.bluettoth.bluetoothflow.presentation.base.BaseViewModel
import com.bluettoth.bluetoothflow.utils.BluetoothActionEnum
import com.bluettoth.bluetoothflow.utils.BtConnection
import com.bluettoth.bluetoothflow.utils.BtDiscoveryState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ObsoleteCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class BluetoothViewModel constructor(
    private val btNavigationUseCase: BtNavigationUseCase,
    private val btDiscoverUseCase: DiscoverBtDevicesUseCase,
    private val btConnectUseCase: BtConnectUseCase
) : BaseViewModel() {

    private var job: Job? = null
    private val discoverLive = MutableLiveData<BtDiscoveryState>()

    fun initBtNavigation(bluetoothActionEnum: BluetoothActionEnum) {
        btNavigationUseCase.execBtNavigateFlow(bluetoothActionEnum)
    }

    fun discoverBtDevices(): LiveData<BtDiscoveryState> {
        job?.cancelIfActive()
        job = btDiscoverUseCase.discoverDevices().onEach { discoverLive.value = it }.launchIn(viewModelScope)
        return discoverLive
    }

    fun getBondedDevices(): LiveData<BtDiscoveryState> {
        job?.cancelIfActive()
        job = btDiscoverUseCase.discoverBondedDevices().onEach { discoverLive.value = it }.launchIn(viewModelScope)
        return discoverLive
    }

    fun startDiscovery() {
        btDiscoverUseCase.startDiscovery()
    }
    fun enablebluetooth() = btConnectUseCase.enableBluetooth()
    fun cancelDiscovery() {
        btDiscoverUseCase.cancelDiscovery()
    }

    fun connect(device: BluetoothDevice): LiveData<BtConnection> {
        return btConnectUseCase.connect(device).asLiveData()
    }
}