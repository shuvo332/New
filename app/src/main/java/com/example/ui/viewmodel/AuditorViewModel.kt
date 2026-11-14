package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.ai.AuditAiRepository
import com.example.data.ai.PlaceholderAuditAiRepository
import com.example.data.local.AuditWithItems
import com.example.data.local.AuditorDatabase
import com.example.data.local.BagStatus
import com.example.data.local.DiscrepancyWithAudit
import com.example.data.local.OrderBagEntity
import com.example.data.repository.AuditRepository
import com.example.util.ImageLoaderUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

data class DashboardStats(
    val totalAudits: Int = 0,
    val openDiscrepancies: Int = 0,
    val totalVarianceCaught: Double = 0.0,
    val averageMatchRate: Int = 100
)

data class ImageSlotState(
    val bitmap: Bitmap? = null,
    val label: String = "",
    val uriString: String? = null,
    val isLoading: Boolean = false
)

sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState
    data class Running(val stageText: String) : AnalysisUiState
    data class Completed(val auditId: Long) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

data class SmartAuditUiState(
    val screenshotSlot: ImageSlotState = ImageSlotState(),
    val invoiceSlot: ImageSlotState = ImageSlotState(),
    val orderRefInput: String = "",
    val vendorInput: String = "",
    val analysisState: AnalysisUiState = AnalysisUiState.Idle
) {
    val canRunAudit: Boolean
        get() = screenshotSlot.bitmap != null &&
            invoiceSlot.bitmap != null &&
            analysisState !is AnalysisUiState.Running
}

class AuditorViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AuditorDatabase.getInstance(application).auditDao()
    private val repository = AuditRepository(dao)
    private val aiRepository: AuditAiRepository = PlaceholderAuditAiRepository()

    val audits: StateFlow<List<AuditWithItems>> = repository.observeAllAudits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val discrepancies: StateFlow<List<DiscrepancyWithAudit>> = repository.observeAllDiscrepancies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val bags: StateFlow<List<OrderBagEntity>> = repository.observeAllBags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = combine(audits, discrepancies) { auditList, discList ->
        val totalAudits = auditList.size
        val openIssues = discList.count { !it.item.isResolved }
        val totalVariance = auditList.sumOf { abs(it.audit.netVariance) }
        val avgMatch = if (auditList.isEmpty()) 100 else {
            auditList.map { it.audit.matchRatePercent }.average().toInt()
        }
        DashboardStats(
            totalAudits = totalAudits,
            openDiscrepancies = openIssues,
            totalVarianceCaught = totalVariance,
            averageMatchRate = avgMatch
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DashboardStats())

    private val _smartAuditState = MutableStateFlow(SmartAuditUiState())
    val smartAuditState: StateFlow<SmartAuditUiState> = _smartAuditState.asStateFlow()

    private val activeAuditId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeResultAudit: StateFlow<AuditWithItems?> = activeAuditId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.observeAudit(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun observeAudit(auditId: Long): Flow<AuditWithItems?> = repository.observeAudit(auditId)

    fun updateOrderRef(value: String) {
        _smartAuditState.update { it.copy(orderRefInput = value) }
    }

    fun updateVendorName(value: String) {
        _smartAuditState.update { it.copy(vendorInput = value) }
    }

    fun prefillFromBag(bag: OrderBagEntity) {
        _smartAuditState.update {
            it.copy(
                orderRefInput = bag.bagCode,
                vendorInput = bag.vendorName,
                analysisState = AnalysisUiState.Idle
            )
        }
        activeAuditId.value = null
    }

    fun setScreenshotFromUri(uri: Uri, fallbackLabel: String = "System_SS.jpg") {
        val context = getApplication<Application>()
        viewModelScope.launch {
            _smartAuditState.update {
                it.copy(
                    screenshotSlot = it.screenshotSlot.copy(isLoading = true),
                    analysisState = AnalysisUiState.Idle
                )
            }
            val bitmap = ImageLoaderUtil.loadSampledBitmapFromUri(context, uri)
            val displayName = ImageLoaderUtil.resolveDisplayName(context, uri, fallbackLabel)
            if (bitmap != null) {
                _smartAuditState.update {
                    it.copy(
                        screenshotSlot = ImageSlotState(
                            bitmap = bitmap,
                            label = displayName,
                            uriString = uri.toString(),
                            isLoading = false
                        )
                    )
                }
            } else {
                _smartAuditState.update {
                    it.copy(
                        screenshotSlot = it.screenshotSlot.copy(isLoading = false),
                        analysisState = AnalysisUiState.Error("Could not decode the selected screenshot image.")
                    )
                }
            }
        }
    }

    fun setInvoiceFromUri(uri: Uri, fallbackLabel: String = "Invoice_Capture.jpg") {
        val context = getApplication<Application>()
        viewModelScope.launch {
            _smartAuditState.update {
                it.copy(
                    invoiceSlot = it.invoiceSlot.copy(isLoading = true),
                    analysisState = AnalysisUiState.Idle
                )
            }
            val bitmap = ImageLoaderUtil.loadSampledBitmapFromUri(context, uri)
            val displayName = ImageLoaderUtil.resolveDisplayName(context, uri, fallbackLabel)
            if (bitmap != null) {
                _smartAuditState.update {
                    it.copy(
                        invoiceSlot = ImageSlotState(
                            bitmap = bitmap,
                            label = displayName,
                            uriString = uri.toString(),
                            isLoading = false
                        )
                    )
                }
            } else {
                _smartAuditState.update {
                    it.copy(
                        invoiceSlot = it.invoiceSlot.copy(isLoading = false),
                        analysisState = AnalysisUiState.Error("Could not decode the selected invoice image.")
                    )
                }
            }
        }
    }

    fun loadSampleScreenshot() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            _smartAuditState.update {
                it.copy(
                    screenshotSlot = it.screenshotSlot.copy(isLoading = true),
                    analysisState = AnalysisUiState.Idle
                )
            }
            val bmp = ImageLoaderUtil.loadSampledBitmapFromDrawable(context, R.drawable.img_sample_system_ss)
            _smartAuditState.update {
                it.copy(
                    screenshotSlot = ImageSlotState(
                        bitmap = bmp,
                        label = "Sample_ERP_Screen.jpg",
                        uriString = null,
                        isLoading = false
                    )
                )
            }
        }
    }

    fun loadSampleInvoice() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            _smartAuditState.update {
                it.copy(
                    invoiceSlot = it.invoiceSlot.copy(isLoading = true),
                    analysisState = AnalysisUiState.Idle
                )
            }
            val bmp = ImageLoaderUtil.loadSampledBitmapFromDrawable(context, R.drawable.img_sample_invoice_doc)
            _smartAuditState.update {
                it.copy(
                    invoiceSlot = ImageSlotState(
                        bitmap = bmp,
                        label = "Sample_Printed_Invoice.jpg",
                        uriString = null,
                        isLoading = false
                    )
                )
            }
        }
    }

    fun loadDemoPair() {
        loadSampleScreenshot()
        loadSampleInvoice()
        _smartAuditState.update {
            it.copy(
                orderRefInput = it.orderRefInput.ifBlank { "ORD-2026-8841" },
                vendorInput = it.vendorInput.ifBlank { "Apex Supply Co." }
            )
        }
    }

    fun clearScreenshot() {
        _smartAuditState.update {
            it.copy(
                screenshotSlot = ImageSlotState(),
                analysisState = AnalysisUiState.Idle
            )
        }
        activeAuditId.value = null
    }

    fun clearInvoice() {
        _smartAuditState.update {
            it.copy(
                invoiceSlot = ImageSlotState(),
                analysisState = AnalysisUiState.Idle
            )
        }
        activeAuditId.value = null
    }

    fun runInstantAudit() {
        val current = _smartAuditState.value
        val ssBitmap = current.screenshotSlot.bitmap
        val invBitmap = current.invoiceSlot.bitmap
        if (ssBitmap == null || invBitmap == null) {
            _smartAuditState.update {
                it.copy(analysisState = AnalysisUiState.Error("Please upload both a System Screenshot and an Invoice Image."))
            }
            return
        }

        viewModelScope.launch {
            _smartAuditState.update {
                it.copy(analysisState = AnalysisUiState.Running("Initializing AI Audit Engine…"))
            }
            try {
                val result = aiRepository.compareScreenshotAndInvoice(
                    screenshotBitmap = ssBitmap,
                    invoiceBitmap = invBitmap,
                    orderRefHint = current.orderRefInput,
                    vendorHint = current.vendorInput,
                    onStageProgress = { stage ->
                        _smartAuditState.update {
                            it.copy(analysisState = AnalysisUiState.Running(stage))
                        }
                    }
                )
                val context = getApplication<Application>()
                val savedSsPath = ImageLoaderUtil.persistBitmapLocally(context, ssBitmap, "audit_ss")
                val savedInvPath = ImageLoaderUtil.persistBitmapLocally(context, invBitmap, "audit_inv")

                val savedId = repository.saveAnalysisResult(
                    result = result,
                    screenshotLabel = current.screenshotSlot.label.ifBlank { "System_SS.jpg" },
                    invoiceLabel = current.invoiceSlot.label.ifBlank { "Invoice.jpg" },
                    screenshotUri = savedSsPath ?: current.screenshotSlot.uriString,
                    invoiceUri = savedInvPath ?: current.invoiceSlot.uriString
                )
                activeAuditId.value = savedId
                _smartAuditState.update {
                    it.copy(analysisState = AnalysisUiState.Completed(savedId))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _smartAuditState.update {
                    it.copy(
                        analysisState = AnalysisUiState.Error(
                            e.localizedMessage ?: "Audit comparison failed."
                        )
                    )
                }
            }
        }
    }

    fun toggleLineItemResolved(itemId: Long, currentlyResolved: Boolean) {
        viewModelScope.launch {
            repository.setLineItemResolved(itemId, !currentlyResolved)
        }
    }

    fun deleteAudit(auditId: Long) {
        viewModelScope.launch {
            val existing = audits.value.firstOrNull { it.audit.id == auditId }
            if (activeAuditId.value == auditId) {
                activeAuditId.value = null
                _smartAuditState.update { it.copy(analysisState = AnalysisUiState.Idle) }
            }
            repository.deleteAudit(auditId)
            ImageLoaderUtil.deleteLocalImageIfExists(existing?.audit?.screenshotUri)
            ImageLoaderUtil.deleteLocalImageIfExists(existing?.audit?.invoiceUri)
        }
    }

    fun addBag(
        bagCode: String,
        vendorName: String,
        itemCount: Int,
        declaredTotal: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addBag(bagCode, vendorName, itemCount, declaredTotal, notes)
        }
    }

    fun updateBagStatus(bagId: Long, status: BagStatus) {
        viewModelScope.launch {
            repository.updateBagStatus(bagId, status)
        }
    }

    fun deleteBag(bagId: Long) {
        viewModelScope.launch {
            repository.deleteBag(bagId)
        }
    }
}
