package com.autsing.appob

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.autsing.appob.ui.theme.AppOBTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    private val appsStateFlow: MutableStateFlow<List<AppInfo>> = MutableStateFlow(emptyList())

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val apps by appsStateFlow.collectAsState()
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

            AppOBTheme {
                Scaffold(
                    topBar = {
                        AppListTopBar(
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
                        AppListScreen(apps = apps)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.IO) {
            appsStateFlow.value = loadApps(this@MainActivity)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "Loaded ${appsStateFlow.value.size} apps",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }
}

private fun loadApps(context: Context): List<AppInfo> {
    return context.packageManager.getInstalledPackages(PackageManager.MATCH_ALL)
        .filter { it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
        .map {
            AppInfo(
                packageName = it.packageName,
                label = it.applicationInfo.loadLabel(context.packageManager).toString(),
                versionName = it.versionName,
                versionCode = it.versionCode,
                icon = context.packageManager.getApplicationIcon(it.applicationInfo),
            )
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppListTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        title = { Text("App List") },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun AppListScreen(apps: List<AppInfo>) {
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
                modifier = Modifier.clickable { },
            )
        }
    }
}
