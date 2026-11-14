package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AuditWithItems
import com.example.ui.components.AuditorTopHeader
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MatchedGreen
import com.example.ui.theme.MatchedGreenBg
import com.example.ui.theme.MintGlow
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
import java.util.Calendar
import kotlin.math.abs

private enum class HistoryStatusFilter(val label: String) {
    ALL("All Status"),
    ISSUES("Has Issues"),
    MISSING("Missing Items"),
    MISMATCH("Mismatches"),
    CLEAN("Clean Match")
}

private enum class HistoryDateFilter(val label: String) {
    ANY("Any Date"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days")
}

private enum class HistorySort(val label: String) {
    NEWEST("Newest"),
    HIGHEST_VARIANCE("Top Variance")
}

@Composable
fun AuditHistoryScreen(
    audits: List<AuditWithItems>,
    onOpenMenu: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    onDeleteAudit: (Long) -> Unit,
    onExportPdf: (AuditWithItems) -> Unit,
    onStartNewAudit: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(HistoryStatusFilter.ALL) }
    var selectedDate by remember { mutableStateOf(HistoryDateFilter.ANY) }
    var selectedSort by remember { mutableStateOf(HistorySort.NEWEST) }
    var auditToDelete by remember { mutableStateOf<AuditWithItems?>(null) }

    val totalValueAudited = remember(audits) { audits.sumOf { it.audit.invoiceTotal } }
    val issueAuditCount = remember(audits) { audits.count { it.audit.hasIssues } }
    val hasActiveFilters = searchQuery.isNotBlank() ||
        selectedStatus != HistoryStatusFilter.ALL ||
        selectedDate != HistoryDateFilter.ANY

