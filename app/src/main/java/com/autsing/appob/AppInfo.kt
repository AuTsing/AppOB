package com.autsing.appob

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Int,
    val icon: Drawable,
)
