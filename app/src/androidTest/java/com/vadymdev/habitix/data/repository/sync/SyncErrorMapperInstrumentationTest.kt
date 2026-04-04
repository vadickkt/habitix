package com.vadymdev.habitix.data.repository.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestoreException
import com.vadymdev.habitix.domain.model.SyncFailureKind
import com.vadymdev.habitix.domain.model.SyncTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncErrorMapperInstrumentationTest {

    @Test
    fun permissionDenied_mapsToPermanent() {
        val error = FirebaseFirestoreException("denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)

        val result = mapSyncThrowable(SyncTarget.SETTINGS, error)

        assertEquals(SyncFailureKind.PERMANENT, result.kind)
        assertEquals(SyncTarget.SETTINGS, result.target)
        assertTrue(result.message?.contains("PERMISSION_DENIED") == true)
    }

    @Test
    fun unauthenticated_mapsToPermanent() {
        val error = FirebaseFirestoreException("auth", FirebaseFirestoreException.Code.UNAUTHENTICATED)

        val result = mapSyncThrowable(SyncTarget.PROFILE, error)

        assertEquals(SyncFailureKind.PERMANENT, result.kind)
        assertEquals(SyncTarget.PROFILE, result.target)
    }

    @Test
    fun unavailable_mapsToTransient() {
        val error = FirebaseFirestoreException("unavailable", FirebaseFirestoreException.Code.UNAVAILABLE)

        val result = mapSyncThrowable(SyncTarget.HABITS, error)

        assertEquals(SyncFailureKind.TRANSIENT, result.kind)
        assertEquals(SyncTarget.HABITS, result.target)
    }

    @Test
    fun resourceExhausted_mapsToTransient() {
        val error = FirebaseFirestoreException("quota", FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED)

        val result = mapSyncThrowable(SyncTarget.ACHIEVEMENTS, error)

        assertEquals(SyncFailureKind.TRANSIENT, result.kind)
        assertEquals(SyncTarget.ACHIEVEMENTS, result.target)
    }

    @Test
    fun wrappedFirestoreError_isDiscoveredInCauseChain() {
        val firestore = FirebaseFirestoreException("wrapped", FirebaseFirestoreException.Code.FAILED_PRECONDITION)
        val wrapped = IllegalStateException("outer", RuntimeException("mid", firestore))

        val result = mapSyncThrowable(SyncTarget.ORCHESTRATOR, wrapped)

        assertEquals(SyncFailureKind.PERMANENT, result.kind)
        assertEquals(SyncTarget.ORCHESTRATOR, result.target)
        assertEquals(wrapped, result.cause)
    }
}
