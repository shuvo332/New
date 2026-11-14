package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AuditWithItems
import com.example.ui.components.AuditorBottomNavigationBar
import com.example.ui.components.AuditorDrawerContent
import com.example.ui.components.AuditorSidebarRail
import com.example.ui.components.FluidTealGreenBackdrop
import com.example.ui.components.Routes
import com.example.ui.components.rememberTopDestinations
import com.example.ui.screens.AuditDetailScreen
import com.example.ui.screens.AuditHistoryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentScannerScreen
import com.example.ui.screens.MismatchReportsScreen
import com.example.ui.screens.OrderBagScreen
import com.example.ui.screens.SmartAuditScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.InvoiceAuditorTheme
import com.example.ui.viewmodel.AuditorViewModel
import com.example.util.ReportExporter
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemStatusBar()
        setContent {
            InvoiceAuditorTheme {
                InvoiceAuditorApp()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemStatusBar()
        }
    }

    private fun hideSystemStatusBar() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }
}

@Composable
fun InvoiceAuditorApp(
    viewModel: AuditorViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val audits by viewModel.audits.collectAsStateWithLifecycle()
    val discrepancies by viewModel.discrepancies.collectAsStateWithLifecycle()
    val bags by viewModel.bags.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val smartAuditState by viewModel.smartAuditState.collectAsStateWithLifecycle()
    val activeResultAudit by viewModel.activeResultAudit.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showChrome = currentRoute != null &&
        currentRoute != Routes.SPLASH &&
        !currentRoute.startsWith("audit_detail") &&
        !currentRoute.startsWith("camera_scanner")

    val destinations = rememberTopDestinations(
        openDiscrepancies = stats.openDiscrepancies,
        bagCount = bags.size
    )

    val navigateToTopRoute: (String) -> Unit = { route ->
        scope.launch {
            if (drawerState.isOpen) drawerState.close()
        }
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val onExportPdf: (AuditWithItems) -> Unit = { auditWithItems ->
        scope.launch {
            val result = ReportExporter.exportAndSharePdf(context, auditWithItems)
            if (result.isFailure) {
                snackbarHostState.showSnackbar("Unable to export PDF report.")
            }
        }
    }

    val onShareText: (AuditWithItems) -> Unit = { auditWithItems ->
        ReportExporter.sharePlainTextReport(context, auditWithItems)
    }

    val onCopyText: (AuditWithItems) -> Unit = { auditWithItems ->
        ReportExporter.copyReportToClipboard(context, auditWithItems)
        scope.launch {
            snackbarHostState.showSnackbar("Discrepancy report copied to clipboard")
        }
    }

    if (drawerState.isOpen) {
        BackHandler {
            scope.launch { drawerState.close() }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showChrome,
        drawerContent = {
            AuditorDrawerContent(
                currentRoute = currentRoute,
                destinations = destinations,
                onNavigate = navigateToTopRoute
            )
        }
    ) {
        FluidTealGreenBackdrop {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useSidebarRail = maxWidth >= 720.dp

                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        if (showChrome && !useSidebarRail) {
                            AuditorBottomNavigationBar(
                                currentRoute = currentRoute,
                                destinations = destinations,
                                onNavigate = navigateToTopRoute
                            )
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (showChrome && useSidebarRail) {
                            AuditorSidebarRail(
                                currentRoute = currentRoute,
                                destinations = destinations,
                                onNavigate = navigateToTopRoute
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            NavHost(
                                navController = navController,
                                startDestination = Routes.SPLASH
                            ) {
                                composable(Routes.SPLASH) {
                                    SplashScreen(
                                        onFinished = {
                                            navController.navigate(Routes.DASHBOARD) {
                                                popUpTo(Routes.SPLASH) { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable(Routes.DASHBOARD) {
                                    DashboardScreen(
                                        stats = stats,
                                        recentAudits = audits,
                                        bagCount = bags.size,
                                        onOpenMenu = { scope.launch { drawerState.open() } },
                                        onNavigate = navigateToTopRoute,
                                        onOpenAuditDetail = { auditId ->
                                            navController.navigate(Routes.auditDetail(auditId))
                                        }
                                    )
                                }

                                composable(Routes.SMART_AUDIT) {
                                    SmartAuditScreen(
                                        uiState = smartAuditState,
                                        activeResultAudit = activeResultAudit,
                                        onOpenMenu = { scope.launch { drawerState.open() } },
                                        onOrderRefChange = viewModel::updateOrderRef,
                                        onVendorNameChange = viewModel::updateVendorName,
                                        onScreenshotPicked = { uri -> viewModel.setScreenshotFromUri(uri) },
                                        onInvoicePicked = { uri -> viewModel.setInvoiceFromUri(uri) },
                                        onOpenCameraScanner = { slot ->
                                            navController.navigate(Routes.cameraScanner(slot))
                                        },
                                        onLoadSampleScreenshot = viewModel::loadSampleScreenshot,
                                        onLoadSampleInvoice = viewModel::loadSampleInvoice,
                                        onLoadDemoPair = viewModel::loadDemoPair,
                                        onClearScreenshot = viewModel::clearScreenshot,
                                        onClearInvoice = viewModel::clearInvoice,
                                        onRunInstantAudit = viewModel::runInstantAudit,
                                        onToggleLineItemResolved = viewModel::toggleLineItemResolved,
                                        onExportPdf = onExportPdf,
                                        onShareText = onShareText,
                                        onCopyText = onCopyText
                                    )
                                }

                                composable(Routes.AUDIT_HISTORY) {
                                    AuditHistoryScreen(
                                        audits = audits,
                                        onOpenMenu = { scope.launch { drawerState.open() } },
                                        onOpenDetail = { auditId ->
                                            navController.navigate(Routes.auditDetail(auditId))
                                        },
                                        onDeleteAudit = viewModel::deleteAudit,
                                        onExportPdf = onExportPdf,
                                        onStartNewAudit = { navigateToTopRoute(Routes.SMART_AUDIT) }
                                    )
                                }

                                composable(Routes.MISMATCH_REPORTS) {
                                    MismatchReportsScreen(
                                        discrepancies = discrepancies,
                                        onOpenMenu = { scope.launch { drawerState.open() } },
                                        onToggleResolve = viewModel::toggleLineItemResolved,
                                        onOpenAuditDetail = { auditId ->
                                            navController.navigate(Routes.auditDetail(auditId))
                                        }
                                    )
                                }

                                composable(Routes.BAG_MANAGEMENT) {
                                    OrderBagScreen(
                                        bags = bags,
                                        onOpenMenu = { scope.launch { drawerState.open() } },
                                        onAddBag = viewModel::addBag,
                                        onUpdateStatus = viewModel::updateBagStatus,
                                        onDeleteBag = viewModel::deleteBag,
                                        onAuditBag = { bag ->
                                            viewModel.prefillFromBag(bag)
                                            navigateToTopRoute(Routes.SMART_AUDIT)
                                        }
                                    )
                                }

                                composable(
                                    route = Routes.AUDIT_DETAIL,
                                    arguments = listOf(navArgument("auditId") { type = NavType.LongType })
                                ) { entry ->
                                    val auditId = entry.arguments?.getLong("auditId") ?: -1L
                                    val auditWithItems by viewModel.observeAudit(auditId)
                                        .collectAsStateWithLifecycle(initialValue = null)

                                    AuditDetailScreen(
                                        auditWithItems = auditWithItems,
                                        onBack = { navController.popBackStack() },
                                        onToggleLineItemResolved = viewModel::toggleLineItemResolved,
                                        onExportPdf = onExportPdf,
                                        onShareText = onShareText,
                                        onCopyText = onCopyText
                                    )
                                }

                                composable(
                                    route = Routes.CAMERA_SCANNER,
                                    arguments = listOf(navArgument("slot") { type = NavType.StringType })
                                ) { entry ->
                                    val slot = entry.arguments?.getString("slot") ?: "invoice"
                                    DocumentScannerScreen(
                                        targetSlot = slot,
                                        onImageCaptured = { uri ->
                                            if (slot == "ss") {
                                                viewModel.setScreenshotFromUri(uri, "Camera_SS.jpg")
                                            } else {
                                                viewModel.setInvoiceFromUri(uri, "Camera_Invoice.jpg")
                                            }
                                            navController.popBackStack()
                                        },
                                        onClose = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
