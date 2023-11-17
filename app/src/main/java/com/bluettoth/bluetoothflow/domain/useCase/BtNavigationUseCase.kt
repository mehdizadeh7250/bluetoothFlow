package com.bluettoth.bluetoothflow.domain.useCase

import com.bluettoth.bluetoothflow.bluetoothflow.BluetoothFlow
import com.bluettoth.bluetoothflow.presentation.fragment.bluetoothFragment.EventWrapper
import com.bluettoth.bluetoothflow.utils.BluetoothActionEnum
import com.bluettoth.bluetoothflow.utils.BtNativeDialogCallback
import com.bluettoth.bluetoothflow.utils.BtNavigateState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel


@ExperimentalCoroutinesApi
class BtNavigationUseCase(private val blueFlow: BluetoothFlow) {

    val btNavigateChannel: BroadcastChannel<EventWrapper<BtNavigateState>> = ConflatedBroadcastChannel()

    @OptIn(ObsoleteCoroutinesApi::class)
    fun execBtNavigateFlow(bluetoothActionEnum: BluetoothActionEnum) {

        if (!blueFlow.isBluetoothAvailable()) {
            btNavigateChannel.trySend(EventWrapper(BtNavigateState.BtNotAvailableNavigateState)).isSuccess
            return
        }

        if (!blueFlow.isBluetoothEnabled()) {

            val listener = object : BtNativeDialogCallback {
                override fun yes() {
                    btNavigateChannel.trySend(
                        EventWrapper(
                            BtNavigateState.BtEnableSuccessNavigateState(
                                bluetoothActionEnum
                            )
                        )
                    ).isSuccess
                }

                override fun no() {

                }
            }
            btNavigateChannel.trySend(
                EventWrapper(BtNavigateState.BtShowNativeDialogNavigateState(listener))
            ).isSuccess
        } else {
            btNavigateChannel.trySend(
                EventWrapper(
                    BtNavigateState.BtEnableSuccessNavigateState(
                        bluetoothActionEnum
                    )
                )
            ).isSuccess
        }
    }
}
