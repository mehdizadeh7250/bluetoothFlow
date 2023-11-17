package com.bluettoth.bluetoothflow.presentation.fragment.bluetoothFragment.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bluettoth.bluetoothflow.bluetoothflow.BluetoothDeviceWrapper
import com.bluettoth.bluetoothflow.databinding.ItemScanBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
@ExperimentalCoroutinesApi
class DiscoverHolder(val binding: ViewBinding) :  RecyclerView.ViewHolder(binding.root) {

    fun bindItem(item: BluetoothDeviceWrapper) {
        (binding as ItemScanBinding).let {
            it.deviceName.text = item.bluetoothDevice.name
            it.deviceMAC.text = item.bluetoothDevice.address
            it.deviceSignal.text = item.rssi.toString()
        }
    }
}