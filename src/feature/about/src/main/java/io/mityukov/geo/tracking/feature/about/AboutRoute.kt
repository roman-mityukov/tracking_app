@file:Suppress("TooManyFunctions")

package io.mityukov.geo.tracking.feature.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.MailTo
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.core.model.AppInfo
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.core.designsystem.component.ButtonBack
import io.mityukov.geo.tracking.core.ui.FontScalePreviews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutRoute(
    viewModel: AboutViewModel = hiltViewModel(),
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val uriStringState = viewModel.logsStateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(uriStringState.value) {
        val uriString = uriStringState.value
        if (uriString != null) {
            shareLogs(uriString, context, coroutineScope, snackbarHostState)
            viewModel.add(AboutEvent.ConsumeLogs)
        }
    }

    val appInfo = viewModel.appInfo

    AboutScreen(
        modifier = Modifier.testTag(AppTestTag.ABOUT_SCREEN),
        appInfo = appInfo,
        onBack = onBack,
        onShareLogs = {
            viewModel.add(AboutEvent.ShareLogs)
        },
        onSendEmail = {
            sendEmail(context, coroutineScope, snackbarHostState)
        }
    )
}

@Composable
internal fun AboutScreen(
    modifier: Modifier = Modifier,
    appInfo: AppInfo,
    onBack: () -> Unit,
    onShareLogs: () -> Unit,
    onSendEmail: () -> Unit,
) {
    Scaffold(
        topBar = {
            AboutTopBar(onBack = onBack)
        },
        contentWindowInsets = WindowInsets.safeContent
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            AboutContent(
                appInfo = appInfo,
                onShareLogs = onShareLogs,
                onSendEmail = onSendEmail,
            )
        }
    }
}

@Composable
private fun AboutContent(
    modifier: Modifier = Modifier,
    appInfo: AppInfo,
    onShareLogs: () -> Unit,
    onSendEmail: () -> Unit,
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppIcon()
        Spacer(modifier = Modifier.height(16.dp))
        AppInfo(appInfo = appInfo)
        Spacer(modifier = Modifier.height(16.dp))
        ContactButton(onClick = onSendEmail)
        Spacer(modifier = Modifier.height(8.dp))
        ShareLogsButton(onShareLogs = onShareLogs)
    }
}

@Composable
private fun AppInfo(modifier: Modifier = Modifier, appInfo: AppInfo) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.feature_about_app_name))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${appInfo.versionName}(${appInfo.versionCode})")
    }
}

@Composable
private fun AppIcon(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.feature_about_ic_launcher_round),
        contentDescription = stringResource(R.string.feature_about_content_description_app_icon),
    )
}

@Composable
private fun ContactButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Text(text = stringResource(R.string.feature_about_button_label_email), maxLines = 1)
    }
}

@Composable
private fun ShareLogsButton(modifier: Modifier = Modifier, onShareLogs: () -> Unit) {
    Button(modifier = modifier.fillMaxWidth(), onClick = onShareLogs) {
        Text(text = stringResource(R.string.feature_about_button_send_logs), maxLines = 1)
    }
}

@Preview
@FontScalePreviews
@Composable
private fun AboutScreenPreview() {
    AboutScreen(
        appInfo = AppInfo(versionName = "0.40.1", versionCode = 50),
        onBack = {},
        onSendEmail = {},
        onShareLogs = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutTopBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.feature_about_title)) },
        navigationIcon = {
            ButtonBack(onBack = onBack)
        }
    )
}

private fun sendEmail(
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
) {
    val intent = createEmailIntent(context)
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(
            Intent.createChooser(
                intent,
                context.resources.getString(R.string.feature_about_email_app_chooser)
            )
        )
    } else {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = context.resources.getString(R.string.feature_about_email_no_apps_message)
            )
        }
    }
}

private fun shareLogs(
    uriString: String,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
) {
    val sharingErrorMessage = context.resources.getString(R.string.feature_about_error_sharing)
    try {
        val uri = uriString.toUri()
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = "application/zip"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(R.string.feature_about_intent_logs_title)
            )
        )
    } catch (_: ActivityNotFoundException) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(sharingErrorMessage)
        }
    }
}

private fun createEmailIntent(context: Context): Intent {
    return Intent(Intent.ACTION_SENDTO).apply {
        data = MailTo.MAILTO_SCHEME.toUri()
        putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf(context.resources.getString(R.string.feature_about_developer_email))
        )
        putExtra(
            Intent.EXTRA_SUBJECT,
            context.resources.getString(R.string.feature_about_email_subject)
        )
    }
}
