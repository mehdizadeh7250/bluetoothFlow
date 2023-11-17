package com.bluettoth.bluetoothflow.presentation.activity

import android.app.Dialog
import android.os.Bundle
import androidx.navigation.NavController
import com.bluettoth.bluetoothflow.R
import com.bluettoth.bluetoothflow.databinding.ActivityMainBinding
import com.bluettoth.bluetoothflow.presentation.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {
    var navHostController: NavController? = null
    var dialog : Dialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

}
