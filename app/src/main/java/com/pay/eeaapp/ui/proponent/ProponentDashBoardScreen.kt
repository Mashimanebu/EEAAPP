package com.pay.eeaapp.ui.proponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProponentDashboardScreen(
    onApplyClick: () -> Unit,
    onProjectClick: (String) -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProponentDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = { Text("My Projects") }, actions = {
            IconButton(onClick = {
                viewModel.signOut()
                onSignOut()
            }) {
                Icon(Icons.AutoMirrored.Default.ExitToApp, contentDescription = "Sign out")
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(onClick = onApplyClick) {
            Icon(Icons.Default.Add, contentDescription = "Apply for project")
        }
    }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.projects.isEmpty() -> Text(
                    "You haven't submitted any projects yet.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.projects, key = { it.id }) { project ->
                        ProjectRow(project = project, onClick = { onProjectClick(project.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectRow(project: Project, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(project.title, style = MaterialTheme.typography.titleMedium)
                StatusChip(project.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                project.description, style = MaterialTheme.typography.bodySmall, maxLines = 2
            )
        }
    }
}