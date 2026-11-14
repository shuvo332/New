package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AuditWithItems
import com.example.ui.components.AuditSummaryAndExportSection
import com.example.ui.components.AuditorTopHeader
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.MintGlow
import com.example.ui.theme.MissingRed
import com.example.ui.theme.MissingRedBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.TealDeep
import com.example.ui.theme.TealOcean
import com.example.ui.viewmodel.AnalysisUiState
import com.example.ui.viewmodel.ImageSlotState
import com.example.ui.viewmodel.SmartAuditUiState

@Composable
fun SmartAuditScreen(
    uiState: SmartAuditUiState,
    activeResultAudit: AuditWithItems?,
    onOpenMenu: () -> Unit,
    onOrderRefChange: (String) -> Unit,
    onVendorNameChange: (String) -> Unit,
    onScreenshotPicked: (Uri) -> Unit,
    onInvoicePicked: (Uri) -> Unit,
    onOpenCameraScanner: (slot: String) -> Unit,
    onLoadSampleScreenshot: () -> Unit,
    onLoadSampleInvoice: () -> Unit,
    onLoadDemoPair: () -> Unit,
    onClearScreenshot: () -> Unit,
    onClearInvoice: () -> Unit,
    onRunInstantAudit: () -> Unit,
    onToggleLineItemResolved: (Long, Boolean) -> Unit,
    onExportPdf: (AuditWithItems) -> Unit,
    onShareText: (AuditWithItems) -> Unit,
    onCopyText: (AuditWithItems) -> Unit
) {
    val ssPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(onScreenshotPicked) }

    val invPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(onInvoicePicked) }

    Column(modifier = Modifier.fillMaxSize()) {
        AuditorTopHeader(
            title = stringResource(R.string.smart_audit_title),
            subtitle = "System SS vs Invoice AI Engine",
            onMenuClick = onOpenMenu,
            trailingContent = {
                Surface(
                    onClick = onLoadDemoPair,
                    shape = RoundedCornerShape(50),
                    color = MintGlow.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, MintGlow.copy(alpha = 0.7f)),
                    modifier = Modifier.testTag("load_demo_pair_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.btn_load_demo_pair),
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite
                        )
                    }
                }
            }
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 780.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.smart_audit_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite.copy(alpha = 0.90f)
                    )
                }

                // Optional Order Reference & Vendor fields
                item {
                    GlassCard(containerColor = PureWhite.copy(alpha = 0.16f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.orderRefInput,
                                onValueChange = onOrderRefChange,
                                label = { Text(stringResource(R.string.label_order_ref)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("order_ref_input"),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PureWhite,
                                    unfocusedContainerColor = PureWhite.copy(alpha = 0.94f),
                                    focusedTextColor = SlateInk,
                                    unfocusedTextColor = SlateInk,
                                    focusedBorderColor = MintGlow,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedLabelColor = TealDeep,
                                    unfocusedLabelColor = SlateMuted
                                )
                            )
                            OutlinedTextField(
                                value = uiState.vendorInput,
                                onValueChange = onVendorNameChange,
                                label = { Text(stringResource(R.string.label_vendor_name)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_name_input"),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PureWhite,
                                    unfocusedContainerColor = PureWhite.copy(alpha = 0.94f),
                                    focusedTextColor = SlateInk,
                                    unfocusedTextColor = SlateInk,
                                    focusedBorderColor = MintGlow,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedLabelColor = TealDeep,
                                    unfocusedLabelColor = SlateMuted
                                )
                            )
                        }
                    }
                }

                // Dual Upload Zones (Side-by-side on medium/tablet, stacked on phone)
                item {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isWide = maxWidth >= 600.dp
                        if (isWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                UploadDropZoneCard(
                                    title = stringResource(R.string.upload_ss_title),
                                    subtitle = stringResource(R.string.upload_ss_hint),
                                    icon = Icons.Filled.Monitor,
                                    slot = uiState.screenshotSlot,
                                    previewContentDescription = stringResource(R.string.cd_system_screenshot_preview),
                                    testTagPrefix = "ss",
                                    onPickGallery = {
                                        ssPhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onCaptureCamera = { onOpenCameraScanner("ss") },
                                    onLoadSample = onLoadSampleScreenshot,
                                    onClear = onClearScreenshot,
                                    modifier = Modifier.weight(1f)
                                )
                                UploadDropZoneCard(
                                    title = stringResource(R.string.upload_invoice_title),
                                    subtitle = stringResource(R.string.upload_invoice_hint),
                                    icon = Icons.Filled.ReceiptLong,
                                    slot = uiState.invoiceSlot,
                                    previewContentDescription = stringResource(R.string.cd_invoice_image_preview),
                                    testTagPrefix = "invoice",
                                    onPickGallery = {
                                        invPhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onCaptureCamera = { onOpenCameraScanner("invoice") },
                                    onLoadSample = onLoadSampleInvoice,
                                    onClear = onClearInvoice,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                UploadDropZoneCard(
                                    title = stringResource(R.string.upload_ss_title),
                                    subtitle = stringResource(R.string.upload_ss_hint),
                                    icon = Icons.Filled.Monitor,
                                    slot = uiState.screenshotSlot,
                                    previewContentDescription = stringResource(R.string.cd_system_screenshot_preview),
                                    testTagPrefix = "ss",
                                    onPickGallery = {
                                        ssPhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onCaptureCamera = { onOpenCameraScanner("ss") },
                                    onLoadSample = onLoadSampleScreenshot,
                                    onClear = onClearScreenshot
                                )
                                UploadDropZoneCard(
                                    title = stringResource(R.string.upload_invoice_title),
                                    subtitle = stringResource(R.string.upload_invoice_hint),
                                    icon = Icons.Filled.ReceiptLong,
                                    slot = uiState.invoiceSlot,
                                    previewContentDescription = stringResource(R.string.cd_invoice_image_preview),
                                    testTagPrefix = "invoice",
                                    onPickGallery = {
                                        invPhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onCaptureCamera = { onOpenCameraScanner("invoice") },
                                    onLoadSample = onLoadSampleInvoice,
                                    onClear = onClearInvoice
                                )
                            }
                        }
                    }
                }

                // Error Banner if any
                if (uiState.analysisState is AnalysisUiState.Error) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MissingRedBg,
                            border = BorderStroke(1.dp, MissingRed.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = uiState.analysisState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MissingRed,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                // Run Instant Audit CTA Button
                item {
                    val isRunning = uiState.analysisState is AnalysisUiState.Running
                    Button(
                        onClick = onRunInstantAudit,
                        enabled = uiState.canRunAudit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("run_instant_audit_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldVibrant,
                            contentColor = PureWhite,
                            disabledContainerColor = PureWhite.copy(alpha = 0.25f),
                            disabledContentColor = PureWhite.copy(alpha = 0.65f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = PureWhite,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = (uiState.analysisState as AnalysisUiState.Running).stageText,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CompareArrows,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_run_audit),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Results Output Area
                item {
                    androidx.compose.animation.AnimatedVisibility(visible = activeResultAudit != null) {
                        if (activeResultAudit != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = stringResource(R.string.results_title),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = PureWhite,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                                AuditSummaryAndExportSection(
                                    auditWithItems = activeResultAudit,
                                    onToggleResolve = onToggleLineItemResolved,
                                    onExportPdf = { onExportPdf(activeResultAudit) },
                                    onShareText = { onShareText(activeResultAudit) },
                                    onCopyText = { onCopyText(activeResultAudit) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadDropZoneCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    slot: ImageSlotState,
    previewContentDescription: String,
    testTagPrefix: String,
    onPickGallery: () -> Unit,
    onCaptureCamera: () -> Unit,
    onLoadSample: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealOcean.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TealOcean
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateInk
                    )
                    Text(
                        text = if (slot.bitmap != null) slot.label else subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (slot.bitmap != null) EmeraldVibrant else SlateMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (slot.bitmap != null) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("${testTagPrefix}_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.btn_clear),
                            tint = SlateMuted
                        )
                    }
                }
            }

            // Preview or Drop Zone Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F8F7))
                    .border(
                        width = 1.5.dp,
                        color = if (slot.bitmap != null) EmeraldVibrant else TealOcean.copy(alpha = 0.30f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    slot.isLoading -> {
                        CircularProgressIndicator(color = TealOcean, modifier = Modifier.size(32.dp))
                    }
                    slot.bitmap != null -> {
                        Image(
                            bitmap = slot.bitmap.asImageBitmap(),
                            contentDescription = previewContentDescription,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = TealOcean.copy(alpha = 0.55f),
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = "Choose Gallery, Camera, or Sample",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPickGallery,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("${testTagPrefix}_gallery_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TealOcean.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        tint = TealOcean,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.btn_gallery),
                        color = TealOcean,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onCaptureCamera,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("${testTagPrefix}_camera_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EmeraldVibrant.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = EmeraldVibrant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.btn_camera),
                        color = EmeraldVibrant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TextButton(
                    onClick = onLoadSample,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("${testTagPrefix}_sample_button")
                ) {
                    Text(
                        text = stringResource(R.string.btn_sample),
                        color = TealDeep,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
