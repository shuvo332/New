package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AuditWithItems
import com.example.ui.components.AuditorTopHeader
import com.example.ui.components.GlassCard
import com.example.ui.components.Routes
import com.example.ui.theme.CyanVibrant
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
import com.example.ui.viewmodel.DashboardStats
import com.example.util.ReportExporter

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    recentAudits: List<AuditWithItems>,
    bagCount: Int,
    onOpenMenu: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenAuditDetail: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AuditorTopHeader(
            title = stringResource(R.string.app_full_title),
            subtitle = stringResource(R.string.auditor_branch),
            onMenuClick = onOpenMenu,
            trailingContent = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PureWhite.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.40f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = MintGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.auditor_id_badge),
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Profile & Live KPI Glass Card
                item {
                    GlassCard(
                        containerColor = GlassWhiteStrong
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_auditor_avatar),
                                        contentDescription = stringResource(R.string.cd_auditor_avatar),
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(MintGlow)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.auditor_name),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = stringResource(R.string.auditor_role),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PureWhite.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KpiMiniTile(
                                    label = stringResource(R.string.kpi_audits_run),
                                    value = stats.totalAudits.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                KpiMiniTile(
                                    label = stringResource(R.string.kpi_discrepancies),
                                    value = stats.openDiscrepancies.toString(),
                                    highlightColor = if (stats.openDiscrepancies > 0) Color(0xFFFDE68A) else MintGlow,
                                    modifier = Modifier.weight(1f)
                                )
                                KpiMiniTile(
                                    label = stringResource(R.string.kpi_match_rate),
                                    value = "${stats.averageMatchRate}%",
                                    highlightColor = MintGlow,
                                    modifier = Modifier.weight(1f)
                                )
                                KpiMiniTile(
                                    label = stringResource(R.string.kpi_variance_caught),
                                    value = ReportExporter.formatMoney(stats.totalVarianceCaught),
                                    modifier = Modifier.weight(1.25f)
                                )
                            }
                        }
                    }
                }

                // Quick Navigation Header
                item {
                    Text(
                        text = stringResource(R.string.quick_navigation_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite
                    )
                }

                // Primary Hero Feature Card: Upload SS & Invoice
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onNavigate(Routes.SMART_AUDIT) }
                            .testTag("dashboard_upload_audit_card"),
                        shape = RoundedCornerShape(24.dp),
                        color = PureWhite,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(PureWhite, Color(0xFFE6FFFA))
                                    )
                                )
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(listOf(CyanVibrant, EmeraldVibrant))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.nav_smart_audit),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TealDeep
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.primary_cta_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SmallFeatureTag(
                                        icon = Icons.Filled.PhotoLibrary,
                                        label = "System SS"
                                    )
                                    SmallFeatureTag(
                                        icon = Icons.Filled.CameraAlt,
                                        label = "Invoice Scan"
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TealOcean),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Secondary Quick Navigation Grid (3 tiles)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickMenuTile(
                            title = stringResource(R.string.nav_audit_history),
                            countText = "${stats.totalAudits} logs",
                            icon = Icons.Filled.History,
                            accentColor = CyanVibrant,
                            testTag = "dashboard_history_tile",
                            onClick = { onNavigate(Routes.AUDIT_HISTORY) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickMenuTile(
                            title = stringResource(R.string.nav_mismatch_reports),
                            countText = "${stats.openDiscrepancies} open",
                            icon = Icons.Filled.ReportProblem,
                            accentColor = if (stats.openDiscrepancies > 0) MissingRed else EmeraldVibrant,
                            testTag = "dashboard_mismatches_tile",
                            onClick = { onNavigate(Routes.MISMATCH_REPORTS) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickMenuTile(
                            title = "Bag / Orders",
                            countText = "$bagCount active",
                            icon = Icons.Filled.Inventory2,
                            accentColor = EmeraldVibrant,
                            testTag = "dashboard_bags_tile",
                            onClick = { onNavigate(Routes.BAG_MANAGEMENT) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Recent Audits Preview
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recent_audits_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                        TextButton(
                            onClick = { onNavigate(Routes.AUDIT_HISTORY) },
                            modifier = Modifier.testTag("dashboard_view_all_audits")
                        ) {
                            Text(
                                text = stringResource(R.string.view_all),
                                color = MintGlow,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                if (recentAudits.isEmpty()) {
                    item {
                        GlassCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.empty_audits_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite
                                )
                                Text(
                                    text = stringResource(R.string.empty_audits_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PureWhite.copy(alpha = 0.80f)
                                )
                            }
                        }
                    }
                } else {
                    items(recentAudits.take(3), key = { it.audit.id }) { item ->
                        RecentAuditPreviewCard(
                            auditWithItems = item,
                            onClick = { onOpenAuditDetail(item.audit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMiniTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = PureWhite
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = TealDeep.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = highlightColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = PureWhite.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SmallFeatureTag(
    icon: ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = TealOcean.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TealOcean,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TealDeep
            )
        }
    }
}

@Composable
private fun QuickMenuTile(
    title: String,
    countText: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        containerColor = PureWhite.copy(alpha = 0.17f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PureWhite.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = PureWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = countText,
                style = MaterialTheme.typography.labelSmall,
                color = MintGlow
            )
        }
    }
}

@Composable
private fun RecentAuditPreviewCard(
    auditWithItems: AuditWithItems,
    onClick: () -> Unit
) {
    val audit = auditWithItems.audit
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("recent_audit_${audit.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (audit.missingCount > 0) {
                        MiniBadge("${audit.missingCount} missing", MissingRed, MissingRedBg)
                    }
                    if (audit.mismatchCount > 0) {
                        MiniBadge("${audit.mismatchCount} mismatch", MismatchAmber, MismatchAmberBg)
                    }
                    if (!audit.hasIssues) {
                        MiniBadge("100% Matched", MatchedGreen, MatchedGreenBg)
                    }
                }
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
                }
            }
        }
    }
}

@Composable
private fun MiniBadge(text: String, textColor: Color, bgColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = bgColor) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
