package com.vadymdev.habitix.presentation.auth

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.vadymdev.habitix.R
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthorized: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val webClientId = resolveWebClientId(context)
    val googleIconRequest = remember(context) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/google_icon.svg")
            .decoderFactory(SvgDecoder.Factory())
            .build()
    }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    viewModel.setError(context.getString(R.string.auth_google_setup_error))
                } else {
                    viewModel.signInWithGoogleToken(idToken)
                }
            } catch (_: ApiException) {
                viewModel.setError("Помилка Google входу. Спробуйте ще раз")
            }
        }
    }

    LaunchedEffect(webClientId) {
        if (webClientId.isBlank()) {
            viewModel.setError(context.getString(R.string.auth_google_setup_error))
        }
    }

    LaunchedEffect(state.isAuthorized) {
        if (state.isAuthorized) {
            onAuthorized()
        }
    }

    if (state.showLoadingFlow) {
        AuthLoadingContent(currentStepIndex = state.loadingStepIndex)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.start_logo_auth),
            contentDescription = "Mascot",
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Привіт!",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Я допоможу тобі сформувати корисні звички",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(34.dp))

        Button(
            onClick = {
                if (webClientId.isBlank()) {
                    viewModel.setError(context.getString(R.string.auth_google_setup_error))
                    return@Button
                }

                val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(webClientId)
                    .build()
                val client = GoogleSignIn.getClient(context, options)
                activityResultLauncher.launch(client.signInIntent)
            },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
            shape = RoundedCornerShape(34.dp),
            border = BorderStroke(2.dp, Color(0xFF8F8F8F)),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF2A2A2A),
                disabledContainerColor = Color(0xFFF3F3F3),
                disabledContentColor = Color(0xFF9C9C9C)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = googleIconRequest,
                    contentDescription = "Google",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.size(14.dp))
                Text(
                    text = "Sign in with Google",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2A2A2A)
                )
            }
        }

        if (!state.error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.error.orEmpty(),
                color = Color(0xFFB3261E),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            BenefitChip("Безпечно")
            Spacer(modifier = Modifier.size(8.dp))
            BenefitChip("Швидко")
            Spacer(modifier = Modifier.size(8.dp))
            BenefitChip("Безкоштовно")
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Продовжити як гість",
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .padding(bottom = 14.dp)
                .clickable {
                    viewModel.continueAsGuest(onDone = onContinueAsGuest)
                }
        )

        Text(
            text = "Продовжуючи, ви погоджуєтесь з Умовами та\nПолітикою",
            color = TextSecondary.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = TextDecoration.Underline
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun BenefitChip(text: String) {
    Row(
        modifier = Modifier
            .background(Color(0xFFFFFFFF), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(BrandGreen, CircleShape)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(text = text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun resolveWebClientId(context: Context): String {
    val generatedId = context.resources.getIdentifier(
        "default_web_client_id",
        "string",
        context.packageName
    )

    if (generatedId != 0) {
        val value = context.getString(generatedId)
        if (value.isNotBlank()) return value
    }

    return context.getString(R.string.google_web_client_id)
}
