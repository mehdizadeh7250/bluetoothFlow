package com.bluettoth.bluetoothflow.presentation.fragment.bluetoothFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bluettoth.bluetoothflow.bluetoothflow.BluetoothDeviceWrapper
import com.bluettoth.bluetoothflow.databinding.ItemScanBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class DiscoverRecyclerAdapter : ListAdapter<BluetoothDeviceWrapper, DiscoverHolder>(DeviceDiffCallback()) {

    var itemclicks: ((BluetoothDeviceWrapper) -> Unit)? = null

    fun setOnItemClickListener(itemclicks: (BluetoothDeviceWrapper) -> Unit) {
        this.itemclicks = itemclicks
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverHolder {
        val inflater = LayoutInflater.from(parent.context)
        return  DiscoverHolder(
                ItemScanBinding.inflate(inflater, parent, false)
            ).apply {
            itemView.setOnClickListener { itemclicks?.invoke(getItem(adapterPosition)) }
        }
    }

    override fun onBindViewHolder(holder: DiscoverHolder, position: Int) {
        holder.bindItem(getItem(position))
    }

    private class DeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDeviceWrapper>() {
        override fun areItemsTheSame(oldItem: BluetoothDeviceWrapper, newItem: BluetoothDeviceWrapper): Boolean {
            return oldItem.bluetoothDevice.address == newItem.bluetoothDevice.address
        }

        override fun areContentsTheSame(oldItem: BluetoothDeviceWrapper, newItem: BluetoothDeviceWrapper): Boolean {
            return oldItem.bluetoothDevice == newItem.bluetoothDevice
        }
    }
}