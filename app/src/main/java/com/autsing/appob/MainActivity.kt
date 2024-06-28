package com.autsing.appob

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.autsing.appob.ui.theme.AppOBTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val appsStateFlow: MutableStateFlow<List<AppInfo>> = MutableStateFlow(emptyList())
    private val loadingStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadApps()

        setContent {
            val apps by appsStateFlow.collectAsState()
            val loading by loadingStateFlow.collectAsState()
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

            AppOBTheme {
                Scaffold(
                    topBar = {
                        AppListTopBar(
                            loading = loading,
                            scrollBehavior = scrollBehavior,
                            onClickRefresh = this::loadApps,
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
                        AppListScreen(
                            loading = loading,
                            apps = apps,
                        )
                    }
                }
            }
        }
    }

    private fun loadApps() = lifecycleScope.launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            loadingStateFlow.value = true
        }

        val minDelay = launch(Dispatchers.IO) { delay(500) }

        val apps = packageManager.getInstalledPackages(PackageManager.MATCH_ALL)
            .filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map { AppInfo.fromPackageInfo(this@MainActivity, it) }

        minDelay.join()

        withContext(Dispatchers.Main) {
            appsStateFlow.value = apps
            loadingStateFlow.value = false
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppListTopBar(
    loading: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onClickRefresh: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.label_app_list)) },
        actions = {
            if (!loading) {
                IconButton(onClick = onClickRefresh) {
                    Icon(
                        painter = painterResource(R.drawable.round_refresh_24),
                        contentDescription = "Refresh button",
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun AppListScreen(
    loading: Boolean,
    apps: List<AppInfo>,
) {
    val context = LocalContext.current

    if (loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn {
            items(apps) {
                ListItem(
                    headlineContent = {
                        Text(it.label)
                    },
                    overlineContent = {
                        Text(it.packageName)
                    },
                    supportingContent = {
                        Text("${it.versionName} (${it.versionCode})")
                    },
                    leadingContent = {
                        Image(
                            bitmap = it.icon.toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                        )
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.round_keyboard_arrow_right_24),
                            contentDescription = null,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    },
                    modifier = Modifier.clickable {
                        AppDetailActivity.startActivity(context, it)
                    },
                )
            }
        }
    }
}
