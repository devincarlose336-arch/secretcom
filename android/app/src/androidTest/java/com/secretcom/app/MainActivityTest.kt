package com.secretcom.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.secretcom.app.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginScreenIsDisplayed() {
        composeTestRule.onNodeWithText("SECRETCOM").assertIsDisplayed()
    }

    @Test
    fun loginButtonIsDisplayed() {
        composeTestRule.onNodeWithText("LOGIN").assertIsDisplayed()
    }

    @Test
    fun registerLinkIsDisplayed() {
        composeTestRule.onNodeWithText("New user? Register with Meeting ID").assertIsDisplayed()
    }
}
