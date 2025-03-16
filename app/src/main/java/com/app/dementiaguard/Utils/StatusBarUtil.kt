package com.app.dementiaguard.Utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController

object StatusBarUtil {
    fun setStatusBarAppearance(activity: Activity, isLightScreen: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = activity.window.insetsController
            if (controller != null) {
                if (isLightScreen) {
                    controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            } else {
                // Legacy method for API < 30
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var flags = activity.window.decorView.systemUiVisibility
                    flags = if (isLightScreen) {
                        flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    }
                    activity.window.decorView.systemUiVisibility = flags
                }
            }
        }
    }
}
