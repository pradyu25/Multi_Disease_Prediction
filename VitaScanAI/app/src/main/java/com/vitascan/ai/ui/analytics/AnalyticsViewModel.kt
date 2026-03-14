package com.vitascan.ai.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitascan.ai.data.local.dao.MedicalValueWithDate
import com.vitascan.ai.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class AnalyticsUiState(
    val parameterData: Map<String, List<Pair<String, Double>>> = emptyMap(), // param → list of (date, value)
    val selectedParameter: String = "glucose",
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val reportRepo: ReportRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> =
        reportRepo.getAllMedicalValuesWithDate()
            .map { rows -> buildChartData(rows) }
            .map { data ->
                AnalyticsUiState(
                    parameterData = data,
                    selectedParameter = data.keys.firstOrNull() ?: "glucose",
                    isLoading = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AnalyticsUiState()
            )

    fun selectParameter(param: String) {
        // Handled via UI-level copy; viewModel exposes derived state
    }

    private fun buildChartData(rows: List<MedicalValueWithDate>): Map<String, List<Pair<String, Double>>> {
        return rows
            .filter { it.value != null }
            .groupBy { it.parameterName }
            .mapValues { (_, entries) ->
                entries
                    .sortedBy { it.uploadDate }
                    .map { it.uploadDate to it.value!! }
            }
    }
}
