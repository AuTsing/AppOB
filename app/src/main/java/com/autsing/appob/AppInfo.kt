package com.autsing.appob

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import java.io.File
import java.text.StringCharacterIterator

data class AppInfo(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Int,
    val icon: Drawable,
    val apkPath: String,
    val dataPath: String,
    val size: Long,
    val sizeDisplay: String,
    val uid: Int,
) {
    companion object {
        fun fromPackageInfo(context: Context, packageInfo: PackageInfo): AppInfo {
            val apkPath = packageInfo.applicationInfo.sourceDir
            val apkFile = File(apkPath)
            val size = apkFile.length()
            val sizeDisplay = toHumanReadableByte(size)
            return AppInfo(
                packageName = packageInfo.packageName,
                label = packageInfo.applicationInfo.loadLabel(context.packageManager).toString(),
                versionName = packageInfo.versionName,
                versionCode = packageInfo.versionCode,
                icon = context.packageManager.getApplicationIcon(packageInfo.applicationInfo),
                apkPath = apkPath,
                dataPath = packageInfo.applicationInfo.dataDir,
                size = size,
                sizeDisplay = sizeDisplay,
                uid = packageInfo.applicationInfo.uid,
            )
        }

        private fun toHumanReadableByte(byte: Long): String {
            val unitIter = StringCharacterIterator("BKMGTPE")
            var readable = byte.toFloat()
            while (readable >= 1024) {
                readable /= 1024
                unitIter.next()
            }
            return "${"%.2f".format(readable)}${unitIter.current()}"
        }
    }

    fun getKvs(context: Context): Map<String, String> {
        return mapOf(
            context.getString(R.string.label_label) to label,
            context.getString(R.string.label_version_name) to versionName,
            context.getString(R.string.label_version_code) to versionCode.toString(),
            context.getString(R.string.label_size) to sizeDisplay,
            context.getString(R.string.label_data_path) to dataPath,
            context.getString(R.string.label_apk_path) to apkPath,
            context.getString(R.string.label_uid) to uid.toString(),
        )
    }
}
