package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AuditWithItems
import com.example.ui.components.AuditSummaryAndExportSection
import com.example.ui.components.AuditorTopHeader
import com.example.ui.components.GlassCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.util.ImageLoaderUtil

@Composable
fun AuditDetailScreen(
    auditWithItems: AuditWithItems?,
    onBack: () -> Unit,
    onToggleLineItemResolved: (Long, Boolean) -> Unit,
    onExportPdf: (AuditWithItems) -> Unit,
    onShareText: (AuditWithItems) -> Unit,
    onCopyText: (AuditWithItems) -> Unit
) {
    BackHandler { onBack() }

    val ssBitmap by produceState<Bitmap?>(initialValue = null, auditWithItems?.audit?.screenshotUri) {
        value = ImageLoaderUtil.loadBitmapFromLocalPath(auditWithItems?.audit?.screenshotUri)
    }
    val invBitmap by produceState<Bitmap?>(initialValue = null, auditWithItems?.audit?.invoiceUri) {
        value = ImageLoaderUtil.loadBitmapFromLocalPath(auditWithItems?.audit?.invoiceUri)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AuditorTopHeader(
            title = auditWithItems?.audit?.orderRef ?: "Audit Report",
            subtitle = auditWithItems?.audit?.vendorName,
            showBackButton = true,
            onMenuClick = onBack,
            onBackClick = onBack
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (auditWithItems == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = PureWhite)
                            Text(
                                text = "Loading audit report…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhite
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (ssBitmap != null || invBitmap != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.source_docs_offline),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = SlateInk
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        ssBitmap?.let { bmp ->
                                            OfflineDocThumbnail(
                                                bitmap = bmp,
                                                label = auditWithItems.audit.screenshotLabel,
                                                contentDescription = stringResource(R.string.cd_system_screenshot_preview),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        invBitmap?.let { bmp ->
                                            OfflineDocThumbnail(
                                                bitmap = bmp,
                                                label = auditWithItems.audit.invoiceLabel,
                                                contentDescription = stringResource(R.string.cd_invoice_image_preview),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        AuditSummaryAndExportSection(
                            auditWithItems = auditWithItems,
                            onToggleResolve = onToggleLineItemResolved,
                            onExportPdf = { onExportPdf(auditWithItems) },
                            onShareText = { onShareText(auditWithItems) },
                            onCopyText = { onCopyText(auditWithItems) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineDocThumbnail(
    bitmap: Bitmap,
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SlateMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
