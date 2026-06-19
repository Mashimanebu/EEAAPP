package com.pay.eeaapp.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pay.eeaapp.domain.models.ProjectStats
import com.pay.eeaapp.ui.admin.state.AnalyticsPeriod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> Text(
                    uiState.error ?: "",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.error
                )
                uiState.stats != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    SummaryCards(stats = uiState.stats!!)
                    Spacer(Modifier.height(24.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AnalyticsPeriod.entries.forEachIndexed { index, period ->
                            SegmentedButton(
                                selected = uiState.period == period,
                                onClick = { viewModel.setPeriod(period) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = AnalyticsPeriod.entries.size
                                )
                            ) { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    val data = when (uiState.period) {
                        AnalyticsPeriod.WEEKLY -> uiState.stats!!.weekly
                        AnalyticsPeriod.MONTHLY -> uiState.stats!!.monthly
                        AnalyticsPeriod.YEARLY -> uiState.stats!!.yearly
                    }
                    BarChart(data = data)
                }
            }
        }
    }
}

@Composable
private fun SummaryCards(stats: ProjectStats) {
    val items = listOf(
        "Submitted" to stats.totalSubmitted,
        "Under Review" to stats.totalUnderReview,
        "Approved" to stats.totalApproved,
        "Rejected" to stats.totalRejected
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (label, count) ->
            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(count.toString(), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun BarChart(data: Map<String, Int>) {
    if (data.isEmpty()) {
        Text("No data for this period.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val maxValue = (data.values.maxOrNull() ?: 1).coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        data.entries.forEach { (label, value) ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall)
                    Text(value.toString(), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                val fraction = value.toFloat() / maxValue.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}