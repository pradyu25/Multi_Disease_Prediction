package com.vitascan.ai.ui.reportview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitascan.ai.data.local.entities.PredictionEntity
import com.vitascan.ai.data.local.entities.ReportEntity
import com.vitascan.ai.data.models.CompareResponse
import com.vitascan.ai.data.repository.ReportRepository
import com.vitascan.ai.domain.usecases.CompareReportsUseCase
import com.vitascan.ai.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportViewUiState(
    val report: ReportEntity? = null,
    val prediction: PredictionEntity? = null,
    val compareData: CompareResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepo: ReportRepository,
    private val compareUseCase: CompareReportsUseCase
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle["reportId"])

    private val _uiState = MutableStateFlow(ReportViewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            val pred = reportRepo.getLocalPrediction(reportId)
            _uiState.update { it.copy(prediction = pred, isLoading = false) }
        }

        viewModelScope.launch {
            reportRepo.getLocalReports()
                .collect { reports ->
                    val report = reports.find { it.reportId == reportId }
                    val previous = reports.firstOrNull { it.reportId != reportId }
                    _uiState.update { it.copy(report = report) }

                    if (previous != null) {
                        when (val r = compareUseCase(previous.reportId, reportId)) {
                            is Result.Success -> _uiState.update { it.copy(compareData = r.data) }
                            else -> {}
                        }
                    }
                }
        }
    }
}
