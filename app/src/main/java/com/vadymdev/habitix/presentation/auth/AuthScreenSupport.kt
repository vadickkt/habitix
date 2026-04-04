package com.vadymdev.habitix.presentation.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.vadymdev.habitix.R
import com.vadymdev.habitix.ui.theme.BrandGreen
import com.vadymdev.habitix.ui.theme.TextSecondary

@Composable
internal fun BenefitChip(text: String) {
    Row(
        modifier = Modifier
            .background(Color(0xFFFFFFFF), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(6.dp)
                .background(BrandGreen, CircleShape)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(text = text, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

internal fun resolveWebClientId(context: Context): String {
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

internal suspend fun requestGoogleIdToken(context: Context, webClientId: String): Result<String> {
    return runCatching {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = context,
            request = request
        )

        val credential = result.credential
        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
        googleCredential.idToken
    }
}
