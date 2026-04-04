package com.vadymdev.habitix.presentation.auth

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.vadymdev.habitix.R
import com.vadymdev.habitix.domain.model.AppLanguage
import com.vadymdev.habitix.ui.theme.AppBackground
import com.vadymdev.habitix.ui.theme.TextPrimary
import com.vadymdev.habitix.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    language: AppLanguage,
    onAuthorized: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    googleSignInRequest: suspend (Context, String) -> GoogleSignInOutcome = ::requestGoogleIdToken
) {
    val tag = "AuthScreen"
    val isUk = language == AppLanguage.UK
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val webClientId = resolveWebClientId(context)
    val coroutineScope = rememberCoroutineScope()
    val googleIconRequest = remember(context) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/google_icon.svg")
            .decoderFactory(SvgDecoder.Factory())
            .build()
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
        AuthLoadingContent(
            currentStepIndex = state.loadingStepIndex,
            isUk = isUk
        )
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
            text = stringResource(R.string.auth_hello),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.auth_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(34.dp))

        Button(
            onClick = {
                viewModel.setError(null)
                if (webClientId.isBlank()) {
                    viewModel.setError(context.getString(R.string.auth_google_setup_error))
                    return@Button
                }

                coroutineScope.launch {
                    when (val result = googleSignInRequest(context, webClientId)) {
                        is GoogleSignInOutcome.Success -> {
                            if (result.idToken.isBlank()) {
                                viewModel.setError(context.getString(R.string.auth_google_setup_error))
                            } else {
                                viewModel.signInWithGoogleToken(result.idToken)
                            }
                        }

                        GoogleSignInOutcome.Canceled -> {
                            // User canceled account chooser intentionally: keep screen silent.
                            viewModel.setError(null, promoteGuestFallback = false)
                        }

                        is GoogleSignInOutcome.Failure -> {
                            val error = result.error
                            val kind = classifyAuthFailure(error)
                            Log.w(tag, "CredentialManager sign-in failed: kind=$kind message=${error.message}", error)

                            val message = when (kind) {
                                AuthFailureKind.USER_CANCELED -> null
                                AuthFailureKind.NO_ACCOUNT -> context.getString(R.string.auth_error_canceled_or_no_account)
                                AuthFailureKind.PROVIDER_UNAVAILABLE -> context.getString(R.string.auth_error_provider_unavailable)
                                AuthFailureKind.TRANSIENT -> context.getString(R.string.auth_error_transient)
                                AuthFailureKind.UNKNOWN -> context.getString(R.string.auth_error_unknown)
                            }
                            viewModel.setError(message, promoteGuestFallback = message != null && shouldPromoteGuest(kind))
                        }
                    }
                }
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
                    text = stringResource(R.string.auth_sign_in_google),
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

            if (state.promoteGuestFallback) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.continueAsGuest(onDone = onContinueAsGuest) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.auth_continue_guest), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            BenefitChip(stringResource(R.string.auth_benefit_secure))
            Spacer(modifier = Modifier.size(8.dp))
            BenefitChip(stringResource(R.string.auth_benefit_fast))
            Spacer(modifier = Modifier.size(8.dp))
            BenefitChip(stringResource(R.string.auth_benefit_free))
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(R.string.auth_continue_guest),
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .padding(bottom = 14.dp)
                .clickable {
                    viewModel.continueAsGuest(onDone = onContinueAsGuest)
                }
        )

        Text(
            text = stringResource(R.string.auth_terms_note),
            color = TextSecondary.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = TextDecoration.Underline
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable(onClick = onOpenPrivacyPolicy)
        )

        Spacer(modifier = Modifier.height(6.dp))
    }
}

