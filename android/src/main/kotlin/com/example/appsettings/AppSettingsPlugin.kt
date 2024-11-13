package com.example.appsettings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class AppSettingsPlugin : MethodCallHandler, FlutterPlugin, ActivityAware {
    /// Private variable to hold instance of Registrar for creating Intents.
    private lateinit var activity: Activity

    /// Private method to open device settings window
    private fun openSettings(url: String, asAnotherTask: Boolean = false) {
        try {
            val intent = Intent(url)
            if (asAnotherTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.activity.startActivity(intent)
        } catch (e: Exception) {
            // Default to APP Settings if setting activity fails to load/be available on device
            openAppSettings(asAnotherTask)
        }
    }

    private fun openAppSettings(asAnotherTask: Boolean = false) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        if (asAnotherTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromParts("package", this.activity.packageName, null)
        intent.data = uri
        this.activity.startActivity(intent)
    }

    private fun openNotificationSettings(asAnotherTask: Boolean = false) {
        when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.O -> {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, this@AppSettingsPlugin.activity.packageName)
                    if (asAnotherTask) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                this.activity.startActivity(intent)
            }

            Build.VERSION_CODES.M ->
                openSettings(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, asAnotherTask)
        }
    }

    private fun openVpnSettings(asAnotherTask: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            openSettings(Settings.ACTION_VPN_SETTINGS, asAnotherTask)
        } else {
            openSettings("android.net.vpn.SETTINGS", asAnotherTask)
        }
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        val channel = MethodChannel(binding.binaryMessenger, "app_settings")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {

    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.activity = binding.activity
    }

    override fun onDetachedFromActivity() {
    }

    /// Handler method to manage method channel calls.
    override fun onMethodCall(call: MethodCall, result: Result) {

        val asAnotherTask = call.argument("asAnotherTask") ?: false

        when (call.method) {
            "wifi" -> openSettings(Settings.ACTION_WIFI_SETTINGS, asAnotherTask)
            "location" -> openSettings(Settings.ACTION_LOCATION_SOURCE_SETTINGS, asAnotherTask)
            "security" -> openSettings(Settings.ACTION_SECURITY_SETTINGS, asAnotherTask)
            "bluetooth" -> openSettings(Settings.ACTION_BLUETOOTH_SETTINGS, asAnotherTask)
            "data_roaming" -> openSettings(Settings.ACTION_DATA_ROAMING_SETTINGS, asAnotherTask)
            "date" -> openSettings(Settings.ACTION_DATE_SETTINGS, asAnotherTask)
            "display" -> openSettings(Settings.ACTION_DISPLAY_SETTINGS, asAnotherTask)
            "notification" -> openNotificationSettings(asAnotherTask)
            "nfc" -> openSettings(Settings.ACTION_NFC_SETTINGS, asAnotherTask)
            "sound" -> openSettings(Settings.ACTION_SOUND_SETTINGS, asAnotherTask)
            "internal_storage" -> openSettings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS, asAnotherTask)
            "battery_optimization" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                openSettings(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, asAnotherTask)
            }

            "vpn" -> openVpnSettings(asAnotherTask)
            "app_settings" -> openAppSettings(asAnotherTask)
            "device_settings" -> openSettings(Settings.ACTION_SETTINGS, asAnotherTask)
        }
    }
}
