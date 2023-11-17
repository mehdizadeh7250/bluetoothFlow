package com.bluettoth.bluetoothflow.presentation.activity

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.MutableLiveData
import com.bluettoth.bluetoothflow.domain.useCase.BtConnectUseCase
import com.bluettoth.bluetoothflow.presentation.base.BaseViewModel

class MainActivityViewModel constructor(
    private val btConnectUseCase: BtConnectUseCase
) : BaseViewModel(){

    fun closeConnection() = btConnectUseCase.closeConnection()
    fun disableBluetooth() = btConnectUseCase.disableBluetooth()
    var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothConnectionState = MutableLiveData<Boolean>()


    fun setBluetoothConnectionState(value: Boolean) {
        bluetoothConnectionState.value = value
    }

    fun setBlueFlow(bluetoothSocket: BluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket
    }

}