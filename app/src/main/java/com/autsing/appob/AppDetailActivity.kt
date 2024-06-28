package com.autsing.appob

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.autsing.appob.ui.theme.AppOBTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AppDetailActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_KEY_PACKAGE_NAME = "extra_key_package_name"

        fun startActivity(context: Context, appInfo: AppInfo) {
            val intent = Intent(context, AppDetailActivity::class.java)
            intent.putExtra(EXTRA_KEY_PACKAGE_NAME, appInfo.packageName)
            context.startActivity(intent)
        }
    }

    private val appInfoStateFlow: MutableStateFlow<AppInfo?> = MutableStateFlow(null)
    private val exceptionStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val sharingStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadAppInfo()

        setContent {
            val appInfo by appInfoStateFlow.collectAsState()
            val exception by exceptionStateFlow.collectAsState()
            val sharing by sharingStateFlow.collectAsState()
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

            AppOBTheme {
                Scaffold(
                    topBar = {
                        AppDetailTopBar(
                            appInfo = appInfo,
                            scrollBehavior = scrollBehavior,
                            sharing = sharing,
                            onClickShare = this::shareApp,
                        )
                    },
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        if (exception != "") {
                            ExceptionScreen(exception = exception)
                        } else {
                            appInfo?.let { AppDetailScreen(appInfo = it) }
                        }
                    }
                }
            }
        }
    }

    private fun loadAppInfo() {
        val packageName = intent.getStringExtra(EXTRA_KEY_PACKAGE_NAME)
        if (packageName == null) {
            exceptionStateFlow.value = "Package not found."
            return
        }

        val packageInfo = runCatching {
            packageManager.getPackageInfo(packageName, PackageManager.MATCH_ALL)
        }.getOrNull()
        if (packageInfo == null) {
            exceptionStateFlow.value = "Package $packageName not found."
            return
        }

        appInfoStateFlow.value = AppInfo.fromPackageInfo(this, packageInfo)
    }

    private fun shareApp() = lifecycleScope.launch(Dispatchers.IO) {
        runCatching {
            withContext(Dispatchers.Main) {
                sharingStateFlow.value = true
            }

            val appInfo = appInfoStateFlow.value ?: throw Exception("Package not found.")
            val apkPathFile = File(appInfo.apkPath)
            val tmpDir = cacheDir.resolve("share")
            if (tmpDir.exists()) {
                tmpDir.deleteRecursively()
                tmpDir.mkdir()
            }
            val filename = "${appInfo.label}_${appInfo.packageName}_${appInfo.versionName}.apk"
            val cacheApkPath = tmpDir.resolve(filename)
            apkPathFile.copyTo(cacheApkPath, true)

            val uri = FileProvider.getUriForFile(
                this@AppDetailActivity,
                "$packageName.fileprovider",
                cacheApkPath,
            )

            ShareCompat.IntentBuilder(this@AppDetailActivity)
                .setType("application/vnd.android.package-archive")
                .addStream(uri)
                .setChooserTitle("Share")
                .startChooser()
        }.onFailure {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AppDetailActivity, "${it.message}", Toast.LENGTH_LONG).show()
            }
        }.also {
            withContext(Dispatchers.Main) {
                sharingStateFlow.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailTopBar(
    appInfo: AppInfo?,
    scrollBehavior: TopAppBarScrollBehavior,
    sharing: Boolean,
    onClickShare: () -> Unit = {},
) {
    val context = LocalContext.current

    TopAppBar(
        title = { Text(appInfo?.label ?: "App Detail") },
        navigationIcon = {
            IconButton(onClick = { (context as AppDetailActivity).finish() }) {
                Icon(
                    painter = painterResource(R.drawable.round_keyboard_arrow_left_24),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }
        },
        actions = {
            if (sharing) {
                CircularProgressIndicator()
            } else {
                IconButton(onClick = onClickShare) {
                    Icon(
                        painter = painterResource(R.drawable.round_ios_share_24),
                        contentDescription = null,
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun AppDetailScreen(
    appInfo: AppInfo,
) {
    Text(appInfo.toString())
}

@Composable
private fun ExceptionScreen(exception: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(exception)
    }
}
