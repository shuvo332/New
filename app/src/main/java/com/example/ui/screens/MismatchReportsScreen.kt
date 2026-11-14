package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DiscrepancyWithAudit
import com.example.data.local.LineItemCategory
import com.example.ui.components.AuditLineItemCard
import com.example.ui.components.AuditorTopHeader
import com.example.ui.components.GlassCard
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MintGlow
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TealDeep
import com.example.util.ReportExporter
import kotlin.math.abs

private enum class DiscrepancyFilter(val label: String) {
    OPEN("Open Issues"),
    MISSING("Missing"),
    MISMATCH("Wrong / Price / Qty"),
    RESOLVED("Resolved")
}

@Composable
fun MismatchReportsScreen(
    discrepancies: List<DiscrepancyWithAudit>,
    onOpenMenu: () -> Unit,
    onToggleResolve: (Long, Boolean) -> Unit,
    onOpenAuditDetail: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(DiscrepancyFilter.OPEN) }

    val openCount = remember(discrepancies) { discrepancies.count { !it.item.isResolved } }
    val openValue = remember(discrepancies) {
        discrepancies.filter { !it.item.isResolved }.sumOf { abs(it.item.lineVariance) }
    }

    val filtered = remember(discrepancies, selectedFilter) {
        discrepancies.filter { entry ->
            when (selectedFilter) {
                DiscrepancyFilter.OPEN -> !entry.item.isResolved
                DiscrepancyFilter.MISSING -> entry.item.category == LineItemCategory.MISSING && !entry.item.isResolved
                DiscrepancyFilter.MISMATCH -> entry.item.category == LineItemCategory.MISMATCH && !entry.item.isResolved
                DiscrepancyFilter.RESOLVED -> entry.item.isResolved
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AuditorTopHeader(
            title = stringResource(R.string.nav_mismatch_reports),
            subtitle = "$openCount open discrepancies across audits",
            onMenuClick = onOpenMenu
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    GlassCard(containerColor = GlassWhiteStrong) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "OPEN VARIANCE EXPOSURE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PureWhite.copy(alpha = 0.80f)
                                )
                                Text(
                                    text = ReportExporter.formatMoney(openValue),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    color = PureWhite
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = TealDeep.copy(alpha = 0.55f)
                            ) {
                                Text(
                                    text = "$openCount Open",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MintGlow,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DiscrepancyFilter.entries) { filter ->
                            val selected = selectedFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter.label) },
                                modifier = Modifier.testTag("mismatch_filter_${filter.name.lowercase()}"),
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
                }

                if (filtered.isEmpty()) {
                    item {
                        GlassCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = null,
                                    tint = MintGlow,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = stringResource(R.string.empty_mismatches_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite
                                )
                                Text(
                                    text = stringResource(R.string.empty_mismatches_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PureWhite.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.item.id }) { entry ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            entry.audit?.let { parentAudit ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenAuditDetail(parentAudit.id) }
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${parentAudit.orderRef} • ${parentAudit.vendorName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MintGlow
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Open Audit",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PureWhite
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            AuditLineItemCard(
                                item = entry.item,
                                onToggleResolve = {
                                    onToggleResolve(entry.item.id, entry.item.isResolved)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
