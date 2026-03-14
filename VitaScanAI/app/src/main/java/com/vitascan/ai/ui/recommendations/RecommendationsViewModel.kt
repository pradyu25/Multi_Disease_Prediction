package com.vitascan.ai.ui.recommendations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitascan.ai.data.local.entities.RecommendationEntity
import com.vitascan.ai.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendationsUiState(
    val recommendation: RecommendationEntity? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepo: ReportRepository
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle["reportId"])
    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val rec = reportRepo.getLocalRecommendation(reportId)
            _uiState.update { it.copy(recommendation = rec, isLoading = false) }
        }
    }
}
