package com.bluettoth.bluetoothflow.presentation.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

open class BaseViewModel : ViewModel() {
    var scope = viewModelScope
    var errorLiveData = MutableLiveData<String>()


}
