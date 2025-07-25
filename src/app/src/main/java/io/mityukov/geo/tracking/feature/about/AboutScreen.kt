package io.mityukov.geo.tracking.feature.about

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.mityukov.geo.tracking.BuildConfig
import io.mityukov.geo.tracking.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeContent
    ) { paddingValues ->
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_round),
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.app_name))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:".toUri()
                    putExtra(
                        Intent.EXTRA_EMAIL,
                        arrayOf(context.resources.getString(R.string.about_developer_email))
                    )
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        context.resources.getString(R.string.about_email_subject)
                    )
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            context.resources.getString(R.string.about_email_app_chooser)
                        )
                    )
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.resources.getString(R.string.about_email_no_apps_message)
                        )
                    }
                }
            }) {
                Text(text = stringResource(R.string.about_button_label_email))
            }
        }
    }
}
