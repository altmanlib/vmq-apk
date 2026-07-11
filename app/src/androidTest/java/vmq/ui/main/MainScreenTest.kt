package vmq.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.vone.qrcode.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vmq.data.AppConfig
import vmq.ui.theme.VmqTheme

class MainScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mainScreen_showsCurrentConfigurationAndInvokesActions() {
        var scanClicks = 0
        var manualClicks = 0
        var heartbeatClicks = 0
        var pushClicks = 0

        composeTestRule.setContent {
            VmqTheme {
                MainScreen(
                    uiState = MainUiState(
                        config = AppConfig(host = "https://vmq.example.com", key = "secret"),
                        isConfigured = true,
                    ),
                    appVersion = "2.2.0",
                    onScanQrCode = { scanClicks++ },
                    onManualInput = { manualClicks++ },
                    onCheckHeartbeat = { heartbeatClicks++ },
                    onCheckPush = { pushClicks++ },
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.main_config_title),
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("https://vmq.example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("secret").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.main_footer_version, "2.2.0"),
        ).assertIsDisplayed()

        composeTestRule.onNodeWithTag("scan_config_action").performClick()
        composeTestRule.onNodeWithTag("manual_config_action").performClick()
        composeTestRule.onNodeWithTag("heartbeat_action").performClick()
        composeTestRule.onNodeWithTag("push_test_action").performClick()

        assertEquals(1, scanClicks)
        assertEquals(1, manualClicks)
        assertEquals(1, heartbeatClicks)
        assertEquals(1, pushClicks)
    }
}
