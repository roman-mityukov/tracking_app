package io.mityukov.geo.tracking.feature.map

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.data.repository.geo.GeolocationUpdateException

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapPermissions(
    viewModelState: MapState,
    onLocationDisabled: (String, String) -> Unit,
) {
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = buildList {
            add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    )
    val showLocationRationale = remember { mutableStateOf(false) }

    if (showLocationRationale.value) {
        LocationRationaleDialog(
            onNegative = {
                showLocationRationale.value = false
            },
            onPositive = {
                showLocationRationale.value = false
                multiplePermissionsState.launchMultiplePermissionRequest()
            },
        )
    }

    if (viewModelState is MapState.NoLocation) {
        NoLocation(
            state = viewModelState,
            onLocationDisabled = onLocationDisabled,
            onPermissionsNotGranted = {
                if (multiplePermissionsState.shouldShowRationale) {
                    showLocationRationale.value = true
                } else if (multiplePermissionsState.allPermissionsGranted.not()) {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationRationaleDialog(
    modifier: Modifier = Modifier,
    onNegative: () -> Unit,
    onPositive: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onNegative
    ) {
        Surface(
            modifier = modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.map_location_permission_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onNegative) {
                        Text(text = stringResource(R.string.dialog_no))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onPositive) {
                        Text(text = stringResource(R.string.map_location_permission_consent))
                    }
                }
            }
        }
    }
}

@Composable
private fun NoLocation(
    state: MapState.NoLocation,
    onLocationDisabled: (String, String) -> Unit,
    onPermissionsNotGranted: () -> Unit,
) {
    val message = stringResource(R.string.map_disabled_location_permission_description)
    val actionLabel = stringResource(R.string.map_disabled_location_permission_consent)
    LaunchedEffect(state) {
        when (state.cause) {
            GeolocationUpdateException.LocationDisabled -> {
                onLocationDisabled(message, actionLabel)
            }

            GeolocationUpdateException.PermissionsNotGranted -> {
                onPermissionsNotGranted()
            }

            GeolocationUpdateException.LocationIsNull, GeolocationUpdateException.Initialization -> {
                // Валидное состояние - ничего не делаем
            }

            null -> {
                // Валидное состояние если локация не пришла без ошибки
            }
        }
    }
}
