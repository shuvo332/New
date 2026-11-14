package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.BagStatus
import com.example.data.local.OrderBagEntity
import com.example.ui.components.AuditorTopHeader
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.InfoTeal
import com.example.ui.theme.InfoTealBg
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

private enum class BagFilter(val label: String, val status: BagStatus?) {
    ALL("All", null),
    PENDING("Pending Audit", BagStatus.PENDING_AUDIT),
    DISCREPANCY("Discrepancy", BagStatus.DISCREPANCY_FOUND),
    VERIFIED("Verified", BagStatus.VERIFIED),
    DISPATCHED("Dispatched", BagStatus.DISPATCHED)
}

@Composable
fun OrderBagScreen(
    bags: List<OrderBagEntity>,
    onOpenMenu: () -> Unit,
    onAddBag: (code: String, vendor: String, count: Int, total: Double, notes: String) -> Unit,
    onUpdateStatus: (Long, BagStatus) -> Unit,
    onDeleteBag: (Long) -> Unit,
    onAuditBag: (OrderBagEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(BagFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredBags = remember(bags, selectedFilter) {
        if (selectedFilter.status == null) bags
        else bags.filter { it.status == selectedFilter.status }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MintGlow,
                contentColor = TealDeep,
                modifier = Modifier.testTag("add_bag_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_bag_order)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            AuditorTopHeader(
                title = stringResource(R.string.nav_bag_management),
                subtitle = "${bags.size} tracked bags & orders",
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(BagFilter.entries) { filter ->
                                val selected = selectedFilter == filter
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter.label) },
                                    modifier = Modifier.testTag("bag_filter_${filter.name.lowercase()}"),
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

                    if (filteredBags.isEmpty()) {
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
                                        imageVector = Icons.Filled.Inventory2,
                                        contentDescription = null,
                                        tint = MintGlow,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.empty_bags_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = stringResource(R.string.empty_bags_body),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PureWhite.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredBags, key = { it.id }) { bag ->
                            OrderBagCard(
                                bag = bag,
                                onUpdateStatus = { status -> onUpdateStatus(bag.id, status) },
                                onDelete = { onDeleteBag(bag.id) },
                                onAuditBag = { onAuditBag(bag) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBagDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { code, vendor, count, total, notes ->
                onAddBag(code, vendor, count, total, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun OrderBagCard(
    bag: OrderBagEntity,
    onUpdateStatus: (BagStatus) -> Unit,
    onDelete: () -> Unit,
    onAuditBag: () -> Unit
) {
    val (statusLabel, statusColor, statusBg) = when (bag.status) {
        BagStatus.PENDING_AUDIT -> Triple("Pending Audit", MismatchAmber, MismatchAmberBg)
        BagStatus.DISCREPANCY_FOUND -> Triple("Discrepancy", MissingRed, MissingRedBg)
        BagStatus.VERIFIED -> Triple("Verified", MatchedGreen, MatchedGreenBg)
        BagStatus.DISPATCHED -> Triple("Dispatched", InfoTeal, InfoTealBg)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bag_card_${bag.id}"),
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
                        .background(statusBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (bag.status) {
                            BagStatus.DISCREPANCY_FOUND -> Icons.Filled.ReportProblem
                            BagStatus.VERIFIED -> Icons.Filled.CheckCircle
                            BagStatus.DISPATCHED -> Icons.Filled.LocalShipping
                            BagStatus.PENDING_AUDIT -> Icons.Filled.Inventory2
                        },
                        contentDescription = null,
                        tint = statusColor
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = bag.bagCode,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = SlateInk
                        )
                        Surface(shape = RoundedCornerShape(50), color = statusBg) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = bag.vendorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_bag_${bag.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete bag",
                        tint = SlateMuted
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${bag.itemCount} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateInk
                )
                Text(
                    text = ReportExporter.formatMoney(bag.declaredTotal),
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = TealDeep
                )
            }

            if (bag.notes.isNotBlank()) {
                Text(
                    text = bag.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAuditBag,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("audit_bag_${bag.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealOcean)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DocumentScanner,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan & Audit", maxLines = 1)
                }

                val nextStatus = when (bag.status) {
                    BagStatus.PENDING_AUDIT, BagStatus.DISCREPANCY_FOUND -> BagStatus.VERIFIED to "Mark Verified"
                    BagStatus.VERIFIED -> BagStatus.DISPATCHED to "Dispatch"
                    BagStatus.DISPATCHED -> BagStatus.PENDING_AUDIT to "Re-open"
                }

                OutlinedButton(
                    onClick = { onUpdateStatus(nextStatus.first) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("advance_bag_${bag.id}"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EmeraldVibrant.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = nextStatus.second,
                        color = EmeraldVibrant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun AddBagDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, vendor: String, count: Int, total: Double, notes: String) -> Unit
) {
    var code by remember { mutableStateOf("BAG-2026-") }
    var vendor by remember { mutableStateOf("") }
    var itemCount by remember { mutableStateOf("5") }
    var total by remember { mutableStateOf("250.00") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_bag_order)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Bag / Order Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = vendor,
                    onValueChange = { vendor = it },
                    label = { Text("Vendor / Supplier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = itemCount,
                        onValueChange = { itemCount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Items") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = total,
                        onValueChange = { total = it },
                        label = { Text("Total ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.2f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Audit / Dispatch Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedCount = itemCount.toIntOrNull() ?: 1
                    val parsedTotal = total.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        code.ifBlank { "BAG-${System.currentTimeMillis() % 10000}" },
                        vendor.ifBlank { "General Supplier" },
                        parsedCount,
                        parsedTotal,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealOcean)
            ) {
                Text("Save Bag")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
