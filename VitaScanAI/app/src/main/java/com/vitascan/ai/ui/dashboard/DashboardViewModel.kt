package com.vitascan.ai.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitascan.ai.data.local.entities.PredictionEntity
import com.vitascan.ai.data.local.entities.ReportEntity
import com.vitascan.ai.data.models.RecommendationResponse
import com.vitascan.ai.data.repository.AuthRepository
import com.vitascan.ai.data.repository.ReportRepository
import com.vitascan.ai.domain.usecases.GetHistoryUseCase
import com.vitascan.ai.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val recentReports: List<ReportEntity> = emptyList(),
    val latestPrediction: PredictionEntity? = null,
    val latestRecommendation: RecommendationResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val historyUseCase: GetHistoryUseCase,
    private val reportRepo: ReportRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        _uiState.value = _uiState.value.copy(
            userName  = authRepo.getUserName(),
            isLoading = true
        )

        // Observe local reports for instant display
        viewModelScope.launch {
            historyUseCase.localFlow()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { reports ->
                    _uiState.update { it.copy(recentReports = reports.take(5)) }

                    // Load latest prediction for topmost report
                    reports.firstOrNull()?.let { latest ->
                        val pred = reportRepo.getLocalPrediction(latest.reportId)
                        _uiState.update { it.copy(latestPrediction = pred) }
                    }
                }
        }

        // Sync from network
        viewModelScope.launch {
            historyUseCase()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun logout() = authRepo.logout()
}
