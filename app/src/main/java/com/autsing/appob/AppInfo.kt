package com.autsing.appob

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Int,
    val icon: Drawable,
) {
    companion object {
        fun fromPackageInfo(context: Context, packageInfo: PackageInfo): AppInfo {
            return AppInfo(
                packageName = packageInfo.packageName,
                label = packageInfo.applicationInfo.loadLabel(context.packageManager).toString(),
                versionName = packageInfo.versionName,
                versionCode = packageInfo.versionCode,
                icon = context.packageManager.getApplicationIcon(packageInfo.applicationInfo),
            )
        }
    }
}
