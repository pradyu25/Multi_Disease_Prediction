package com.vitascan.ai.ui.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitascan.ai.data.local.entities.PredictionEntity
import com.vitascan.ai.data.local.entities.ReportEntity
import com.vitascan.ai.ui.theme.*
import com.vitascan.ai.utils.toRiskColor
import com.vitascan.ai.utils.toRiskLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToUpload: () -> Unit,
    onNavigateToReport: (String) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = PureBlack
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("V", color = PureWhite, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("VITASCAN AI", style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GhostWhite,
                    titleContentColor = PureBlack,
                    actionIconContentColor = PureBlack
                ),
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick            = onNavigateToUpload,
                containerColor     = PureBlack,
                contentColor       = PureWhite,
                shape              = RoundedCornerShape(24.dp)
            ) {
                Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, "Upload")
                    Spacer(Modifier.width(8.dp))
                    Text("NEW SCAN", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        if (state.isLoading && state.recentReports.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PureBlack, strokeWidth = 1.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(GhostWhite).padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Greeting and Logo placeholder
                item {
                    Column {
                        Text("Vitascan Analytics", style = MaterialTheme.typography.labelMedium, color = MediumGray, letterSpacing = 1.sp)
                        Text("Hello, ${state.userName}", style = MaterialTheme.typography.displayMedium)
                    }
                }

                // Trends Banner (Prominent Navigation)
                item {
                    TrendsBanner(onClick = onNavigateToAnalytics)
                }

                // Risk score cards
                item {
                    state.latestPrediction?.let { pred ->
                        RiskScoreSection(pred)
                    } ?: EmptyRiskCard(onNavigateToUpload)
                }

                // Recent reports Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LATEST ANALYSIS", style = MaterialTheme.typography.labelLarge, 
                             fontWeight = FontWeight.Black, color = PureBlack, letterSpacing = 1.sp)
                        TextButton(onClick = onNavigateToAnalytics) {
                            Text("HISTORY", style = MaterialTheme.typography.labelSmall, color = MediumGray)
                        }
                    }
                }

                if (state.recentReports.isEmpty()) {
                    item { EmptyReportsCard(onNavigateToUpload) }
                } else {
                    items(state.recentReports) { report ->
                        ReportCard(report = report, onClick = { onNavigateToReport(report.reportId) })
                    }
                }
                
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun TrendsBanner(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PureBlack),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(Modifier.fillMaxWidth().padding(24.dp)) {
            Column(Modifier.align(Alignment.CenterStart)) {
                Text("Health Analytics", style = MaterialTheme.typography.titleMedium, color = GhostWhite, fontWeight = FontWeight.Bold)
                Text("View Parameter Trends", style = MaterialTheme.typography.bodySmall, color = LightGray)
            }
            Icon(
                Icons.Default.TrendingUp, 
                contentDescription = null, 
                tint = PureWhite, 
                modifier = Modifier.size(40.dp).align(Alignment.CenterEnd).alpha(0.3f)
            )
        }
    }
}

@Composable
private fun RiskScoreSection(pred: PredictionEntity) {
    Column {
        Text("CLINICAL RISK ASSESSMENT", style = MaterialTheme.typography.labelLarge, 
             fontWeight = FontWeight.Black, color = PureBlack, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RiskCard(label = "Diabetes",   risk = pred.diabetesRisk,     icon = Icons.Default.WaterDrop,    modifier = Modifier.weight(1f))
            RiskCard(label = "Heart",      risk = pred.heartDiseaseRisk, icon = Icons.Default.Favorite,     modifier = Modifier.weight(1f))
            RiskCard(label = "Anaemia",    risk = pred.anemiaRisk,       icon = Icons.Default.Bloodtype,    modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RiskCard(label: String, risk: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, BorderGray),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = PureBlack, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text("${risk}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReportCard(report: ReportEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(ElevationGray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.InsertDriveFile, null, tint = PureBlack)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("SCAN ANALYSIS", style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
                Text("Report ${report.reportId.take(6).uppercase()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(report.uploadDate.take(10), style = MaterialTheme.typography.bodySmall, color = MediumGray)
            }
            Icon(Icons.Default.ArrowForwardIos, null, tint = LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun EmptyRiskCard(onUpload: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = ElevationGray
    ) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DATA COLLECTION REQUIRED", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))
            Text("No clinical reports detected. Start scanning to see real-time health trends.", 
                color = MediumGray, style = MaterialTheme.typography.bodyMedium, 
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun EmptyReportsCard(onUpload: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(100.dp).background(ElevationGray, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("HISTORY EMPTY", style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Black)
    }
}
