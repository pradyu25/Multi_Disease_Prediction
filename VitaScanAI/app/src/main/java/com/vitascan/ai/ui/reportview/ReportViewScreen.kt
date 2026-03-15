package com.vitascan.ai.ui.reportview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitascan.ai.data.models.ParameterChange
import com.vitascan.ai.ui.theme.*
import com.vitascan.ai.utils.toRiskColor
import com.vitascan.ai.utils.toRiskLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportViewScreen(
    reportId: String,
    viewModel: ReportViewModel,
    onNavigateToRecs: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MedicalBlue, titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(Icons.Default.Analytics, null, tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.analyzeReport() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                    ) {
                        Icon(Icons.Default.QueryStats, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze Risk")
                    }
                    Button(
                        onClick = onNavigateToRecs,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                    ) {
                        Icon(Icons.Default.AutoAwesome, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Get Advice")
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MedicalBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Risk Summary
                state.prediction?.let { pred ->
                    item {
                        Text("Risk Assessment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RiskGauge("Diabetes",    pred.diabetesRisk,      Icons.Default.WaterDrop, Modifier.weight(1f))
                            RiskGauge("Heart",       pred.heartDiseaseRisk,  Icons.Default.Favorite,  Modifier.weight(1f))
                            RiskGauge("Anaemia",     pred.anemiaRisk,        Icons.Default.Bloodtype, Modifier.weight(1f))
                        }
                    }
                }

                // Parameter comparison
                state.compareData?.let { compare ->
                    item {
                        Text("Parameter Changes",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(compare.changes) { change ->
                        ParameterChangeCard(change)
                    }
                } ?: item {
                    Card(
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NeutralGrayLight)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = NeutralGray)
                            Spacer(Modifier.width(8.dp))
                            Text("Upload more reports to see parameter trends",
                                style = MaterialTheme.typography.bodySmall, color = NeutralGray)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun RiskGauge(
    label: String,
    risk: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    val color = risk.toRiskColor()
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text("$risk%", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralGray)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress   = { risk / 100f },
                modifier   = Modifier.fillMaxWidth(),
                color      = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun ParameterChangeCard(change: ParameterChange) {
    val (statusColor, statusIcon) = when (change.status) {
        "worsened" -> RiskHigh to Icons.Default.TrendingUp
        "improved" -> HealthGreen to Icons.Default.TrendingDown
        else       -> NeutralGray  to Icons.Default.TrendingFlat
    }

    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(change.parameter.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${change.previous ?: "–"}", style = MaterialTheme.typography.bodySmall, color = NeutralGray)
                    Icon(Icons.Default.ArrowRightAlt, null, tint = NeutralGray, modifier = Modifier.size(16.dp))
                    Text("${change.current ?: "–"}", style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when(change.status) {
                            "improved" -> "Improving"
                            "worsened" -> "Concern"
                            else -> "Stable"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(16.dp))
                }
                change.change?.let {
                    Text(
                        "${if (it > 0) "+" else ""}${String.format("%.1f", it)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}
