package com.vitascan.ai.ui.analytics

import android.graphics.Color as AndroidColor
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
        if (selectedParam.isEmpty()) selectedParam = state.selectedParameter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Trends", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MedicalBlue, titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MedicalBlue)
                }
            } else if (state.parameterData.isEmpty()) {
                EmptyAnalyticsState()
            } else {
                Text("Track Parameter Trends",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                // Parameter selector chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.parameterData.keys.toList()) { param ->
                        FilterChip(
                            selected  = param == selectedParam,
                            onClick   = { selectedParam = param },
                            label     = { Text(param.replaceFirstChar { it.uppercase() }) },
                            colors    = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedicalBlue,
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }

                // Line chart
                val data = state.parameterData[selectedParam] ?: emptyList()
                if (data.size >= 2) {
                    Card(
                        modifier  = Modifier.fillMaxWidth().height(320.dp),
                        shape     = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors    = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        MedicalLineChart(
                            entries   = data,
                            label     = selectedParam,
                            modifier  = Modifier.fillMaxSize().padding(16.dp)
                        )
                    }

                    // Stats row
                    val values = data.map { it.second }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Min",  String.format("%.1f", values.min()), Modifier.weight(1f))
                        StatCard("Max",  String.format("%.1f", values.max()), Modifier.weight(1f))
                        StatCard("Latest", String.format("%.1f", values.last()), Modifier.weight(1f))
                    }
                } else {
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp),
                        colors    = CardDefaults.cardColors(containerColor = NeutralGrayLight)
                    ) {
                        Text(
                            "Need at least 2 reports to show trend",
                            modifier = Modifier.padding(20.dp),
                            color    = NeutralGray,
                            style    = MaterialTheme.typography.bodyMedium
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

                axisRight.isEnabled = false
                axisLeft.apply {
                    textColor = AndroidColor.parseColor("#757575")
                    gridColor = AndroidColor.parseColor("#E0E0E0")
                }

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = AndroidColor.parseColor("#757575")
                    gridColor = AndroidColor.parseColor("#E0E0E0")
                    valueFormatter = IndexAxisValueFormatter(labels)
                    granularity = 1f
                    labelCount = labels.size.coerceAtMost(6)
                }

                animateX(600)
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(chartEntries, label).apply {
                color = AndroidColor.parseColor("#1565C0")
                valueTextColor = AndroidColor.parseColor("#212121")
                lineWidth = 2.5f
                circleRadius = 5f
                setCircleColor(AndroidColor.parseColor("#1565C0"))
                circleHoleColor = AndroidColor.WHITE
                circleHoleRadius = 3f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = AndroidColor.parseColor("#1565C0")
                fillAlpha = 25
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
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MedicalBlueSurface)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MedicalBlue)
            Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralGray)
        }
    }
}

@Composable
private fun EmptyAnalyticsState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(12.dp))
            Text("No trend data available", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text("Upload reports to see analytics", color = NeutralGray)
        }
    }
}
