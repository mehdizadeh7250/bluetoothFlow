package com.bluettoth.bluetoothflow.bluetoothflow

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.util.*

fun BluetoothDevice.createRfcommSocket(channel: Int): BluetoothSocket {
    try {
        val method = this.javaClass.getMethod("createRfcommSocket", Integer.TYPE)
        return method.invoke(this, channel) as BluetoothSocket
    } catch (e: Exception) {
        throw UnsupportedOperationException(e)
    }
}

fun BluetoothDevice.isConnected(): Boolean {
    try {
        val method = this.javaClass.getMethod("isConnected")
        return method.invoke(this) as Boolean
    } catch (e: Exception) {
        throw UnsupportedOperationException(e)
    }
}

@ExperimentalCoroutinesApi
fun BluetoothAdapter.discoverDevices(context: Context) =
    BluetoothFlow.getInstance(context).discoverDevices()
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission( Manifest.permission.BLUETOOTH_CONNECT)
suspend fun BluetoothDevice.connectAsClientAsync(uuid: UUID, secure: Boolean = true) =
    coroutineScope {
        return@coroutineScope async(Dispatchers.IO) {
            val bluetoothSocket =


                if (secure) createRfcommSocketToServiceRecord(uuid)
                else createInsecureRfcommSocketToServiceRecord(uuid)
            bluetoothSocket.also { it.connect() }
        }
    }

@ExperimentalCoroutinesApi
fun BluetoothSocket.readByteStream() = channelFlow {
    while (isActive) {
        try {
            this.trySend(inputStream.read().toByte()).isSuccess
        } catch (e: IOException) {
            error("Couldn't read bytes from flow. Disconnected")
        }
    }
}.flowOn(Dispatchers.IO)

@ExperimentalCoroutinesApi
fun BluetoothSocket.readByteArrayStream(
    delayMillis: Long = 1000,
    minExpectedBytes: Int = 2,
    bufferCapacity: Int = 1024,
    readInterceptor: (ByteArray) -> ByteArray? = { it }
): Flow<ByteArray> = channelFlow {
    if (inputStream == null) {
        throw NullPointerException("inputStream is null. Perhaps bluetoothSocket is also null")
    }
    val buffer = ByteArray(bufferCapacity)
    val byteAccumulatorList = mutableListOf<Byte>()
    while (isActive) {
        try {
            if (inputStream.available() < minExpectedBytes) {
                delay(delayMillis)
                continue
            }
            val numBytes = inputStream.read(buffer)
            val readBytes = buffer.trim(numBytes)
            if (byteAccumulatorList.size >= bufferCapacity)
                byteAccumulatorList.clear()

            byteAccumulatorList.addAll(readBytes.toList())
            val interceptor = readInterceptor(byteAccumulatorList.toByteArray())

            if (interceptor == null)
                delay(delayMillis)

            interceptor?.let {
                this.trySend(it).isSuccess
                byteAccumulatorList.clear()
            }
        } catch (e: IOException) {
            byteAccumulatorList.clear()
            close()
            error("Couldn't read bytes from flow. Disconnected")
        }
    }
}.flowOn(Dispatchers.IO)

fun BluetoothSocket.send(bytes: ByteArray): Boolean {

    if (!isConnected) return false

    return try {
        outputStream.write(bytes)
        outputStream.flush()
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}