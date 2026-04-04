package com.vadymdev.habitix.presentation.profile

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.ui.theme.AppBackground

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    language: AppLanguage,
    onUpdateName: (String) -> Unit,
    onUpdateBio: (String) -> Unit,
    onUpdateAvatar: (String?) -> Unit,
    onOpenAllAchievements: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val isUk = language == AppLanguage.UK
    var showShare by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(false) }
    var editBio by remember { mutableStateOf(false) }
    var nameValidationError by remember { mutableStateOf<String?>(null) }
    var showAvatarActions by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingAvatarSourceUri by remember { mutableStateOf<Uri?>(null) }
    var showAvatarEditor by remember { mutableStateOf(false) }
    var draftName by remember(state.identity.displayName) { mutableStateOf(state.identity.displayName) }
    var draftBio by remember(state.identity.bio) { mutableStateOf(state.identity.bio) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            persistReadPermissionIfPossible(context, uri)
            pendingAvatarSourceUri = uri
            showAvatarEditor = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val sourceUri = pendingCameraUri
            if (sourceUri != null) {
                pendingAvatarSourceUri = sourceUri
                showAvatarEditor = true
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, t(context, isUk, R.string.profile_camera_permission_required_uk, R.string.profile_camera_permission_required_en), Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = t(isUk, R.string.nav_profile_uk, R.string.nav_profile_en),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            item {
                ProfileHeader(
                    state = state,
                    isUk = isUk,
                    onEditAvatar = { showAvatarActions = true },
                    onEditName = { editName = true },
                    onEditBio = { editBio = true },
                    onAvatarBroken = { onUpdateAvatar(null) }
                )
            }

            item { ProfileStatsGrid(state = state, isUk = isUk) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t(isUk, R.string.achievements_title_uk, R.string.achievements_title_en), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = t(isUk, R.string.profile_all_uk, R.string.profile_all_en),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onOpenAllAchievements)
                    )
                }
            }

            items(state.analytics.topAchievements.size) { index ->
                val achievement = state.analytics.topAchievements[index]
                AchievementCard(achievement = achievement, compact = true, isUk = isUk)
            }

            item {
                MonthActivityCard(state = state, isUk = isUk)
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { showShare = true }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t(isUk, R.string.profile_share_progress_uk, R.string.profile_share_progress_en), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        ProfileBottomBar(
            onHome = onOpenDashboard,
            onStats = onOpenStats,
            onProfile = {},
            onSettings = onOpenSettings,
            isUk = isUk,
            activeTab = "profile"
        )
    }

    if (showShare) {
        ShareProgressDialog(state = state, isUk = isUk, onDismiss = { showShare = false })
    }

    if (showAvatarActions) {
        AlertDialog(
            onDismissRequest = { showAvatarActions = false },
            confirmButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showAvatarActions = false
                }) {
                    Text(t(isUk, R.string.profile_gallery_uk, R.string.profile_gallery_en))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val permissionState = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                    if (permissionState == PackageManager.PERMISSION_GRANTED) {
                        val uri = createTempImageUri(context)
                        pendingCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                    showAvatarActions = false
                }) {
                    Text(t(isUk, R.string.profile_camera_uk, R.string.profile_camera_en))
                }
            },
            title = { Text(t(isUk, R.string.profile_choose_avatar_uk, R.string.profile_choose_avatar_en)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t(isUk, R.string.profile_avatar_local_note_uk, R.string.profile_avatar_local_note_en))
                    Text(t(isUk, R.string.profile_avatar_edit_note_uk, R.string.profile_avatar_edit_note_en))
                    TextButton(onClick = {
                        onUpdateAvatar(null)
                        showAvatarActions = false
                    }) {
                        Text(t(isUk, R.string.profile_reset_avatar_uk, R.string.profile_reset_avatar_en))
                    }
                }
            }
        )
    }

    val editorUri = pendingAvatarSourceUri
    if (showAvatarEditor && editorUri != null) {
        AvatarEditorSheet(
            sourceUri = editorUri,
            isUk = isUk,
            onDismiss = {
                showAvatarEditor = false
                pendingAvatarSourceUri = null
            },
            onConfirm = { transform ->
                val localPath = saveAvatarWithTransform(context, editorUri, transform)
                if (localPath == null) {
                    Toast.makeText(context, t(context, isUk, R.string.profile_image_process_failed_uk, R.string.profile_image_process_failed_en), Toast.LENGTH_SHORT).show()
                } else {
                    onUpdateAvatar(localPath)
                    Toast.makeText(context, t(context, isUk, R.string.profile_avatar_updated_uk, R.string.profile_avatar_updated_en), Toast.LENGTH_SHORT).show()
                }
                showAvatarEditor = false
                pendingAvatarSourceUri = null
            }
        )
    }

    if (editName) {
        val emptyNameError = t(isUk, R.string.profile_name_empty_error_uk, R.string.profile_name_empty_error_en)
        val shortNameError = t(isUk, R.string.profile_name_short_error_uk, R.string.profile_name_short_error_en)
        EditTextDialog(
            title = t(isUk, R.string.profile_name_uk, R.string.profile_name_en),
            value = draftName,
            onValueChange = { draftName = it },
            error = nameValidationError,
            confirmText = t(isUk, R.string.common_save_uk, R.string.common_save_en),
            dismissText = t(isUk, R.string.common_cancel_uk, R.string.common_cancel_en),
            onDismiss = { editName = false },
            onConfirm = {
                val trimmed = draftName.trim()
                when {
                    trimmed.isBlank() -> nameValidationError = emptyNameError
                    trimmed.length < 2 -> nameValidationError = shortNameError
                    else -> {
                        nameValidationError = null
                        onUpdateName(trimmed)
                        editName = false
                    }
                }
            }
        )
    }

    if (editBio) {
        EditTextDialog(
            title = t(isUk, R.string.profile_bio_uk, R.string.profile_bio_en),
            value = draftBio,
            onValueChange = { draftBio = it },
            confirmText = t(isUk, R.string.common_save_uk, R.string.common_save_en),
            dismissText = t(isUk, R.string.common_cancel_uk, R.string.common_cancel_en),
            onDismiss = { editBio = false },
            onConfirm = {
                onUpdateBio(draftBio)
                editBio = false
            }
        )
    }
}

