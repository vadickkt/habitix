package com.vadymdev.habitix.presentation.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.vadymdev.habitix.R
import java.io.File
import java.io.FileOutputStream

internal fun copyLink(context: Context, state: ProfileUiState, isUk: Boolean) {
    val text = buildShareText(context, state, isUk)
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("progress", text))
    Toast.makeText(context, t(context, isUk, R.string.profile_copied_uk, R.string.profile_copied_en), Toast.LENGTH_SHORT).show()
}

internal fun shareText(context: Context, state: ProfileUiState, isUk: Boolean) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, buildShareText(context, state, isUk))
    }
    context.startActivity(Intent.createChooser(shareIntent, t(context, isUk, R.string.profile_share_progress_uk, R.string.profile_share_progress_en)))
}

internal fun shareToInstagram(context: Context, state: ProfileUiState, style: ShareStyle, isUk: Boolean) {
    shareToPackage(context, "com.instagram.android", "https://instagram.com", state, style, isUk)
}

internal fun shareToTwitter(context: Context, state: ProfileUiState, style: ShareStyle, isUk: Boolean) {
    shareToPackage(context, "com.twitter.android", "https://x.com", state, style, isUk)
}

internal fun shareToTelegram(context: Context, state: ProfileUiState, style: ShareStyle, isUk: Boolean) {
    shareToPackage(context, "org.telegram.messenger", "https://t.me", state, style, isUk)
}

internal fun shareImageToAny(context: Context, state: ProfileUiState, style: ShareStyle, isUk: Boolean) {
    val uri = createShareImageUri(context, state, style, isUk)
    if (uri == null) {
        Toast.makeText(context, t(context, isUk, R.string.profile_prepare_image_failed_uk, R.string.profile_prepare_image_failed_en), Toast.LENGTH_SHORT).show()
        return
    }

    val text = buildShareText(context, state, isUk)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, t(context, isUk, R.string.profile_share_progress_uk, R.string.profile_share_progress_en)))
}

private fun shareToPackage(
    context: Context,
    packageName: String,
    fallbackUrl: String,
    state: ProfileUiState,
    style: ShareStyle,
    isUk: Boolean
) {
    val uri = createShareImageUri(context, state, style, isUk)
    if (uri == null) {
        Toast.makeText(context, t(context, isUk, R.string.profile_prepare_image_failed_uk, R.string.profile_prepare_image_failed_en), Toast.LENGTH_SHORT).show()
        return
    }

    val text = buildShareText(context, state, isUk)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        setPackage(packageName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)))
        }
    }
}

private fun createShareImageUri(context: Context, state: ProfileUiState, style: ShareStyle, isUk: Boolean): Uri? {
    return runCatching {
        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val shareFile = File(shareDir, "share_${System.currentTimeMillis()}.png")
        val bitmap = renderShareBitmap(context, state, style, isUk)
        FileOutputStream(shareFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", shareFile)
    }.getOrNull()
}

internal fun persistReadPermissionIfPossible(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

internal fun createTempImageUri(context: Context): Uri {
    val imageDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File(imageDir, "avatar_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

internal fun resolveAvatarModel(value: String?): Any? {
    if (value.isNullOrBlank()) return null
    return if (
        value.startsWith("content://") ||
        value.startsWith("file://") ||
        value.startsWith("http://") ||
        value.startsWith("https://") ||
        value.startsWith("android.resource://")
    ) {
        value
    } else {
        File(value)
    }
}

internal fun saveShareCardToGallery(context: Context, state: ProfileUiState, style: ShareStyle, isUk: Boolean) {
    runCatching {
        val bitmap = renderShareBitmap(context, state, style, isUk)
        val fileName = "habitix_share_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver

        val values = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Habitix")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values) ?: error(t(context, isUk, R.string.profile_create_file_failed_uk, R.string.profile_create_file_failed_en))

        resolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } ?: error(t(context, isUk, R.string.profile_write_png_failed_uk, R.string.profile_write_png_failed_en))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
    }.onSuccess {
        Toast.makeText(context, t(context, isUk, R.string.profile_png_saved_uk, R.string.profile_png_saved_en), Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, it.message ?: t(context, isUk, R.string.profile_save_png_failed_uk, R.string.profile_save_png_failed_en), Toast.LENGTH_SHORT).show()
    }
}

private fun renderShareBitmap(context: Context, state: ProfileUiState, style: ShareStyle, isUk: Boolean): Bitmap {
    val width = 1080
    val height = 1350
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val background = Paint().apply {
        shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            style.gradientColors(),
            null,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 56f, 56f, background)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 54f
        isFakeBoldText = true
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#EAF5EF")
        textSize = 34f
    }
    canvas.drawText(state.identity.displayName, 88f, 130f, titlePaint)
    canvas.drawText(t(context, isUk, R.string.profile_level_uk, R.string.profile_level_en, state.analytics.level), 88f, 180f, subtitlePaint)

    drawStatBlock(canvas, 80f, 250f, 430f, 240f, "${state.analytics.currentStreakDays}", t(context, isUk, R.string.profile_days_in_row_uk, R.string.profile_days_in_row_en))
    drawStatBlock(canvas, 570f, 250f, 430f, 240f, "${state.analytics.bestStreakDays}", t(context, isUk, R.string.profile_best_streak_short_uk, R.string.profile_best_streak_short_en))
    drawStatBlock(canvas, 80f, 540f, 430f, 240f, "${state.analytics.totalCompleted}", t(context, isUk, R.string.profile_completed_short_uk, R.string.profile_completed_short_en))
    drawStatBlock(canvas, 570f, 540f, 430f, 240f, "${state.analytics.daysWithUs}", t(context, isUk, R.string.profile_days_with_habitix_uk, R.string.profile_days_with_habitix_en))

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 38f
        isFakeBoldText = true
    }
    canvas.drawText("Habitix", 88f, 1260f, footerPaint)

    return bitmap
}

private fun drawStatBlock(
    canvas: Canvas,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    value: String,
    label: String
) {
    val block = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(45, 255, 255, 255)
    }
    canvas.drawRoundRect(left, top, left + width, top + height, 36f, 36f, block)

    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 62f
        isFakeBoldText = true
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#EAF5EF")
        textSize = 32f
    }

    canvas.drawText(value, left + 40f, top + 118f, valuePaint)
    canvas.drawText(label, left + 40f, top + 176f, labelPaint)
}

private fun buildShareText(context: Context, state: ProfileUiState, isUk: Boolean): String {
    return t(
        context,
        isUk,
        R.string.profile_share_text_uk,
        R.string.profile_share_text_en,
        state.analytics.level,
        state.analytics.xpCurrent,
        state.analytics.xpTarget,
        state.analytics.currentStreakDays,
        state.analytics.bestStreakDays,
        state.analytics.totalCompleted
    )
}
