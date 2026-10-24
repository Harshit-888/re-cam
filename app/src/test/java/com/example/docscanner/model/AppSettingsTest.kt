package com.example.docscanner.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Round-trip and compatibility tests for the settings model.
 *
 * The settings file is read before the first frame, so a decoding failure here
 * means the app loses the user's theme and PDF defaults on every launch. These
 * tests pin the two properties that matter: defaults survive a missing file, and
 * a file written by a *different* build still decodes.
 */
class AppSettingsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `defaults match the documented starting point`() {
        val settings = AppSettings()
        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertEquals(AccentColor.BLUE, settings.accentColor)
        assertTrue(settings.useInAppCamera)
        assertEquals(ImageQuality.HIGH, settings.imageQuality)
        assertEquals(PaperSize.A4, settings.pageSize)
        assertEquals(ExportResolution.HIGH, settings.resolution)
        assertEquals(PageOrientation.AUTO, settings.pageOrientation)
        assertEquals(ImageScale.FIT, settings.imageScale)
        assertEquals("Document", settings.defaultDocumentName)
        assertEquals("", settings.emailTo)
        assertEquals("", settings.emailSubject)
    }

    @Test
    fun `round trips through json`() {
        val original = AppSettings(
            themeMode = ThemeMode.LIGHT,
            accentColor = AccentColor.TEAL,
            useInAppCamera = false,
            shutterSound = true,
            imageQuality = ImageQuality.LOW,
            autoSaveToGallery = true,
            defaultDocumentName = "Invoices",
            pageSize = PaperSize.LEGAL,
            resolution = ExportResolution.LOW,
            pageOrientation = PageOrientation.LANDSCAPE,
            imageScale = ImageScale.FILL,
            emailTo = "office@example.com",
            emailSubject = "Scan attached"
        )

        val decoded = json.decodeFromString(
            AppSettings.serializer(),
            json.encodeToString(AppSettings.serializer(), original)
        )

        assertEquals(original, decoded)
    }

    @Test
    fun `unknown keys from a newer build are ignored`() {
        val payload = """
            {"themeMode":"LIGHT","ocrLanguage":"ENGLISH","syncEnabled":true}
        """.trimIndent()

        val decoded = json.decodeFromString(AppSettings.serializer(), payload)

        assertEquals(ThemeMode.LIGHT, decoded.themeMode)
        // Everything absent from the payload falls back to its default rather
        // than throwing, which is what keeps an upgrade from wiping settings.
        assertEquals(AppSettings(), decoded.copy(themeMode = ThemeMode.DARK))
    }

    @Test
    fun `accent palettes keep readable contrast`() {
        AccentColor.entries.forEach { accent ->
            assertNotEquals(
                "onPrimary must differ from primary for ${accent.label}",
                accent.lightPrimary,
                accent.lightOnPrimary
            )
            assertNotEquals(
                "dark onPrimary must differ from primary for ${accent.label}",
                accent.darkPrimary,
                accent.darkOnPrimary
            )
        }
    }

    @Test
    fun `image quality presets stay inside jpeg range and ordered`() {
        assertTrue(ImageQuality.HIGH.maxDimension > ImageQuality.MEDIUM.maxDimension)
        assertTrue(ImageQuality.MEDIUM.maxDimension > ImageQuality.LOW.maxDimension)
        ImageQuality.entries.forEach { quality ->
            assertTrue(quality.jpegQuality in 1..100)
        }
    }

    @Test
    fun `export resolution increases with dpi label`() {
        assertTrue(
            ExportResolution.HIGH.maxImageDimensionPx > ExportResolution.MEDIUM.maxImageDimensionPx
        )
        assertTrue(
            ExportResolution.MEDIUM.maxImageDimensionPx > ExportResolution.LOW.maxImageDimensionPx
        )
    }
}
