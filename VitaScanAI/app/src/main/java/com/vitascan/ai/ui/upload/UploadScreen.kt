package com.vitascan.ai.ui.upload

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitascan.ai.ui.theme.*
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onDone: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onFileSelected(it) } }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) showCamera = true }

    LaunchedEffect(state.step) {
        if (state.step == UploadStep.DONE) {
            state.reportId?.let { onDone(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack, titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showCamera) {
                CameraPreview(
                    onImageCaptured = { uri ->
                        viewModel.onFileSelected(uri)
                        showCamera = false
                    },
                    onDismiss = { showCamera = false }
                )
            } else {
                // File type options
                Text("Select Input Method",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UploadOptionCard(
                        icon    = Icons.Default.PictureAsPdf,
                        label   = "PDF / DOCX",
                        color   = MedicalBlue,
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.weight(1f)
                    )
                    UploadOptionCard(
                        icon    = Icons.Default.CameraAlt,
                        label   = "Camera Scan",
                        color   = HealthGreen,
                        onClick = {
                            cameraPermLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    UploadOptionCard(
                        icon    = Icons.Default.Image,
                        label   = "Image",
                        color   = RiskMedium,
                        onClick = { filePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Selected file indicator
                state.selectedUri?.let { uri ->
                    Card(
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HealthGreenSurface)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = HealthGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("File selected", style = MaterialTheme.typography.bodyMedium,
                                color = HealthGreenDark, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Pipeline progress
                AnimatedVisibility(state.step != UploadStep.IDLE && state.step != UploadStep.DONE) {
                    PipelineProgressCard(state.step, state.progressPercent)
                }

                // Error display
                state.error?.let { err ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape  = RoundedCornerShape(12.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = RiskHigh)
                            Spacer(Modifier.width(8.dp))
                            Text(err, color = RiskHigh, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Process button
                Button(
                    onClick  = viewModel::processReport,
                    enabled  = state.selectedUri != null && state.step == UploadStep.IDLE,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = PureBlack,
                        contentColor   = PureWhite,
                        disabledContainerColor = LightGray,
                        disabledContentColor   = MediumGray
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Analyse Report", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun UploadOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        modifier  = modifier.clickable(onClick = onClick).aspectRatio(0.85f),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(48.dp).background(color.copy(alpha = 0.12f),
                    RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = NeutralGrayDark, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PipelineProgressCard(step: UploadStep, progress: Int) {
    val stepLabels = mapOf(
        UploadStep.UPLOADING    to "Uploading file…",
        UploadStep.EXTRACTING   to "Extracting medical data…",
        UploadStep.PREDICTING   to "Running AI predictions…",
        UploadStep.RECOMMENDING to "Generating recommendations…"
    )
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalBlueSurface)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(stepLabels[step] ?: "", style = MaterialTheme.typography.titleSmall,
                color = MedicalBlue, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress        = { progress / 100f },
                modifier        = Modifier.fillMaxWidth(),
                color           = MedicalBlue,
                trackColor      = MedicalBlueDark.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(6.dp))
            Text("$progress% complete", style = MaterialTheme.typography.bodySmall, color = NeutralGray)
        }
    }
}

@Composable
private fun CameraPreview(
    onImageCaptured: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA,
                            preview, imageCapture
                        )
                    } catch (e: Exception) { e.printStackTrace() }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Controls overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FloatingActionButton(
                onClick            = onDismiss,
                containerColor     = Color.White.copy(alpha = 0.8f),
                contentColor       = NeutralGrayDark
            ) {
                Icon(Icons.Default.Close, "Cancel")
            }
            FloatingActionButton(
                onClick        = {
                    val outputFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
                    imageCapture?.takePicture(
                        outputOptions, cameraExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                output.savedUri?.let { onImageCaptured(it) }
                            }
                            override fun onError(exc: ImageCaptureException) { exc.printStackTrace() }
                        }
                    )
                },
                containerColor = PureBlack,
                contentColor   = Color.White
            ) {
                Icon(Icons.Default.Camera, "Capture")
            }
        }
    }
}
