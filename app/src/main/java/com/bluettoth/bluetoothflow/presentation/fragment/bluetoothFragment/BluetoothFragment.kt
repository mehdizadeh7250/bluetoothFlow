package com.bluettoth.bluetoothflow.presentation.fragment.bluetoothFragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluettoth.bluetoothflow.R
import com.bluettoth.bluetoothflow.databinding.FragmentBluetoothBinding
import com.bluettoth.bluetoothflow.presentation.activity.MainActivityViewModel
import com.bluettoth.bluetoothflow.presentation.base.BaseFragment
import com.bluettoth.bluetoothflow.presentation.fragment.bluetoothFragment.adapter.DiscoverRecyclerAdapter
import com.bluettoth.bluetoothflow.utils.BluetoothActionEnum
import com.bluettoth.bluetoothflow.utils.BtConnection
import com.bluettoth.bluetoothflow.utils.BtDiscoveryState
import com.bluettoth.bluetoothflow.utils.Injection
import com.bluettoth.bluetoothflow.utils.permissions.PermissionUtil
import com.bluettoth.bluetoothflow.utils.permissions.PermissionUtil.requestPermissions
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class BluetoothFragment :
    BaseFragment<FragmentBluetoothBinding>(R.layout.fragment_bluetooth) {

    var loading: Dialog? = null
    private var adapter: DiscoverRecyclerAdapter? = null
    private var showCameraLauncher: PermissionUtil.PermissionLauncher? = null
    protected val viewModel: BluetoothViewModel by lazy {
        ViewModelProvider(this, Injection.provideViewModelFactory()).get(
            BluetoothViewModel::class.java
        )
    }
    var bluetoothActionEnum: BluetoothActionEnum = BluetoothActionEnum.GET_BONDED_DEVICES
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DiscoverRecyclerAdapter()
        adapter?.setOnItemClickListener {

                viewModel.connect(it.bluetoothDevice)
                    .observe(viewLifecycleOwner) {
                        checkBtConnectionState(it)
                    }
        }
        onFragmentLauncher = { resultCode, data, requestCode ->
            if (resultCode == Activity.RESULT_OK) {
                loading?.show()
                lifecycleScope.launch {
                    viewModel.enablebluetooth()
                    delay(1000)
                    executeBtCommand(bluetoothActionEnum)
                }

            }
        }
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerScan.setHasFixedSize(true)
        binding.recyclerScan.layoutManager = layoutManager
        binding.recyclerScan.adapter = adapter

        binding.btnBounded.setOnClickListener {
            bluetoothActionEnum = BluetoothActionEnum.GET_BONDED_DEVICES
            openCameraWithPermissionCheck()
        }

        binding.btnStart.setOnClickListener {
            bluetoothActionEnum = BluetoothActionEnum.DISCOVER_DEVICES
            openCameraWithPermissionCheck()
        }

        registerPermissionForCamera()
    }


    private fun observeDeviceDiscoveryState(btDiscoveryState: BtDiscoveryState) {
        when (btDiscoveryState) {
            is BtDiscoveryState.BtDiscoverySuccess -> {
                adapter?.submitList(btDiscoveryState.devices)
            }

            else -> {
                Toast.makeText(requireContext(), "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun executeBtCommand(params: BluetoothActionEnum) {
        when (params) {
            BluetoothActionEnum.DISCOVER_DEVICES -> {
                viewModel.discoverBtDevices()
                    .observe(viewLifecycleOwner) { observeDeviceDiscoveryState(it) }
            }

            BluetoothActionEnum.GET_BONDED_DEVICES -> {
                viewModel.getBondedDevices()
                    .observe(viewLifecycleOwner) { observeDeviceDiscoveryState(it) }
            }
        }
    }

    private fun checkBtConnectionState(btConnection: BtConnection) {
        when (btConnection) {
            is BtConnection.BtConnectingLoadingState -> {
                Toast.makeText(requireContext(), "CONNECTING PLEASE WAIT...", Toast.LENGTH_LONG).show()
            }
            is BtConnection.BtConnectedState -> {
                Toast.makeText(requireContext(), "CONNECTED", Toast.LENGTH_LONG).show()
            }
            is BtConnection.BtErrorConnectingState -> {
                Toast.makeText(requireContext(), "SOMETHING WENT WRONG WHILE CONNECTING", Toast.LENGTH_LONG).show()
            }
            is BtConnection.BtDisconnectedState -> {
                Toast.makeText(requireContext(), "DISCONNECTED", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        loading?.dismiss()
    }




    // Called when clicking on a device entry to start the CommunicateActivity

    private fun openCameraWithPermissionCheck() {
        showCameraLauncher?.launch()
    }

    val REQUEST_ENABLE_BT = 1001
    private fun registerPermissionForCamera() {
        showCameraLauncher = requestPermissions(
            permissions = if (PermissionUtil.isSdk31OrAbove()) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        ).callbacks {
            onAllow {
                if (PermissionUtil.isSdk33OrAbove()) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startFragmentForResult(REQUEST_ENABLE_BT, enableBtIntent)
                } else {
                    lifecycleScope.launch {
                        viewModel.enablebluetooth()
                        delay(1000)
                        executeBtCommand(bluetoothActionEnum)
                    }
                }

            }
            onDeny {
            }
            onNeverAsk {
            }
            onShowRationale { _, request ->
            }
        }
    }

    override fun onStop() {
        super.onStop()
        loading?.dismiss()
        viewModel.cancelDiscovery()
    }
}