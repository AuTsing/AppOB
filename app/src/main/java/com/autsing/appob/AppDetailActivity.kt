package com.autsing.appob

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.autsing.appob.ui.theme.AppOBTheme
import kotlinx.coroutines.flow.MutableStateFlow

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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadAppInfo()

        setContent {
            val appInfo by appInfoStateFlow.collectAsState()
            val exception by exceptionStateFlow.collectAsState()
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

            AppOBTheme {
                Scaffold(
                    topBar = {
                        AppDetailTopBar(
                            appInfo = appInfo,
                            scrollBehavior = scrollBehavior,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailTopBar(
    appInfo: AppInfo?,
    scrollBehavior: TopAppBarScrollBehavior,
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
            IconButton(
                onClick = {
                    val intent = Intent()
                    intent.setAction(Intent.ACTION_SEND)
                    intent.
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_ios_share_24),
                    contentDescription = null,
                )
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
