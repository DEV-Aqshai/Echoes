package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EchoCall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Echoes", appName)
  }

  @Test
  fun `create echo call model`() {
    val call = EchoCall(
      id = 1,
      title = "Ask how I am doing",
      description = "Check-in from past self",
      scheduledTimestamp = 1800000000000L,
      audioFilePath = "/path/to/echo.m4a",
      audioDurationSeconds = 12
    )
    assertEquals("Ask how I am doing", call.title)
    assertEquals(EchoCall.REPEAT_ONCE, call.repeatType)
    assertNotNull(call.audioFilePath)
  }
}
