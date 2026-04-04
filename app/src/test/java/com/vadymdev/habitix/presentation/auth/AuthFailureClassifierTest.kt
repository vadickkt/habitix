package com.vadymdev.habitix.presentation.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFailureClassifierTest {

    @Test
    fun classifyAuthFailure_canceled_mapsToNoAccountKind() {
        val kind = classifyAuthFailure("GetGoogleIdOperation failed with status CANCELED")

        assertEquals(AuthFailureKind.CANCELED_OR_NO_ACCOUNT, kind)
        assertTrue(shouldPromoteGuest(kind))
    }

    @Test
    fun classifyAuthFailure_providerUnavailable_mapsCorrectly() {
        val kind = classifyAuthFailure("No credential provider found, Play Services not available")

        assertEquals(AuthFailureKind.PROVIDER_UNAVAILABLE, kind)
        assertTrue(shouldPromoteGuest(kind))
    }

    @Test
    fun classifyAuthFailure_network_mapsTransient() {
        val kind = classifyAuthFailure("Network timeout during sign-in")

        assertEquals(AuthFailureKind.TRANSIENT, kind)
        assertFalse(shouldPromoteGuest(kind))
    }
}
