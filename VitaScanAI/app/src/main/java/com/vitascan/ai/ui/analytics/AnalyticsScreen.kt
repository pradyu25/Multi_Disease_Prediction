package com.vitascan.ai.ui.analytics

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.vitascan.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedParam by remember { mutableStateOf(state.selectedParameter) }

    LaunchedEffect(state.selectedParameter) {
        if (selectedParam.isEmpty() || selectedParam == "glucose") selectedParam = state.selectedParameter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HEALTH TRENDS", style = MaterialTheme.typography.titleMedium, 
                             fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GhostWhite, titleContentColor = PureBlack,
                    navigationIconContentColor = PureBlack
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(GhostWhite).padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PureBlack, strokeWidth = 1.dp)
                }
            } else if (state.parameterData.isEmpty()) {
                EmptyAnalyticsState()
            } else {
                Text("Select Parameter", style = MaterialTheme.typography.labelLarge, 
                     fontWeight = FontWeight.Black, color = PureBlack, letterSpacing = 1.sp)

                // Parameter selector chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.parameterData.keys.toList()) { param ->
                        FilterChip(
                            selected  = param == selectedParam,
                            onClick   = { selectedParam = param },
                            label     = { Text(param.uppercase(), style = MaterialTheme.typography.labelSmall) },
                            shape     = RoundedCornerShape(12.dp),
                            colors    = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PureBlack,
                                selectedLabelColor     = PureWhite,
                                containerColor         = PureWhite,
                                labelColor             = DeepGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled   = true,
                                selected  = param == selectedParam,
                                borderColor = BorderGray,
                                selectedBorderColor = PureBlack,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                }

                // Line chart
                val data = state.parameterData[selectedParam] ?: emptyList()
                if (data.size >= 2) {
                    Surface(
                        modifier  = Modifier.fillMaxWidth().height(300.dp),
                        shape     = RoundedCornerShape(24.dp),
                        color     = PureWhite,
                        border    = BorderStroke(1.dp, BorderGray)
                    ) {
                        MedicalLineChart(
                            entries   = data,
                            label     = selectedParam,
                            modifier  = Modifier.fillMaxSize().padding(20.dp)
                        )
                    }

                    // Stats row
                    val values = data.map { it.second }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("MINIMUM",  String.format("%.1f", values.min()), Modifier.weight(1f))
                        StatCard("MAXIMUM",  String.format("%.1f", values.max()), Modifier.weight(1f))
                        StatCard("LATEST",   String.format("%.1f", values.last()), Modifier.weight(1f))
                    }
                } else {
                    Surface(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(20.dp),
                        color     = ElevationGray
                    ) {
                        Text(
                            "Insufficient data points for trend analysis. Please upload at least 2 reports.",
                            modifier = Modifier.padding(24.dp),
                            color    = CaptionGray,
                            style    = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalLineChart(
    entries: List<Pair<String, Double>>,
    label: String,
    modifier: Modifier
) {
    val chartEntries = entries.mapIndexed { i, (_, v) ->
        Entry(i.toFloat(), v.toFloat())
    }
    val labels = entries.map { it.first.take(5) }

    AndroidView(
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(false)

                axisRight.isEnabled = false
                axisLeft.apply {
                    textColor = AndroidColor.BLACK
                    gridColor = AndroidColor.LTGRAY
                    setDrawAxisLine(false)
                    textSize = 10f
                }

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = AndroidColor.BLACK
                    gridColor = AndroidColor.TRANSPARENT
                    valueFormatter = IndexAxisValueFormatter(labels)
                    granularity = 1f
                    setDrawAxisLine(false)
                    textSize = 10f
                }

                animateX(800)
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(chartEntries, label).apply {
                color = AndroidColor.BLACK
                valueTextColor = AndroidColor.BLACK
                lineWidth = 3f
                setDrawCircles(true)
                setCircleColor(AndroidColor.BLACK)
                circleRadius = 6f
                circleHoleRadius = 3f
                circleHoleColor = AndroidColor.WHITE
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = AndroidColor.BLACK
                fillAlpha = 15
                valueTextSize = 10f
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        },
        modifier = modifier
    )
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Surface(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        color     = PureWhite,
        border    = BorderStroke(1.dp, BorderGray)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = PureBlack)
            Text(label, style = MaterialTheme.typography.labelSmall, color = CaptionGray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyAnalyticsState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DATA DEFICIT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MediumGray)
            Spacer(Modifier.height(16.dp))
            Text("No comparative data points available.", color = CaptionGray)
        }
    }
}
