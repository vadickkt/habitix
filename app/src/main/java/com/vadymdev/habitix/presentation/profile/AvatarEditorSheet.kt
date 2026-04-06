package com.vadymdev.habitix.presentation.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

private const val AVATAR_EDITOR_TAG = "AvatarEditor"

data class AvatarEditorTransform(
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float,
    val viewportSizePx: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarEditorSheet(
    sourceUri: Uri,
    isUk: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (AvatarEditorTransform) -> Unit
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var viewportSizePx by remember { mutableStateOf(0) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isUk) "Редактор аватара" else "Avatar editor",
                style = MaterialTheme.typography.titleLarge
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1EFEB))
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                        .onSizeChanged { viewportSizePx = it.width }
                ) {
                    AsyncImage(
                        model = sourceUri,
                        contentDescription = if (isUk) "Попередній перегляд аватара" else "Avatar preview",
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = offsetX
                                translationY = offsetY
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    zoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Text(
                text = if (isUk) "Масштаб" else "Zoom",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = zoom,
                onValueChange = { zoom = it.coerceIn(1f, 4f) },
                valueRange = 1f..4f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    zoom = 1f
                    offsetX = 0f
                    offsetY = 0f
                }) {
                    Text(if (isUk) "Скинути" else "Reset")
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onDismiss) {
                    Text(if (isUk) "Скасувати" else "Cancel")
                }

                Button(
                    onClick = {
                        onConfirm(
                            AvatarEditorTransform(
                                zoom = zoom,
                                offsetX = offsetX,
                                offsetY = offsetY,
                                viewportSizePx = viewportSizePx
                            )
                        )
                    },
                    enabled = viewportSizePx > 0
                ) {
                    Text(if (isUk) "Зберегти" else "Save")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

fun saveAvatarWithTransform(
    context: Context,
    sourceUri: Uri,
    transform: AvatarEditorTransform
): String? {
    val transformedPath = runCatching {
        val sourceBitmap = decodeBitmapFromUri(context, sourceUri)
            ?: throw IllegalStateException("Unable to decode avatar source bitmap")
        val srcWidth = sourceBitmap.width.toFloat()
        val srcHeight = sourceBitmap.height.toFloat()
        val viewport = transform.viewportSizePx.toFloat().coerceAtLeast(1f)

        val baseScale = max(viewport / srcWidth, viewport / srcHeight)
        val finalScale = baseScale * transform.zoom.coerceIn(1f, 4f)

        val srcVisibleWidth = (viewport / finalScale).coerceIn(1f, srcWidth)
        val srcVisibleHeight = (viewport / finalScale).coerceIn(1f, srcHeight)

        val centerX = (srcWidth / 2f) - (transform.offsetX / finalScale)
        val centerY = (srcHeight / 2f) - (transform.offsetY / finalScale)

        val left = (centerX - srcVisibleWidth / 2f).coerceIn(0f, srcWidth - srcVisibleWidth)
        val top = (centerY - srcVisibleHeight / 2f).coerceIn(0f, srcHeight - srcVisibleHeight)

        val srcRect = Rect(
            left.roundToInt(),
            top.roundToInt(),
            (left + srcVisibleWidth).roundToInt(),
            (top + srcVisibleHeight).roundToInt()
        )

        val outputSize = 512
        val squared = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val squaredCanvas = Canvas(squared)
        squaredCanvas.drawBitmap(sourceBitmap, srcRect, Rect(0, 0, outputSize, outputSize), Paint(Paint.ANTI_ALIAS_FLAG))

        val circular = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val circularCanvas = Canvas(circular)

        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        }
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }

        circularCanvas.drawOval(RectF(0f, 0f, outputSize.toFloat(), outputSize.toFloat()), maskPaint)
        circularCanvas.drawBitmap(squared, Rect(0, 0, outputSize, outputSize), Rect(0, 0, outputSize, outputSize), imagePaint)

        val avatarsDir = File(context.filesDir, "avatars").apply { mkdirs() }
        val avatarFile = File(avatarsDir, "avatar_${System.currentTimeMillis()}.png")
        FileOutputStream(avatarFile).use { output ->
            val compressed = circular.compress(Bitmap.CompressFormat.PNG, 100, output)
            if (!compressed) {
                throw IOException("Failed to compress transformed avatar bitmap")
            }
        }

        sourceBitmap.recycle()
        squared.recycle()
        circular.recycle()

        avatarFile.absolutePath
    }
        .onFailure { error ->
            Log.w(AVATAR_EDITOR_TAG, "Transformed avatar save failed, will fallback to original copy", error)
        }
        .getOrNull()

    if (transformedPath != null) {
        return transformedPath
    }

    return runCatching {
        val avatarsDir = File(context.filesDir, "avatars")
        if (!avatarsDir.exists() && !avatarsDir.mkdirs()) {
            throw IOException("Unable to create avatar directory")
        }
        val avatarFile = File(avatarsDir, "avatar_fallback_${System.currentTimeMillis()}.img")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(avatarFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Unable to open avatar input stream")
        avatarFile.absolutePath
    }
        .onFailure { error ->
            Log.e(AVATAR_EDITOR_TAG, "Fallback avatar save failed", error)
        }
        .getOrNull()
}

private fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }
    }
        .onFailure { error ->
            Log.w(AVATAR_EDITOR_TAG, "Avatar decode failed", error)
        }
        .getOrNull()
}
