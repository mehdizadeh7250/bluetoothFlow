package com.bluettoth.bluetoothflow.domain.useCase

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.bluettoth.bluetoothflow.bluetoothflow.BluetoothDeviceWrapper
import com.bluettoth.bluetoothflow.bluetoothflow.BluetoothFlow
import com.bluettoth.bluetoothflow.utils.BtDiscoveryState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@ExperimentalCoroutinesApi
class DiscoverBtDevicesUseCase(private val blueFlow: BluetoothFlow) {
    private val bondedDevicesList = mutableListOf<BluetoothDeviceWrapper>()
    private val devicesList = mutableListOf<BluetoothDeviceWrapper>()

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getBondedDevices(): List<BluetoothDevice>? {
        return blueFlow.bondedDevices()?.toList()
    }
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscovery(): Boolean {
        if (!blueFlow.isDiscovering())
            return blueFlow.startDiscovery()
        return true
    }
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun cancelDiscovery(): Boolean {
        if (blueFlow.isDiscovering())
            return blueFlow.cancelDiscovery()
        return true
    }
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun discoverBondedDevices() = flow<BtDiscoveryState> {
        bondedDevicesList.clear()
        with(bondedDevicesList) {
            getBondedDevices()?.forEach {
                add(BluetoothDeviceWrapper(it, 0))
                val deviceWrapperList = this.distinctBy { devList -> devList.bluetoothDevice.address }
                emit(BtDiscoveryState.BtDiscoverySuccess(deviceWrapperList))
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun discoverDevices() = flow {
        devicesList.clear()
        cancelDiscovery()

        Log.i(TAG, "IS DISCOVERY ACTUALLY STARTED? " + startDiscovery())

        try {
            blueFlow.discoverDevices().collect { device ->
                Log.i(TAG, "FOUND DEVICE $device")
                val deliveredDevices = with(devicesList) {
                    add(device)
                    distinctBy { it.bluetoothDevice.address }.map { it }
                }
                emit(BtDiscoveryState.BtDiscoverySuccess(deliveredDevices))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(BtDiscoveryState.BtDiscoveryError)
        }
    }
}