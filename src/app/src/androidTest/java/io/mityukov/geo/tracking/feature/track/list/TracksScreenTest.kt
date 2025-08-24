package io.mityukov.geo.tracking.feature.track.list

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.app.ui.theme.GeoAppTheme
import io.mityukov.geo.tracking.core.database.AppDatabase
import io.mityukov.geo.tracking.core.database.dao.TrackDao
import io.mityukov.geo.tracking.core.database.model.TrackEntity
import io.mityukov.geo.tracking.hilt.HiltTestActivity
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.InputStream
import javax.inject.Inject

@HiltAndroidTest
class TracksScreenTest {
    val hiltRule = HiltAndroidRule(this)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()
    val context: Context = ApplicationProvider.getApplicationContext()

    @get:Rule
    val rule: RuleChain = RuleChain.outerRule(hiltRule).around(composeRule)

    @Inject
    lateinit var trackDao: TrackDao

    @Inject
    lateinit var db: AppDatabase

    lateinit var mockOnNavigateToTrack: (String) -> Unit
    lateinit var mockOnNavigateToTracksEditing: (String) -> Unit

    @Before
    fun setUp() {
        hiltRule.inject()

        db.clearAllTables()

        mockOnNavigateToTrack = mock()
        mockOnNavigateToTracksEditing = mock()

        composeRule.setContent {
            GeoAppTheme {
                TracksScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToTrack = mockOnNavigateToTrack,
                    onNavigateToTracksEditing = mockOnNavigateToTracksEditing
                )
            }
        }
    }

    @Test
    fun emptyList_showEmptyListMessage() {
        composeRule
            .onNodeWithText(context.resources.getString(R.string.tracks_title))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.resources.getString(R.string.tracks_empty_list_message))
            .assertIsDisplayed()
    }

    @Test
    fun oneTrackInDb_showListWithOneTrackItem() {
        insertTrackToDb()
        composeRule.waitUntil(100) { true }
        composeRule
            .onNodeWithText(context.resources.getString(R.string.tracks_title))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.resources.getString(R.string.tracks_empty_list_message))
            .assertIsNotDisplayed()
        composeRule.onNodeWithTag("TracksLazyColumn").onChildren().assertCountEquals(1)
    }

    @Test
    fun performClick_onNavigateToTrackCalled() {
        insertTrackToDb()
        composeRule.waitUntil(100) { true }
        composeRule.onNodeWithTag("TrackItem").performClick()
        verify(mockOnNavigateToTrack).invoke("trackId")
    }

    @Test
    fun performLongClick_onNavigateToTrackEditingCalled() {
        insertTrackToDb()
        composeRule.waitUntil(100) { true }
        composeRule.onNodeWithTag("TrackItem").performTouchInput {
            longClick(durationMillis = 500)
        }
        verify(mockOnNavigateToTracksEditing).invoke("trackId")
    }

    private fun insertTrackToDb() {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.track)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val json = Json {
            ignoreUnknownKeys = true
        }
        val entity = json.decodeFromString<TrackEntity>(jsonString)
        trackDao.insertTrack(entity)
    }
}
