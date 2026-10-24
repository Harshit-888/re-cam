package com.example.docscanner.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.docscanner.model.AccentColor
import com.example.docscanner.model.AppSettings
import com.example.docscanner.model.ExportResolution
import com.example.docscanner.model.ImageQuality
import com.example.docscanner.model.ImageScale
import com.example.docscanner.model.PageOrientation
import com.example.docscanner.model.PaperSize
import com.example.docscanner.model.ThemeMode

/**
 * Preferences screen.
 *
 * Every row writes straight into [com.example.docscanner.data.ScannerRepository]
 * through [onSettingsChange], so the effect is immediate and persisted — there is
 * no OK/Apply step and no local copy of the settings to drift out of sync.
 *
 * Rows the original app had that are **not** here: *Camera Engine* (CameraX is
 * the only engine this app ships), *Image Picker for Scanning* (gallery import
 * always uses the system photo picker), *OCR Language* / *Sync to Cloud* (neither
 * feature exists yet). A row that persists a value nothing reads would be a fake
 * control, which `AGENTS.md` §2.7 rules out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: ((AppSettings) -> AppSettings) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var activeDialog by remember { mutableStateOf<SettingsDialog?>(null) }

    // Saving to the public Pictures folder is only permission-free from Android 10
    // (scoped storage). On 8/9 the app needs WRITE_EXTERNAL_STORAGE, so ask at the
    // moment the user turns the row on rather than at launch.
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onSettingsChange { it.copy(autoSaveToGallery = granted) }
        if (!granted) {
            Toast.makeText(
                context,
                "Storage permission is needed to save copies to the gallery",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun setAutoSaveToGallery() {
        val alreadyGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            onSettingsChange { it.copy(autoSaveToGallery = true) }
        } else {
            galleryPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ------------------------------------------------------------ theme
            item { SectionHeader("Theme") }
            item {
                SettingsRow(
                    title = "Choose theme",
                    subtitle = settings.themeMode.label,
                    onClick = { activeDialog = SettingsDialog.THEME }
                )
            }
            item {
                SettingsRow(
                    title = "Color",
                    subtitle = settings.accentColor.label,
                    onClick = { activeDialog = SettingsDialog.ACCENT },
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(accentSwatch(settings.accentColor))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }
                )
            }
            item { SectionDivider() }

            // ------------------------------------------------------------- scan
            item { SectionHeader("Scan") }
            item {
                CheckRow(
                    title = "Use in-app camera",
                    subtitle = "Batch scan support. Off = the system photo picker opens instead.",
                    checked = settings.useInAppCamera,
                    onCheckedChange = { checked ->
                        onSettingsChange { it.copy(useInAppCamera = checked) }
                    }
                )
            }
            item {
                CheckRow(
                    title = "Shutter sound",
                    subtitle = "Play a click when a page is captured",
                    checked = settings.shutterSound,
                    onCheckedChange = { checked ->
                        onSettingsChange { it.copy(shutterSound = checked) }
                    }
                )
            }
            item {
                SettingsRow(
                    title = "Image quality",
                    subtitle = "${settings.imageQuality.label} — ${settings.imageQuality.maxDimension} px, JPEG ${settings.imageQuality.jpegQuality}",
                    onClick = { activeDialog = SettingsDialog.QUALITY }
                )
            }
            item {
                CheckRow(
                    title = "Auto-save to Gallery",
                    subtitle = "Keep a copy of every captured page in Pictures/DocScanner",
                    checked = settings.autoSaveToGallery,
                    onCheckedChange = { checked ->
                        if (checked) setAutoSaveToGallery()
                        else onSettingsChange { it.copy(autoSaveToGallery = false) }
                    }
                )
            }
            item { SectionDivider() }

            // -------------------------------------------------------------- pdf
            item { SectionHeader("PDF") }
            item {
                SettingsRow(
                    title = "Document Name",
                    subtitle = settings.defaultDocumentName.ifBlank { "none" },
                    onClick = { activeDialog = SettingsDialog.DOCUMENT_NAME }
                )
            }
            item {
                SettingsRow(
                    title = "Page Size",
                    subtitle = settings.pageSize.label,
                    onClick = { activeDialog = SettingsDialog.PAGE_SIZE }
                )
            }
            item {
                SettingsRow(
                    title = "Resolution",
                    subtitle = settings.resolution.label,
                    onClick = { activeDialog = SettingsDialog.RESOLUTION }
                )
            }
            item {
                SettingsRow(
                    title = "Page Orientation",
                    subtitle = settings.pageOrientation.label,
                    onClick = { activeDialog = SettingsDialog.ORIENTATION }
                )
            }
            item {
                SettingsRow(
                    title = "Image Scale",
                    subtitle = settings.imageScale.label,
                    onClick = { activeDialog = SettingsDialog.IMAGE_SCALE }
                )
            }
            item { SectionDivider() }

            // ---------------------------------------------------------- email
            item { SectionHeader("Default Email") }
            item {
                SettingsRow(
                    title = "To",
                    subtitle = settings.emailTo.ifBlank { "none" },
                    onClick = { activeDialog = SettingsDialog.EMAIL_TO }
                )
            }
            item {
                SettingsRow(
                    title = "Subject",
                    subtitle = settings.emailSubject.ifBlank { "none" },
                    onClick = { activeDialog = SettingsDialog.EMAIL_SUBJECT }
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    when (activeDialog) {
        SettingsDialog.THEME -> ChoiceDialog(
            title = "Choose theme",
            options = ThemeMode.entries,
            selected = settings.themeMode,
            optionLabel = { it.label },
            onSelect = { mode ->
                onSettingsChange { it.copy(themeMode = mode) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        SettingsDialog.ACCENT -> AccentDialog(
            selected = settings.accentColor,
            onSelect = { accent ->
                onSettingsChange { it.copy(accentColor = accent) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        SettingsDialog.QUALITY -> ChoiceDialog(
            title = "Image quality",
            options = ImageQuality.entries,
            selected = settings.imageQuality,
            optionLabel = { "${it.label} — ${it.maxDimension} px" },
            onSelect = { quality ->
                onSettingsChange { it.copy(imageQuality = quality) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        SettingsDialog.PAGE_SIZE -> ChoiceDialog(
            title = "Page size",
            options = PaperSize.entries,
            selected = settings.pageSize,
            optionLabel = { it.label },
            onSelect = { size ->
                onSettingsChange { it.copy(pageSize = size) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        SettingsDialog.RESOLUTION -> ChoiceDialog(
            title = "Resolution",
            options = ExportResolution.entries,
            selected = settings.resolution,
            optionLabel = { it.label },
            onSelect = { resolution ->
                onSettingsChange { it.copy(resolution = resolution) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        SettingsDialog.ORIENTATION -> ChoiceDialog(
            title = "Page orientation",
            options = PageOrientation.entries,
            selected = settings.pageOrientation,
            optionLabel = { it.label },
            onSelect = { orientation ->
                onSettingsChange { it.copy(pageOrientation = orientation) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        SettingsDialog.IMAGE_SCALE -> ChoiceDialog(
            title = "Image scale",
            options = ImageScale.entries,
            selected = settings.imageScale,
            optionLabel = { it.label },
            onSelect = { scale ->
                onSettingsChange { it.copy(imageScale = scale) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        SettingsDialog.DOCUMENT_NAME -> TextSettingDialog(
            title = "Document name",
            hint = "Used as the file name prefix for exported PDFs",
            initial = settings.defaultDocumentName,
            onSave = { value ->
                onSettingsChange { it.copy(defaultDocumentName = value) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        SettingsDialog.EMAIL_TO -> TextSettingDialog(
            title = "Default recipient",
            hint = "Filled into the share sheet when you email a PDF",
            initial = settings.emailTo,
            onSave = { value ->
                onSettingsChange { it.copy(emailTo = value) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        SettingsDialog.EMAIL_SUBJECT -> TextSettingDialog(
            title = "Default subject",
            hint = "Left blank = use the document title",
            initial = settings.emailSubject,
            onSave = { value ->
                onSettingsChange { it.copy(emailSubject = value) }
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )

        null -> Unit
    }
}

private enum class SettingsDialog {
    THEME, ACCENT, QUALITY, PAGE_SIZE, RESOLUTION, ORIENTATION, IMAGE_SCALE,
    DOCUMENT_NAME, EMAIL_TO, EMAIL_SUBJECT
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(top = 12.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun CheckRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(start = 20.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(option) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = optionLabel(option),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun AccentDialog(
    selected: AccentColor,
    onSelect: (AccentColor) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Color") },
        text = {
            Column {
                AccentColor.entries.forEach { accent ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(accent) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(accentSwatch(accent))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = accent.label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (accent == selected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun TextSettingDialog(
    title: String,
    hint: String,
    initial: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember(initial) { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        if (it.length <= AppSettings.MAX_TEXT_LENGTH) value = it
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(value.trim()) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * The accent's own colour for a swatch. Picks the light or dark variant from the
 * current surface luminance, so a pale pastel is never drawn on a white page (or
 * a deep tone on a black one) where it would read as "no colour at all".
 */
@Composable
private fun accentSwatch(accent: AccentColor): Color {
    val onLightSurface = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    return Color(if (onLightSurface) accent.lightPrimary else accent.darkPrimary)
}
