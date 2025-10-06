package io.mityukov.geo.tracking.feature.track.editing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.core.data.validation.TrackProperties
import io.mityukov.geo.tracking.core.data.validation.TrackValidationResult
import io.mityukov.geo.tracking.core.model.track.Track
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.feature.track.details.R
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun TrackEditingRoute(
    viewModel: TrackEditingViewModel = hiltViewModel(),
    track: Track,
    onDismiss: () -> Unit,
) {
    val viewModelState = viewModel.stateFlow.collectAsStateWithLifecycle()

    TrackEditingSheet(
        track = track,
        viewModelState = viewModelState.value,
        onSave = { update ->
            viewModel.add(TrackEditingEvent.Save(update))
        },
        onSaveCompleted = {
            viewModel.add(TrackEditingEvent.ConsumeSaveCompleted)
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackEditingSheet(
    track: Track,
    viewModelState: TrackEditingState,
    onSave: (Track) -> Unit,
    onSaveCompleted: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (viewModelState is TrackEditingState.SaveCompleted) {
        LaunchedEffect(Unit) {
            onSaveCompleted()
        }
    } else {

        val modalBottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ModalBottomSheet(
            sheetState = modalBottomSheetState,
            onDismissRequest = onDismiss,
        ) {
            TrackEditingSheetContent(
                track = track,
                viewModelState = viewModelState,
                onSave = onSave,
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
internal fun TrackEditingSheetContent(
    track: Track,
    viewModelState: TrackEditingState,
    onSave: (Track) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        val nameState = rememberTextFieldState(initialText = track.name)
        val descriptionState = rememberTextFieldState(initialText = track.description)

        val onSaveAugmented = {
            onSave(
                track.copy(
                    name = nameState.text.toString(),
                    description = descriptionState.text.toString(),
                ),
            )
        }

        Text(
            stringResource(R.string.feature_track_details_edit_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.feature_track_details_label_name)) },
            state = nameState,
            isError = (viewModelState as? TrackEditingState.SaveFailed)?.trackValidationResult
                    == TrackValidationResult.Invalid.Name,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.feature_track_details_label_description)) },
            state = descriptionState,
            isError = (viewModelState as? TrackEditingState.SaveFailed)?.trackValidationResult
                    == TrackValidationResult.Invalid.Description,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = {
                onSaveAugmented()
            }
        )
        TrackValidationFailed(viewModelState)
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                modifier = Modifier.testTag(AppTestTag.BUTTON_CANCEL),
                onClick = onDismiss
            ) {
                Text(stringResource(io.mityukov.geo.tracking.core.ui.R.string.core_ui_cancel))
            }
            Spacer(Modifier.width(8.dp))
            Button(
                modifier = Modifier.testTag(AppTestTag.BUTTON_SAVE),
                onClick = onSaveAugmented
            ) {
                Text(stringResource(io.mityukov.geo.tracking.core.ui.R.string.core_ui_save))
            }
        }
    }
}

@Composable
internal fun TrackValidationFailed(viewModelState: TrackEditingState) {
    if (viewModelState is TrackEditingState.SaveFailed) {
        Spacer(Modifier.height(16.dp))
        val textError = when (viewModelState.trackValidationResult) {
            TrackValidationResult.Invalid.Name -> {
                stringResource(
                    R.string.feature_track_details_edit_error_name,
                    TrackProperties.MAX_LENGTH_NAME
                )
            }

            TrackValidationResult.Invalid.Description -> {
                stringResource(
                    R.string.feature_track_details_edit_error_desctiption,
                    TrackProperties.MAX_LENGTH_DESCRIPTION
                )
            }
        }
        Text(
            text = textError,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
internal fun PreviewTrackEditingSheetContent(
    @PreviewParameter(TrackEditingStateProvider::class) trackEditingState: TrackEditingState
) {
    TrackEditingSheetContent(
        track = Track(
            id = "49defd14-ae28-4705-9334-59761914de0c",
            name = "Тестовый трек 1",
            description = "Описание",
            start = 1757038748000,
            duration = 78.seconds,
            end = 1757038758000,
            distance = 1547f,
            altitudeUp = 32f,
            altitudeDown = 12f,
            sumSpeed = 256f,
            maxSpeed = 1.2f,
            minSpeed = 1.0f,
            geolocationCount = 2,
            filePath = "",
        ),
        viewModelState = trackEditingState,
        onSave = { _ -> },
        onDismiss = {},
    )
}

internal class TrackEditingStateProvider : PreviewParameterProvider<TrackEditingState> {
    override val values: Sequence<TrackEditingState> = sequenceOf(
        TrackEditingState.Initial,
        TrackEditingState.SaveFailed(TrackValidationResult.Invalid.Name),
        TrackEditingState.SaveFailed(TrackValidationResult.Invalid.Description),
    )
}
