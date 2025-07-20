package io.mityukov.geo.tracking.feature.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.mityukov.geo.tracking.R

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(onSettingsSelected: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.profile_title)) },
                actions = {
                    IconButton(onClick = onSettingsSelected) {
                        Icon(painterResource(R.drawable.icon_settings), null)
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeContent,
    ) { paddingValues ->

    }
}
