package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MintGlow
import com.example.ui.theme.MissingRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TealDeep
import com.example.ui.theme.TealOcean

object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val SMART_AUDIT = "smart_audit"
    const val AUDIT_HISTORY = "audit_history"
    const val MISMATCH_REPORTS = "mismatch_reports"
    const val BAG_MANAGEMENT = "bag_management"
    const val AUDIT_DETAIL = "audit_detail/{auditId}"
    const val CAMERA_SCANNER = "camera_scanner/{slot}"

    fun auditDetail(auditId: Long): String = "audit_detail/$auditId"
    fun cameraScanner(slot: String): String = "camera_scanner/$slot"
}

data class TopNavDestination(
    val route: String,
    val label: String,
    val shortLabel: String,
    val icon: ImageVector,
    val testTag: String,
    val badgeCount: Int = 0
)

@Composable
fun rememberTopDestinations(
    openDiscrepancies: Int,
    bagCount: Int
): List<TopNavDestination> {
    return listOf(
        TopNavDestination(
            route = Routes.DASHBOARD,
            label = stringResource(R.string.nav_dashboard),
            shortLabel = stringResource(R.string.nav_dashboard),
            icon = Icons.Filled.Dashboard,
            testTag = "nav_dashboard"
        ),
        TopNavDestination(
            route = Routes.SMART_AUDIT,
            label = stringResource(R.string.nav_smart_audit),
            shortLabel = stringResource(R.string.nav_smart_audit_short),
            icon = Icons.Filled.DocumentScanner,
            testTag = "nav_smart_audit"
        ),
        TopNavDestination(
            route = Routes.AUDIT_HISTORY,
            label = stringResource(R.string.nav_audit_history),
            shortLabel = "History",
            icon = Icons.Filled.History,
            testTag = "nav_audit_history"
        ),
        TopNavDestination(
            route = Routes.MISMATCH_REPORTS,
            label = stringResource(R.string.nav_mismatch_reports),
            shortLabel = "Mismatches",
            icon = Icons.Filled.ReportProblem,
            testTag = "nav_mismatch_reports",
            badgeCount = openDiscrepancies
        ),
        TopNavDestination(
            route = Routes.BAG_MANAGEMENT,
            label = stringResource(R.string.nav_bag_management),
            shortLabel = "Orders",
            icon = Icons.Filled.Inventory2,
            testTag = "nav_bag_management",
            badgeCount = bagCount
        )
    )
}

@Composable
fun AuditorTopHeader(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit = {},
    trailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = GlassWhite,
            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f))
        ) {
            IconButton(
                onClick = if (showBackButton) onBackClick else onMenuClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag(if (showBackButton) "header_back_button" else "header_menu_button")
            ) {
                Icon(
                    imageVector = if (showBackButton) {
                        Icons.AutoMirrored.Filled.ArrowBack
                    } else {
                        Icons.Filled.Menu
                    },
                    contentDescription = stringResource(
                        if (showBackButton) R.string.cd_navigate_back else R.string.cd_open_menu
                    ),
                    tint = PureWhite
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = PureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = PureWhite.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        trailingContent()
    }
}

@Composable
fun AuditorDrawerContent(
    currentRoute: String?,
    destinations: List<TopNavDestination>,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(304.dp),
        drawerContainerColor = TealDeep,
        drawerContentColor = PureWhite
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(TealDeep, TealOcean, EmeraldVibrant.copy(alpha = 0.85f)))
                )
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_auditor_avatar),
                        contentDescription = stringResource(R.string.cd_auditor_avatar),
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.auditor_name),
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                        Text(
                            text = stringResource(R.string.auditor_role),
                            style = MaterialTheme.typography.bodySmall,
                            color = PureWhite.copy(alpha = 0.85f)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PureWhite.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = EmeraldVibrant.copy(alpha = 0.30f)
                    ) {
                        Text(
                            text = stringResource(R.string.auditor_branch),
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        destinations.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationDrawerItem(
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                selected = selected,
                onClick = { onNavigate(dest.route) },
                icon = {
                    Icon(imageVector = dest.icon, contentDescription = null)
                },
                badge = if (dest.badgeCount > 0) {
                    {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (dest.route == Routes.MISMATCH_REPORTS) MissingRed else EmeraldVibrant
                        ) {
                            Text(
                                text = dest.badgeCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else null,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 3.dp)
                    .testTag("drawer_${dest.testTag}"),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = PureWhite.copy(alpha = 0.20f),
                    selectedIconColor = MintGlow,
                    selectedTextColor = PureWhite,
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = PureWhite.copy(alpha = 0.80f),
                    unselectedTextColor = PureWhite.copy(alpha = 0.88f)
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(color = PureWhite.copy(alpha = 0.16f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Assessment,
                contentDescription = null,
                tint = MintGlow,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.app_full_title),
                style = MaterialTheme.typography.bodySmall,
                color = PureWhite.copy(alpha = 0.80f)
            )
        }
    }
}

@Composable
fun AuditorBottomNavigationBar(
    currentRoute: String?,
    destinations: List<TopNavDestination>,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = TealDeep.copy(alpha = 0.96f),
        contentColor = PureWhite,
        tonalElevation = 8.dp
    ) {
        destinations.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest.route) },
                modifier = Modifier.testTag("bottom_${dest.testTag}"),
                icon = {
                    if (dest.badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = if (dest.route == Routes.MISMATCH_REPORTS) MissingRed else EmeraldVibrant,
                                    contentColor = PureWhite
                                ) {
                                    Text(dest.badgeCount.toString())
                                }
                            }
                        ) {
                            Icon(imageVector = dest.icon, contentDescription = dest.label)
                        }
                    } else {
                        Icon(imageVector = dest.icon, contentDescription = dest.label)
                    }
                },
                label = {
                    Text(
                        text = dest.shortLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealDeep,
                    selectedTextColor = MintGlow,
                    indicatorColor = MintGlow,
                    unselectedIconColor = PureWhite.copy(alpha = 0.72f),
                    unselectedTextColor = PureWhite.copy(alpha = 0.72f)
                )
            )
        }
    }
}

@Composable
fun AuditorSidebarRail(
    currentRoute: String?,
    destinations: List<TopNavDestination>,
    onNavigate: (String) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = TealDeep.copy(alpha = 0.96f),
        contentColor = PureWhite
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.img_auditor_avatar),
            contentDescription = stringResource(R.string.cd_auditor_avatar),
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(20.dp))
        destinations.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(dest.route) },
                modifier = Modifier.testTag("rail_${dest.testTag}"),
                icon = {
                    if (dest.badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = if (dest.route == Routes.MISMATCH_REPORTS) MissingRed else EmeraldVibrant,
                                    contentColor = PureWhite
                                ) {
                                    Text(dest.badgeCount.toString())
                                }
                            }
                        ) {
                            Icon(imageVector = dest.icon, contentDescription = dest.label)
                        }
                    } else {
                        Icon(imageVector = dest.icon, contentDescription = dest.label)
                    }
                },
                label = {
                    Text(
                        text = dest.shortLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = TealDeep,
                    selectedTextColor = MintGlow,
                    indicatorColor = MintGlow,
                    unselectedIconColor = PureWhite.copy(alpha = 0.75f),
                    unselectedTextColor = PureWhite.copy(alpha = 0.75f)
                )
            )
        }
    }
}
