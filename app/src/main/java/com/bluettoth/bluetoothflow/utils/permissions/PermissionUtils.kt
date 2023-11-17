package com.bluettoth.bluetoothflow.utils.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

typealias OnAllow = ((requestCode: Int?) -> Unit)?
typealias OnDeny = ((requestCode: Int?) -> Unit)?
typealias OnNeverAsk = ((requestCode: Int?) -> Unit)?
typealias OnShowRationale = (
    (requestCode: Int?, request: PermissionUtil.RuntimePermissionRequest) -> Unit
)?

object PermissionUtil {

    private val MIN_SDK_PERMISSIONS = mapOf(
        Manifest.permission.ADD_VOICEMAIL to 14,
        Manifest.permission.BODY_SENSORS to 20,
        Manifest.permission.READ_CALL_LOG to 16,
        Manifest.permission.WRITE_CALL_LOG to 16,
        Manifest.permission.USE_SIP to 9,
        Manifest.permission.SYSTEM_ALERT_WINDOW to 23,
        Manifest.permission.WRITE_SETTINGS to 23,
        Manifest.permission.READ_EXTERNAL_STORAGE to 16
    )

    fun hasWriteExternalStoragePermission(context: Context?): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager())
    }

    fun verifyPermissions(vararg grantResults: Int): Boolean {
        return if (grantResults.isEmpty()) {
            false
        } else {
            for (i in grantResults.indices) {
                val result = grantResults[i]
                if (result != PermissionChecker.PERMISSION_GRANTED) {
                    return false
                }
            }
            true
        }
    }

    fun hasSelfPermissions(context: Context?, vararg permissions: String): Boolean {
        for (i in permissions.indices) {
            val permission = permissions[i]
            if (permissionExists(permission) && !hasSelfPermission(context, permission)) {
                return false
            }
        }
        return true
    }

    private fun permissionExists(permission: String): Boolean {
        val minVersion = MIN_SDK_PERMISSIONS[permission]
        return minVersion == null || Build.VERSION.SDK_INT >= minVersion
    }

    private fun hasSelfPermission(context: Context?, permission: String): Boolean {
        return try {
            PermissionChecker.checkSelfPermission(
                context!!,
                permission
            ) == PermissionChecker.PERMISSION_GRANTED
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
            false
        }
    }

    fun shouldShowRequestPermissionRationale(
        activity: Activity,
        vararg permissions: String
    ): Boolean {
        for (i in permissions.indices) {
            val permission = permissions[i]
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true
            }
        }
        return false
    }

    fun isSdk30OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }
    fun isSdk31OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    fun isSdk33OrAbove():Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
    class PermissionLauncher(
        private val request: RuntimePermission,
        private val resultLauncher: ActivityResultLauncher<Array<String>>
    ) {
        fun launch(requestCode: Int = 0) {
            request.requestCode = requestCode
            if (hasSelfPermissions(request.activity, *request.permissions)) {
                request.permissionCallbacks?.onAllow?.invoke(requestCode)
            } else {
                val proceed = {
                    request.isFirstPermissionRequest = false
                    resultLauncher.launch(request.permissions)
                }
                val cancel = {
                    request.permissionCallbacks?.onDeny?.invoke(requestCode) ?: Unit
                }
                if (request.isFirstPermissionRequest || shouldShowRequestPermissionRationale(
                        request.activity,
                        *request.permissions
                    )
                ) {
                    request.permissionCallbacks?.onShowRationale?.invoke(
                        requestCode,
                        RuntimePermissionRequest(proceed, cancel)
                    )
                        ?: proceed.invoke()
                } else {
                    proceed.invoke()
                }
            }
        }

        fun callbacks(action: PermissionCallbacks.() -> Unit): PermissionLauncher {
            request.permissionCallbacks = PermissionCallbacks()
            request.permissionCallbacks?.action()
            return this
        }
    }

    class StoragePermissionLauncher(
        private val request: RuntimePermission,
        private val activityResultPermissionLauncher: ActivityResultLauncher<Array<String>>,
        private val activityResultLauncherIntent: ActivityResultLauncher<Intent>
    ) {

        fun launch(isPublicDirectory: Boolean, requestCode: Int = 0) {
            request.requestCode = requestCode
            if ((isPublicDirectory && isSdk30OrAbove()) || hasWriteExternalStoragePermission(
                    request.activity
                )
            ) {
                request.permissionCallbacks?.onAllow?.invoke(requestCode)
            } else {
                val proceed = {
                    request.isFirstPermissionRequest = false

                    if (isSdk30OrAbove()) {
                        activityResultLauncherIntent.launch(
                            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        )
                    } else {
                        activityResultPermissionLauncher.launch(request.permissions)
                    }
                }
                val cancel = {
                    request.permissionCallbacks?.onDeny?.invoke(requestCode) ?: Unit
                }
                if (isSdk30OrAbove() || request.isFirstPermissionRequest || shouldShowRequestPermissionRationale(
                        request.activity,
                        *request.permissions
                    )
                ) {
                    request.permissionCallbacks?.onShowRationale?.invoke(
                        requestCode,
                        RuntimePermissionRequest(proceed, cancel)
                    )
                        ?: proceed.invoke()
                } else {
                    proceed.invoke()
                }
            }
        }

        fun callbacks(action: PermissionCallbacks.() -> Unit): StoragePermissionLauncher {
            request.permissionCallbacks = PermissionCallbacks()
            request.permissionCallbacks?.action()
            return this
        }
    }

    fun Fragment.requestPermissions(permissions: Array<String>): PermissionLauncher {
        val request = RuntimePermission(
            activity = requireActivity(),
            permissions = permissions
        )
        return PermissionLauncher(request, createLauncherPermissionResult(request))
    }

    fun FragmentActivity.requestPermissions(permissions: Array<String>): PermissionLauncher {
        val request = RuntimePermission(
            activity = this,
            permissions = permissions
        )
        return PermissionLauncher(request, createLauncherPermissionResult(request))
    }

    fun AppCompatActivity.requestPermissions(permissions: Array<String>): PermissionLauncher {
        val request = RuntimePermission(
            activity = this,
            permissions = permissions
        )
        return PermissionLauncher(request, createLauncherPermissionResult(request))
    }

    fun ComponentActivity.requestPermissions(permissions: Array<String>): PermissionLauncher {
        val request = RuntimePermission(
            activity = this,
            permissions = permissions
        )
        return PermissionLauncher(request, createLauncherPermissionResult(request))
    }

    fun Fragment.requestWriteStoragePermissions(): StoragePermissionLauncher {
        val request = RuntimePermission(
            activity = requireActivity(),
            permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        return StoragePermissionLauncher(
            request,
            createLauncherPermissionResult(request),
            createLauncherStartActivityResult(request)
        )
    }

    fun FragmentActivity.requestWriteStoragePermissions(): StoragePermissionLauncher {
        val request = RuntimePermission(
            activity = this,
            permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        return StoragePermissionLauncher(
            request,
            createLauncherPermissionResult(request),
            createLauncherStartActivityResult(request)
        )
    }

    fun AppCompatActivity.requestWriteStoragePermissions(): StoragePermissionLauncher {
        val request = RuntimePermission(
            activity = this,
            permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        return StoragePermissionLauncher(
            request,
            createLauncherPermissionResult(request),
            createLauncherStartActivityResult(request)
        )
    }

    fun ComponentActivity.requestWriteStoragePermissions(): StoragePermissionLauncher {
        val request = RuntimePermission(
            activity = this,
            permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        return StoragePermissionLauncher(
            request,
            createLauncherPermissionResult(request),
            createLauncherStartActivityResult(request)
        )
    }

    private fun Fragment.createLauncherPermissionResult(request: RuntimePermission) =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            when {
                results.all { it.value } -> {
                    request.permissionCallbacks?.onAllow?.invoke(request.requestCode)
                }
                shouldShowRequestPermissionRationale(requireActivity(), *request.permissions) -> {
                    request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
                else -> {
                    request.permissionCallbacks?.onNeverAsk?.invoke(request.requestCode)
                        ?: request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
            }
        }

    private fun FragmentActivity.createLauncherPermissionResult(request: RuntimePermission) =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            when {
                results.all { it.value } -> {
                    request.permissionCallbacks?.onAllow?.invoke(request.requestCode)
                }
                shouldShowRequestPermissionRationale(this, *request.permissions) -> {
                    request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
                else -> {
                    request.permissionCallbacks?.onNeverAsk?.invoke(request.requestCode)
                        ?: request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
            }
        }

    private fun AppCompatActivity.createLauncherPermissionResult(request: RuntimePermission) =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            when {
                results.all { it.value } -> {
                    request.permissionCallbacks?.onAllow?.invoke(request.requestCode)
                }
                shouldShowRequestPermissionRationale(this, *request.permissions) -> {
                    request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
                else -> {
                    request.permissionCallbacks?.onNeverAsk?.invoke(request.requestCode)
                        ?: request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
            }
        }

    private fun ComponentActivity.createLauncherPermissionResult(request: RuntimePermission) =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            when {
                results.all { it.value } -> {
                    request.permissionCallbacks?.onAllow?.invoke(request.requestCode)
                }
                shouldShowRequestPermissionRationale(this, *request.permissions) -> {
                    request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
                else -> {
                    request.permissionCallbacks?.onNeverAsk?.invoke(request.requestCode)
                        ?: request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
            }
        }

    private fun Fragment.createLauncherStartActivityResult(request: RuntimePermission) =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { results ->
            when {
                hasWriteExternalStoragePermission(request.activity) -> {
                    request.permissionCallbacks?.onAllow?.invoke(request.requestCode)
                }
                else -> {
                    request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
            }
        }

    private fun FragmentActivity.createLauncherStartActivityResult(request: RuntimePermission) =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { results ->
            when {
                hasWriteExternalStoragePermission(request.activity) -> {
                    request.permissionCallbacks?.onAllow?.invoke(request.requestCode)
                }
                else -> {
                    request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
            }
        }

    private fun AppCompatActivity.createLauncherStartActivityResult(request: RuntimePermission) =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { results ->
            when {
                hasWriteExternalStoragePermission(request.activity) -> {
                    request.permissionCallbacks?.onAllow?.invoke(request.requestCode)
                }
                else -> {
                    request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
            }
        }

    private fun ComponentActivity.createLauncherStartActivityResult(request: RuntimePermission) =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { results ->
            when {
                hasWriteExternalStoragePermission(request.activity) -> {
                    request.permissionCallbacks?.onAllow?.invoke(request.requestCode)
                }
                else -> {
                    request.permissionCallbacks?.onDeny?.invoke(request.requestCode)
                }
            }
        }

    class RuntimePermission(
        val activity: Activity,
        val permissions: Array<String>
    ) {

        var permissionCallbacks: PermissionCallbacks? = null
        var isFirstPermissionRequest = true
        var requestCode: Int? = null
    }

    class PermissionCallbacks {

        var onAllow: OnAllow = null
        var onDeny: OnDeny = null
        var onNeverAsk: OnNeverAsk = null
        var onShowRationale: OnShowRationale = null

        fun onAllow(onAllow: OnAllow) {
            this.onAllow = onAllow
        }

        fun onDeny(onDeny: OnDeny) {
            this.onDeny = onDeny
        }

        fun onNeverAsk(onNeverAsk: OnNeverAsk) {
            this.onNeverAsk = onNeverAsk
        }

        fun onShowRationale(onShowRationale: OnShowRationale) {
            this.onShowRationale = onShowRationale
        }
    }

    class RuntimePermissionRequest(
        private val proceed: () -> Unit,
        private val cancel: () -> Unit
    ) {

        fun proceed() {
            proceed.invoke()
        }

        fun cancel() {
            cancel.invoke()
        }
    }
}
