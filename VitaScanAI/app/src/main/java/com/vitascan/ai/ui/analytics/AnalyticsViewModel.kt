package com.vitascan.ai.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitascan.ai.data.local.dao.MedicalValueWithDate
import com.vitascan.ai.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import com.vitascan.ai.data.local.dao.PredictionWithDate

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
        combine(
            reportRepo.getAllMedicalValuesWithDate(),
            reportRepo.getPredictionsWithDate()
        ) { values, predictions ->
            val data = buildChartData(values, predictions)
            AnalyticsUiState(
                parameterData = data,
                selectedParameter = if (data.containsKey("diabetes risk")) "diabetes risk" 
                                   else data.keys.firstOrNull() ?: "glucose",
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalyticsUiState()
        )

    fun selectParameter(param: String) {
        // Derived from derived state (handled in Screen state or via copy)
    }

    private fun buildChartData(
        rows: List<com.vitascan.ai.data.local.dao.MedicalValueWithDate>,
        preds: List<com.vitascan.ai.data.local.dao.PredictionWithDate>
    ): Map<String, List<Pair<String, Double>>> {
        val result = mutableMapOf<String, MutableList<Pair<String, Double>>>()
        
        // Lab values
        rows.filter { it.value != null }.forEach {
            result.getOrPut(it.parameterName) { mutableListOf() }
                .add(it.uploadDate to it.value!!)
        }
        
        // Risk scores (Virtual parameters)
        if (preds.isNotEmpty()) {
            result["diabetes risk"] = preds.map { it.uploadDate to it.prediction.diabetesRisk.toDouble() }.toMutableList()
            result["heart risk"] = preds.map { it.uploadDate to it.prediction.heartDiseaseRisk.toDouble() }.toMutableList()
            result["anemia risk"] = preds.map { it.uploadDate to it.prediction.anemiaRisk.toDouble() }.toMutableList()
        }

        return result.mapValues { (_, v) -> v.sortedBy { it.first } }
    }
}
