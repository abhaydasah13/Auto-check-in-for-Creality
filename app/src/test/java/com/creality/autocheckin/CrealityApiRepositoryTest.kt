package com.creality.autocheckin

import com.creality.autocheckin.data.CrealityApiRepository
import com.creality.autocheckin.worker.CheckInScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrealityApiRepositoryTest {

    private val repository = CrealityApiRepository()

    @Test
    fun testParseCookies() {
        val cookieStr = "_ga=GA1.1.123; model_token=abc123token; model_user_id=6223758544; model_device_id=dev-uuid-456;"
        val map = repository.parseCookies(cookieStr)

        assertEquals("abc123token", map["model_token"])
        assertEquals("6223758544", map["model_user_id"])
        assertEquals("dev-uuid-456", map["model_device_id"])
        assertEquals("GA1.1.123", map["_ga"])
    }

    @Test
    fun testInspectCookieValid() {
        val cookieStr = "model_token=tok123; model_user_id=usr456; model_device_id=did789"
        val details = repository.inspectCookie(cookieStr, fallbackDeviceId = "fallback-id")

        assertTrue(details.isValid)
        assertEquals("tok123", details.token)
        assertEquals("usr456", details.userId)
        assertEquals("did789", details.deviceId)
    }

    @Test
    fun testInspectCookieMissingToken() {
        val cookieStr = "model_user_id=usr456; model_device_id=did789"
        val details = repository.inspectCookie(cookieStr, fallbackDeviceId = "fallback-id")

        assertFalse(details.isValid)
        assertEquals("", details.token)
        assertEquals("did789", details.deviceId)
    }

    @Test
    fun testInspectCookieFallbackDeviceId() {
        val cookieStr = "model_user_id=usr456"
        val details = repository.inspectCookie(cookieStr, fallbackDeviceId = "fallback-id")

        assertFalse(details.isValid)
        assertEquals("fallback-id", details.deviceId)
    }

    @Test
    fun testCalculateInitialDelayMillis() {
        val delay = CheckInScheduler.calculateInitialDelayMillis(6, 0)
        assertTrue(delay > 0)
        assertTrue(delay <= 24 * 60 * 60 * 1000L)
    }
}