    val filteredAudits = remember(audits, searchQuery, selectedStatus, selectedDate, selectedSort) {
        val now = System.currentTimeMillis()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val sevenDaysAgo = now - 7L * 24L * 3_600_000L
        val thirtyDaysAgo = now - 30L * 24L * 3_600_000L

        val q = searchQuery.trim()
        val filtered = audits.filter { entry ->
            val audit = entry.audit
            val formattedDate = ReportExporter.formatDate(audit.createdAtMillis)
            val matchesKeyword = q.isEmpty() ||
                audit.orderRef.contains(q, ignoreCase = true) ||
                audit.vendorName.contains(q, ignoreCase = true) ||
                audit.invoiceLabel.contains(q, ignoreCase = true) ||
                audit.screenshotLabel.contains(q, ignoreCase = true) ||
                audit.summaryNote.contains(q, ignoreCase = true) ||
                formattedDate.contains(q, ignoreCase = true) ||
                entry.items.any { line ->
                    line.sku.contains(q, ignoreCase = true) ||
                        line.invoiceItemName.contains(q, ignoreCase = true) ||
                        line.systemItemName.contains(q, ignoreCase = true)
                }

            val matchesStatus = when (selectedStatus) {
                HistoryStatusFilter.ALL -> true
                HistoryStatusFilter.ISSUES -> audit.hasIssues
                HistoryStatusFilter.MISSING -> audit.missingCount > 0
                HistoryStatusFilter.MISMATCH -> audit.mismatchCount > 0
                HistoryStatusFilter.CLEAN -> !audit.hasIssues
            }

            val matchesDate = when (selectedDate) {
                HistoryDateFilter.ANY -> true
                HistoryDateFilter.TODAY -> audit.createdAtMillis >= startOfToday
                HistoryDateFilter.LAST_7_DAYS -> audit.createdAtMillis >= sevenDaysAgo
                HistoryDateFilter.LAST_30_DAYS -> audit.createdAtMillis >= thirtyDaysAgo
            }

            matchesKeyword && matchesStatus && matchesDate
        }

        when (selectedSort) {
            HistorySort.NEWEST -> filtered.sortedByDescending { it.audit.createdAtMillis }
            HistorySort.HIGHEST_VARIANCE -> filtered.sortedByDescending { abs(it.audit.netVariance) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AuditorTopHeader(
            title = stringResource(R.string.nav_audit_history),
            subtitle = "${filteredAudits.size} of ${audits.size} audits shown",
            onMenuClick = onOpenMenu,
            trailingContent = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PureWhite.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, MintGlow.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OfflinePin,
                            contentDescription = null,
                            tint = MintGlow,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Offline DB",
                            style = MaterialTheme.typography.labelSmall,
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
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Offline Room Storage Summary Strip
                item {
                    GlassCard(containerColor = GlassWhiteStrong) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(R.string.offline_room_badge).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MintGlow
                                )
                                Text(
                                    text = "${ReportExporter.formatMoney(totalValueAudited)} audited",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PureWhite
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = TealDeep.copy(alpha = 0.50f)
                            ) {
                                Text(
                                    text = "$issueAuditCount flagged",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (issueAuditCount > 0) Color(0xFFFDE68A) else MintGlow,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Keyword Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.search_audits_hint)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = TealDeep)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.testTag("history_clear_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = stringResource(R.string.cd_clear_search),
                                        tint = SlateMuted
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite.copy(alpha = 0.94f),
                            focusedTextColor = SlateInk,
                            unfocusedTextColor = SlateInk,
                            focusedBorderColor = MintGlow,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                // Status Filter Chips + Sort Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(HistoryStatusFilter.entries) { filter ->
                                val selected = selectedStatus == filter
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedStatus = filter },
                                    label = { Text(filter.label) },
                                    modifier = Modifier.testTag("history_filter_${filter.name.lowercase()}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MintGlow,
                                        selectedLabelColor = TealDeep,
                                        containerColor = PureWhite.copy(alpha = 0.16f),
                                        labelColor = PureWhite
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = PureWhite.copy(alpha = 0.35f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            onClick = {
                                selectedSort = if (selectedSort == HistorySort.NEWEST) {
                                    HistorySort.HIGHEST_VARIANCE
                                } else {
                                    HistorySort.NEWEST
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("history_sort_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Sort,
                                    contentDescription = null,
                                    tint = MintGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = selectedSort.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }

                // Date Filter Chips Row
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(HistoryDateFilter.entries) { dateFilter ->
                            val selected = selectedDate == dateFilter
                            FilterChip(
                                selected = selected,
                                onClick = { selectedDate = dateFilter },
                                leadingIcon = if (dateFilter == HistoryDateFilter.ANY) null else {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                },
                                label = { Text(dateFilter.label) },
                                modifier = Modifier.testTag("history_date_${dateFilter.name.lowercase()}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PureWhite,
                                    selectedLabelColor = TealDeep,
                                    selectedLeadingIconColor = TealDeep,
                                    containerColor = PureWhite.copy(alpha = 0.12f),
                                    labelColor = PureWhite.copy(alpha = 0.90f),
                                    iconColor = MintGlow
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = PureWhite.copy(alpha = 0.28f)
                                )
                            )
                        }
                    }
                }

                if (filteredAudits.isEmpty()) {
                    item {
                        GlassCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasActiveFilters) Icons.Filled.FilterAltOff else Icons.Filled.History,
                                    contentDescription = null,
                                    tint = MintGlow,
                                    modifier = Modifier.size(42.dp)
                                )
                                Text(
                                    text = if (hasActiveFilters) "No Matching Audits" else stringResource(R.string.empty_audits_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite
                                )
                                Text(
                                    text = if (hasActiveFilters) {
                                        "Try adjusting your search keyword, status chip, or date filter."
                                    } else {
                                        stringResource(R.string.empty_audits_body)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PureWhite.copy(alpha = 0.85f)
                                )
                                if (hasActiveFilters) {
                                    Button(
                                        onClick = {
                                            searchQuery = ""
                                            selectedStatus = HistoryStatusFilter.ALL
                                            selectedDate = HistoryDateFilter.ANY
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MintGlow,
                                            contentColor = TealDeep
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("history_reset_filters_button")
                                    ) {
                                        Text(stringResource(R.string.btn_clear_filters))
                                    }
                                } else {
                                    Button(
                                        onClick = onStartNewAudit,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MintGlow,
                                            contentColor = TealDeep
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("history_empty_new_audit_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.btn_new_audit))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filteredAudits, key = { it.audit.id }) { entry ->
                        AuditHistoryRowCard(
                            entry = entry,
                            onClick = { onOpenDetail(entry.audit.id) },
                            onExportPdf = { onExportPdf(entry) },
                            onDeleteClick = { auditToDelete = entry }
                        )
                    }
                }
            }
        }
    }

    auditToDelete?.let { pendingDelete ->
        AlertDialog(
            onDismissRequest = { auditToDelete = null },
            title = { Text("Delete Audit Record?") },
            text = {
                Text("Remove ${pendingDelete.audit.orderRef} (${pendingDelete.audit.vendorName}) from offline Room history?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAudit(pendingDelete.audit.id)
                        auditToDelete = null
                    }
                ) {
                    Text("Delete", color = MissingRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { auditToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AuditHistoryRowCard(
    entry: AuditWithItems,
    onClick: () -> Unit,
    onExportPdf: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val audit = entry.audit
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("history_audit_card_${audit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (audit.hasIssues) MismatchAmberBg else MatchedGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (audit.hasIssues) Icons.Filled.ReportProblem else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (audit.hasIssues) MismatchAmber else MatchedGreen
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = audit.vendorName,
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${audit.orderRef} • ${ReportExporter.formatDate(audit.createdAtMillis)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                    Text(
                        text = "${audit.screenshotLabel} vs ${audit.invoiceLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealOcean,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onExportPdf,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("export_history_pdf_${audit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = stringResource(R.string.btn_export_pdf),
                        tint = TealOcean
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_audit_${audit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_delete_audit),
                        tint = SlateMuted
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip("${audit.missingCount} Missing", MissingRed, MissingRedBg)
                    StatusChip("${audit.mismatchCount} Mismatch", MismatchAmber, MismatchAmberBg)
                    StatusChip("${audit.matchedCount} OK", MatchedGreen, MatchedGreenBg)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ReportExporter.formatMoney(audit.invoiceTotal),
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = SlateInk
                    )
                    if (audit.netVariance != 0.0) {
                        Text(
                            text = "Δ ${ReportExporter.formatMoney(audit.netVariance)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MissingRed
                        )
                    } else {
                        Text(
                            text = "${audit.matchRatePercent}% match",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldVibrant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, textColor: Color, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(0.5.dp, textColor.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
