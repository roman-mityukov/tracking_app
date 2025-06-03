package io.rm.test.geo.feature.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.rm.test.geo.R

@Composable
fun ProfileScreen(onSettingsSelected: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text(stringResource(R.string.home_navigation_user_profile))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSettingsSelected) {
                Text("Настройки")
            }
        }
    }
}