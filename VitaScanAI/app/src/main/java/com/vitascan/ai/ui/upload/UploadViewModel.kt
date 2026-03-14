package com.vitascan.ai.ui.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitascan.ai.data.models.*
import com.vitascan.ai.domain.usecases.*
import com.vitascan.ai.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UploadStep { IDLE, UPLOADING, EXTRACTING, PREDICTING, RECOMMENDING, DONE, ERROR }

data class UploadUiState(
    val step: UploadStep = UploadStep.IDLE,
    val selectedUri: Uri? = null,
    val reportId: String? = null,
    val extractedData: ExtractedData? = null,
    val prediction: PredictionResponse? = null,
    val recommendation: RecommendationResponse? = null,
    val error: String? = null,
    val progressPercent: Int = 0
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadReport: UploadReportUseCase,
    private val extractData: ExtractDataUseCase,
    private val predictDisease: PredictDiseaseUseCase,
    private val getRecommendations: GetRecommendationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState = _uiState.asStateFlow()

    fun onFileSelected(uri: Uri) {
        _uiState.value = UploadUiState(selectedUri = uri)
    }

    fun processReport() {
        val uri = _uiState.value.selectedUri ?: return

        viewModelScope.launch {
            // Step 1: Upload
            _uiState.value = _uiState.value.copy(step = UploadStep.UPLOADING, progressPercent = 10)
            val uploadResult = uploadReport(uri)
            if (uploadResult is Result.Error) {
                _uiState.value = _uiState.value.copy(step = UploadStep.ERROR, error = uploadResult.message)
                return@launch
            }
            val reportId = (uploadResult as Result.Success).data.reportId
            _uiState.value = _uiState.value.copy(reportId = reportId, progressPercent = 30)

            // Step 2: Extract
            _uiState.value = _uiState.value.copy(step = UploadStep.EXTRACTING)
            val extractResult = extractData(reportId)
            if (extractResult is Result.Error) {
                _uiState.value = _uiState.value.copy(step = UploadStep.ERROR, error = extractResult.message)
                return@launch
            }
            val extracted = (extractResult as Result.Success).data
            _uiState.value = _uiState.value.copy(extractedData = extracted, progressPercent = 55)

            // Step 3: Predict
            _uiState.value = _uiState.value.copy(step = UploadStep.PREDICTING)
            val numericParams = extracted.parameters.filterValues { it != null }.mapValues { it.value!! }
            val predictResult = predictDisease(reportId, numericParams)
            if (predictResult is Result.Error) {
                _uiState.value = _uiState.value.copy(step = UploadStep.ERROR, error = predictResult.message)
                return@launch
            }
            val prediction = (predictResult as Result.Success).data
            _uiState.value = _uiState.value.copy(prediction = prediction, progressPercent = 75)

            // Step 4: Recommend
            _uiState.value = _uiState.value.copy(step = UploadStep.RECOMMENDING)
            val recResult = getRecommendations(reportId, numericParams, prediction)
            if (recResult is Result.Error) {
                _uiState.value = _uiState.value.copy(step = UploadStep.ERROR, error = recResult.message)
                return@launch
            }
            val recommendation = (recResult as Result.Success).data
            _uiState.value = _uiState.value.copy(
                recommendation  = recommendation,
                progressPercent = 100,
                step            = UploadStep.DONE
            )
        }
    }

    fun reset() { _uiState.value = UploadUiState() }
}
