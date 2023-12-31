package com.bluettoth.bluetoothflow.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.bluettoth.bluetoothflow.bluetoothflow.BluetoothDeviceWrapper

sealed class BtNavigateState {

    object BtNotAvailableNavigateState : BtNavigateState()
    data class BtShowNativeDialogNavigateState(val callback: BtNativeDialogCallback) : BtNavigateState()
    data class BtEnableSuccessNavigateState(val bluetoothActionEnum: BluetoothActionEnum) : BtNavigateState()
}

sealed class BtDiscoveryState {
    data class BtDiscoverySuccess(val devices: List<BluetoothDeviceWrapper>) : BtDiscoveryState()
    object BtDiscoveryError : BtDiscoveryState()
    object BtDiscoveryTimeOut : BtDiscoveryState()
}

sealed class BtConnection {
    object BtDisconnectedState : BtConnection()
    data class BtConnectingLoadingState(val device: BluetoothDevice) : BtConnection()
    data class BtConnectedState(val socket: BluetoothSocket) : BtConnection()
    object BtErrorConnectingState : BtConnection()
}

interface BtNativeDialogCallback {
    fun yes()
    fun no()
}

enum class BluetoothActionEnum {
    GET_BONDED_DEVICES,
    DISCOVER_DEVICES;
}