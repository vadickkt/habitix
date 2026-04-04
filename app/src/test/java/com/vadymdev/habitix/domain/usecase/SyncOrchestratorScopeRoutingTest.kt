package com.vadymdev.habitix.domain.usecase

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncOrchestratorScopeRoutingTest {

    @Test
    fun settingsOnly_runsOnlySettingsSync() = runBlocking {
        val recorder = CallRecorder()
        val orchestrator = buildSyncOrchestrator(recorder = recorder)

        val result = orchestrator("uid-1", SyncScope.SETTINGS_ONLY)

        assertTrue(result.isSuccess)
        assertEquals(listOf("settings"), recorder.calls)
    }

    @Test
    fun habitsAndAchievements_runsExpectedOrder() = runBlocking {
        val recorder = CallRecorder()
        val orchestrator = buildSyncOrchestrator(recorder = recorder)

        val result = orchestrator("uid-1", SyncScope.HABITS_AND_ACHIEVEMENTS)

        assertTrue(result.isSuccess)
        assertEquals(listOf("habits", "achievements"), recorder.calls)
    }

    @Test
    fun profileOnly_runsOnlyProfileSync() = runBlocking {
        val recorder = CallRecorder()
        val orchestrator = buildSyncOrchestrator(recorder = recorder)

        val result = orchestrator("uid-1", SyncScope.PROFILE_ONLY)

        assertTrue(result.isSuccess)
        assertEquals(listOf("profile"), recorder.calls)
    }

    @Test
    fun fullScope_runsInDeterministicOrder() = runBlocking {
        val recorder = CallRecorder()
        val orchestrator = buildSyncOrchestrator(recorder = recorder)

        val result = orchestrator("uid-1", SyncScope.FULL)

        assertTrue(result.isSuccess)
        assertEquals(listOf("settings", "profile", "habits", "achievements"), recorder.calls)
    }
}
