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
                title = { Text("VitaScan AI", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MedicalBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToAnalytics) {
                        Icon(Icons.Default.Analytics, "Analytics")
                    }
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
            ExtendedFloatingActionButton(
                onClick            = onNavigateToUpload,
                containerColor     = MedicalBlue,
                contentColor       = Color.White,
                icon               = { Icon(Icons.Default.Upload, "Upload") },
                text               = { Text("Upload Report") }
            )
        }
    ) { padding ->
        if (state.isLoading && state.recentReports.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MedicalBlue)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading your health data…", color = NeutralGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting header
                item { GreetingHeader(name = state.userName) }

                // Risk score cards
                item {
                    state.latestPrediction?.let { pred ->
                        RiskScoreSection(pred, onNavigateToUpload)
                    } ?: EmptyRiskCard(onNavigateToUpload)
                }

                // Recent reports
                item {
                    Text("Recent Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (state.recentReports.isEmpty()) {
                    item {
                        EmptyReportsCard(onNavigateToUpload)
                    }
                } else {
                    items(state.recentReports) { report ->
                        ReportCard(report = report, onClick = { onNavigateToReport(report.reportId) })
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun GreetingHeader(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(MedicalBlue, MedicalBlueLight)))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text("Hello, $name 👋", style = MaterialTheme.typography.titleLarge,
                color = Color.White, fontWeight = FontWeight.Bold)
            Text("Track your health with AI", style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.8f))
        }
    }
}

@Composable
private fun RiskScoreSection(pred: PredictionEntity, onUpload: () -> Unit) {
    Column {
        Text("Health Risk Overview",
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RiskCard(label = "Diabetes",   risk = pred.diabetesRisk,     icon = Icons.Default.WaterDrop,    modifier = Modifier.weight(1f))
            RiskCard(label = "Heart",      risk = pred.heartDiseaseRisk, icon = Icons.Default.Favorite,     modifier = Modifier.weight(1f))
            RiskCard(label = "Anaemia",    risk = pred.anemiaRisk,       icon = Icons.Default.Bloodtype,    modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RiskCard(label: String, risk: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    val color = risk.toRiskColor()
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("$risk%", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralGray)
            Text(risk.toRiskLabel(), style = MaterialTheme.typography.labelSmall,
                color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun EmptyRiskCard(onUpload: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MedicalBlueSurface)
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.MonitorHeart, null, tint = MedicalBlue, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("No Health Data Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Upload a medical report to see your risk scores", color = NeutralGray,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onUpload, colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)) {
                Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Upload Report")
            }
        }
    }
}

@Composable
private fun ReportCard(report: ReportEntity, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(MedicalBlueSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, null, tint = MedicalBlue)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Report ${report.reportId.take(8)}…",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(report.uploadDate.take(10),
                    style = MaterialTheme.typography.bodySmall, color = NeutralGray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = NeutralGray)
        }
    }
}

@Composable
private fun EmptyReportsCard(onUpload: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = NeutralGrayLight)
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.FolderOpen, null, tint = NeutralGray, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text("No reports yet", style = MaterialTheme.typography.bodyMedium, color = NeutralGray)
        }
    }
}
