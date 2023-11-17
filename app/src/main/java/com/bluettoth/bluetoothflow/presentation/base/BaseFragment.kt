package com.bluettoth.bluetoothflow.presentation.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment


abstract class BaseFragment<T : ViewDataBinding>(@LayoutRes val layoutId: Int) : Fragment() {

    private var requestStartActivityCode: Int? = null
    var onFragmentLauncher: ((resultCode: Int, data: Intent?, requestCode: Int) -> Unit)? = null
    lateinit var binding: T
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.executePendingBindings()
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            requestStartActivityCode?.let {
                onFragmentLauncher?.invoke(
                    result.resultCode,
                    result.data,
                    it
                )
            }
        }

    fun startFragmentForResult(requestCode: Int, intent: Intent) {
        requestStartActivityCode = requestCode
        startForResult.launch(intent)
    }


}
