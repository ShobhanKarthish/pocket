package com.shobhankarthish.pocket.shelf

import android.content.Intent
import com.shobhankarthish.pocket.ShareIntake
import com.shobhankarthish.pocket.classifyShare
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareIntakeTest {
    @Test
    fun singleSendKeepsExistingPaths() {
        assertEquals(ShareIntake.SingleStream, classifyShare(Intent.ACTION_SEND, 1, false))
        assertEquals(ShareIntake.PlainText, classifyShare(Intent.ACTION_SEND, 0, true))
        assertNull(classifyShare(Intent.ACTION_SEND, 0, false))
    }

    @Test
    fun sendMultipleUsesStreamsWhenPresent() {
        assertEquals(ShareIntake.MultipleStreams, classifyShare(Intent.ACTION_SEND_MULTIPLE, 3, false))
        assertEquals(ShareIntake.SingleStream, classifyShare(Intent.ACTION_SEND_MULTIPLE, 1, false))
        assertEquals(ShareIntake.PlainText, classifyShare(Intent.ACTION_SEND_MULTIPLE, 0, true))
        assertNull(classifyShare(Intent.ACTION_SEND_MULTIPLE, 0, false))
    }

    @Test
    fun otherActionsAreIgnored() {
        assertNull(classifyShare(Intent.ACTION_VIEW, 2, true))
        assertNull(classifyShare(Intent.ACTION_MAIN, 0, false))
    }
}
