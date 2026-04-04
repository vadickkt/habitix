package com.vadymdev.habitix.presentation.profile

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
internal fun t(isUk: Boolean, @StringRes ukResId: Int, @StringRes enResId: Int, vararg formatArgs: Any): String {
    return if (isUk) {
        stringResource(ukResId, *formatArgs)
    } else {
        stringResource(enResId, *formatArgs)
    }
}

internal fun t(context: Context, isUk: Boolean, @StringRes ukResId: Int, @StringRes enResId: Int, vararg formatArgs: Any): String {
    return context.getString(if (isUk) ukResId else enResId, *formatArgs)
}
