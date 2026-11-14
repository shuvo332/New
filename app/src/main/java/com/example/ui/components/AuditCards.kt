package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AuditLineItemEntity
import com.example.data.local.AuditWithItems
import com.example.data.local.LineItemCategory
import com.example.data.local.MismatchType
import com.example.ui.theme.DividerLight
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.InfoTeal
import com.example.ui.theme.InfoTealBg
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MatchedGreen
import com.example.ui.theme.MatchedGreenBg
import com.example.ui.theme.MismatchAmber
import com.example.ui.theme.MismatchAmberBg
import com.example.ui.theme.MissingRed
import com.example.ui.theme.MissingRedBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.TealDeep
import com.example.ui.theme.TealOcean
import com.example.util.ReportExporter

@Composable
fun SampleAiNoticeBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = InfoTealBg,
        border = BorderStroke(1.dp, InfoTeal.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = InfoTeal,
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.sample_ai_notice_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = TealDeep
                )
                Text(
                    text = stringResource(R.string.sample_ai_notice_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateInk.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun AuditSummaryAndExportSection(
    auditWithItems: AuditWithItems,
    onToggleResolve: (itemId: Long, currentlyResolved: Boolean) -> Unit,
    onExportPdf: () -> Unit,
    onShareText: () -> Unit,
    onCopyText: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audit = auditWithItems.audit
    val missingItems = remember(auditWithItems.items) {
        auditWithItems.items.filter { it.category == LineItemCategory.MISSING }
    }
    val mismatchItems = remember(auditWithItems.items) {
        auditWithItems.items.filter { it.category == LineItemCategory.MISMATCH }
    }
    val matchedItems = remember(auditWithItems.items) {
        auditWithItems.items.filter { it.category == LineItemCategory.MATCHED }
    }
    var matchedExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (audit.isSampleAiPreview) {
            SampleAiNoticeBanner()
        }

        // Financial Summary & Variance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = audit.vendorName,
                            style = MaterialTheme.typography.titleLarge,
                            color = SlateInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${audit.orderRef} • ${ReportExporter.formatDate(audit.createdAtMillis)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (audit.hasIssues) MismatchAmberBg else MatchedGreenBg
                    ) {
                        Text(
                            text = "${audit.matchRatePercent}% Match",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (audit.hasIssues) MismatchAmber else MatchedGreen,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                HorizontalDivider(color = DividerLight)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FinancialMetricColumn(
                        label = stringResource(R.string.invoice_total_label),
                        amount = ReportExporter.formatMoney(audit.invoiceTotal),
                        valueColor = SlateInk
                    )
                    FinancialMetricColumn(
                        label = stringResource(R.string.system_total_label),
                        amount = ReportExporter.formatMoney(audit.systemTotal),
                        valueColor = SlateInk
                    )
                    FinancialMetricColumn(
                        label = stringResource(R.string.variance_label),
                        amount = ReportExporter.formatMoney(audit.netVariance),
                        valueColor = if (audit.netVariance != 0.0) MissingRed else MatchedGreen
                    )
                }

                // Category Count Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CountStatusPill(
                        label = "${missingItems.size} Missing",
                        textColor = MissingRed,
                        bgColor = MissingRedBg,
                        modifier = Modifier.weight(1f)
                    )
                    CountStatusPill(
                        label = "${mismatchItems.size} Mismatch",
                        textColor = MismatchAmber,
                        bgColor = MismatchAmberBg,
                        modifier = Modifier.weight(1f)
                    )
                    CountStatusPill(
                        label = "${matchedItems.size} Matched",
                        textColor = MatchedGreen,
                        bgColor = MatchedGreenBg,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Export & Share Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExportPdf,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("export_pdf_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealOcean)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.btn_export_pdf),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = onShareText,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_text_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, TealOcean.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            tint = TealOcean,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.btn_share_text),
                            color = TealOcean,
                            maxLines = 1
                        )
                    }
                    OutlinedButton(
                        onClick = onCopyText,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("copy_report_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DividerLight)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = stringResource(R.string.btn_copy_report),
                            tint = SlateMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Section 1: Missing Items (Red)
        ResultSectionHeader(
            title = stringResource(R.string.section_missing_items),
            count = missingItems.size,
            icon = Icons.Filled.Error,
            accentColor = MissingRed,
            badgeBg = MissingRedBg
        )
        if (missingItems.isEmpty()) {
            EmptyCategoryCard("No missing items detected between invoice and system screenshot.")
        } else {
            missingItems.forEach { item ->
                AuditLineItemCard(
                    item = item,
                    onToggleResolve = { onToggleResolve(item.id, item.isResolved) }
                )
            }
        }

        // Section 2: Wrong Entries / Mismatches (Amber)
        ResultSectionHeader(
            title = stringResource(R.string.section_mismatches),
            count = mismatchItems.size,
            icon = Icons.Filled.Warning,
            accentColor = MismatchAmber,
            badgeBg = MismatchAmberBg
        )
        if (mismatchItems.isEmpty()) {
            EmptyCategoryCard("No quantity, name, or price mismatches found.")
        } else {
            mismatchItems.forEach { item ->
                AuditLineItemCard(
                    item = item,
                    onToggleResolve = { onToggleResolve(item.id, item.isResolved) }
                )
            }
        }

        // Section 3: Matched Items (Green, collapsible)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { matchedExpanded = !matchedExpanded }
                .testTag("toggle_matched_section"),
            color = PureWhite.copy(alpha = 0.18f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFFA7F3D0),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${stringResource(R.string.section_matched_items)} (${matchedItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite
                    )
                }
                Icon(
                    imageVector = if (matchedExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = PureWhite
                )
            }
        }

        AnimatedVisibility(visible = matchedExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (matchedItems.isEmpty()) {
                    EmptyCategoryCard("No matched items recorded.")
                } else {
                    matchedItems.forEach { item ->
                        AuditLineItemCard(
                            item = item,
                            onToggleResolve = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialMetricColumn(
    label: String,
    amount: String,
    valueColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SlateMuted
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = valueColor
        )
    }
}

@Composable
private fun CountStatusPill(
    label: String,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ResultSectionHeader(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    badgeBg: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = badgeBg
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                color = accentColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyCategoryCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PureWhite.copy(alpha = 0.90f)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateMuted,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
fun AuditLineItemCard(
    item: AuditLineItemEntity,
    onToggleResolve: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val (accentColor, badgeBg, badgeLabel) = when (item.category) {
        LineItemCategory.MISSING -> Triple(MissingRed, MissingRedBg, "MISSING IN SS")
        LineItemCategory.MISMATCH -> {
            val tag = when (item.mismatchType) {
                MismatchType.QUANTITY -> "QTY MISMATCH"
                MismatchType.PRICE -> "PRICE MISMATCH"
                MismatchType.PRODUCT_NAME -> "WRONG ENTRY"
                MismatchType.QTY_AND_PRICE -> "QTY & PRICE"
                MismatchType.NONE -> "MISMATCH"
            }
            Triple(MismatchAmber, MismatchAmberBg, tag)
        }
        LineItemCategory.MATCHED -> Triple(MatchedGreen, MatchedGreenBg, "MATCHED")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(
            width = 1.dp,
            color = if (item.isResolved) EmeraldVibrant.copy(alpha = 0.45f) else accentColor.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = item.sku,
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                }

                if (onToggleResolve != null) {
                    TextButton(
                        onClick = onToggleResolve,
                        modifier = Modifier.testTag("resolve_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TaskAlt,
                            contentDescription = null,
                            tint = if (item.isResolved) MatchedGreen else TealOcean,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (item.isResolved) {
                                stringResource(R.string.resolved_label)
                            } else {
                                stringResource(R.string.mark_resolved)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.isResolved) MatchedGreen else TealOcean
                        )
                    }
                }
            }

            Text(
                text = item.invoiceItemName,
                style = MaterialTheme.typography.titleMedium,
                color = SlateInk
            )

            if (item.category == LineItemCategory.MISMATCH && item.systemItemName != item.invoiceItemName) {
                Text(
                    text = "System Entry: ${item.systemItemName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MismatchAmber
                )
            }

            // Side-by-side Invoice vs System comparison strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "INVOICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                    Text(
                        text = "${item.invoiceQty} × ${ReportExporter.formatMoney(item.invoiceUnitPrice)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SlateInk
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SYSTEM SS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                    Text(
                        text = if (item.category == LineItemCategory.MISSING) {
                            "0 × $0.00"
                        } else {
                            "${item.systemQty} × ${ReportExporter.formatMoney(item.systemUnitPrice)}"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (item.category == LineItemCategory.MISSING) MissingRed else SlateInk
                    )
                }
            }

            if (item.note.isNotBlank()) {
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
            }
        }
    }
}
