package com.vadymdev.habitix.data.local

import com.vadymdev.habitix.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsLocaleDefaultsTest {

    @Test
    fun defaultLanguageForSystemLocale_ukrainian_returnsUk() {
        assertEquals(AppLanguage.UK, defaultLanguageForSystemLocale("uk"))
        assertEquals(AppLanguage.UK, defaultLanguageForSystemLocale("UK"))
    }

    @Test
    fun defaultLanguageForSystemLocale_nonUkrainian_returnsEn() {
        assertEquals(AppLanguage.EN, defaultLanguageForSystemLocale("en"))
        assertEquals(AppLanguage.EN, defaultLanguageForSystemLocale("de"))
        assertEquals(AppLanguage.EN, defaultLanguageForSystemLocale("fr"))
    }
}
