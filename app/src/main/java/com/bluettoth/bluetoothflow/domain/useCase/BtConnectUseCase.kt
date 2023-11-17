package com.bluettoth.bluetoothflow.domain.useCase

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.bluettoth.bluetoothflow.bluetoothflow.BluetoothFlow
import com.bluettoth.bluetoothflow.utils.BtConnection
import kotlinx.coroutines.flow.flow
import java.util.*

class BtConnectUseCase(private val blueFlow: BluetoothFlow) {

    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var socket: BluetoothSocket? = null
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)

    fun connect(device: BluetoothDevice) = flow {
        emit(BtConnection.BtConnectingLoadingState(device))
        try {
            socket = blueFlow.connectAsClientAsync(device, uuid).await()
            emit(BtConnection.BtConnectedState(socket = socket!!))
        } catch (e: Exception) {
            emit(BtConnection.BtErrorConnectingState)
        }
    }
    fun enableBluetooth() = blueFlow.enable()
    fun disableBluetooth() = blueFlow.disable()
    fun bluetoothIsEnabled() = blueFlow.isBluetoothEnabled()
    fun isConnected() = socket?.isConnected
    fun closeConnection() = blueFlow.closeConnections()
}