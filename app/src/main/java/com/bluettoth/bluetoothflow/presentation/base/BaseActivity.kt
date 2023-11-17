package com.bluettoth.bluetoothflow.presentation.base

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<T : ViewDataBinding>(@LayoutRes val layoutId: Int) :
    AppCompatActivity() {
    lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutId)
    }

    fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    var requestCodeForStartActivity: Int? = null
    var onActivityLauncher: ((resultCode: Int, data: Intent?, requestCode: Int) -> Unit)? = null
    private var registerActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            requestCodeForStartActivity?.let {
                onActivityLauncher?.invoke(
                    result.resultCode,
                    result.data,
                    it
                )
            }
        }

    fun startActivityForResult(requestCode: Int, intent: Intent) {
        requestCodeForStartActivity = requestCode
        registerActivityForResult.launch(intent)
    }
}
