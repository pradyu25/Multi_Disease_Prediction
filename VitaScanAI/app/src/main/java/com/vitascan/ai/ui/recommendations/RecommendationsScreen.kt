package com.vitascan.ai.ui.recommendations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.vitascan.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    reportId: String,
    viewModel: RecommendationsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Recommendations", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HealthGreen, titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HealthGreen)
            }
        } else if (state.recommendation == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤖", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("No recommendations yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Analyse a report first to get AI-powered guidance", color = NeutralGray)
                }
            }
        } else {
            val rec = state.recommendation!!
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // AI banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.horizontalGradient(listOf(HealthGreenDark, HealthGreen)))
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("AI Health Recommendations",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Powered by Flan-T5 AI model",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                // Doctor
                item {
                    RecommendationCard(
                        icon      = Icons.Default.LocalHospital,
                        iconColor = RiskHigh,
                        title     = "Consult a Doctor",
                        content   = rec.doctor,
                        bgColor   = Color(0xFFFFF3F3)
                    )
                }

                // Diet
                item {
                    RecommendationCard(
                        icon      = Icons.Default.Restaurant,
                        iconColor = HealthGreen,
                        title     = "Dietary Recommendations",
                        content   = rec.diet,
                        bgColor   = HealthGreenSurface
                    )
                }

                // Exercise
                item {
                    RecommendationCard(
                        icon      = Icons.Default.FitnessCenter,
                        iconColor = MedicalBlue,
                        title     = "Exercise Plan",
                        content   = rec.exercise,
                        bgColor   = MedicalBlueSurface
                    )
                }

                // Lifestyle
                item {
                    RecommendationCard(
                        icon      = Icons.Default.SelfImprovement,
                        iconColor = RiskMedium,
                        title     = "Lifestyle Changes",
                        content   = rec.lifestyle,
                        bgColor   = Color(0xFFFFF8E1)
                    )
                }

                // Disclaimer
                item {
                    Card(
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NeutralGrayLight)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, null, tint = NeutralGray, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "These recommendations are AI-generated and for informational purposes only. Always consult a qualified healthcare professional before making medical decisions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralGray
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    content: String,
    bgColor: Color
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = NeutralGrayDark)
            }
            Spacer(Modifier.height(12.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium, color = NeutralGrayDark)
        }
    }
}
