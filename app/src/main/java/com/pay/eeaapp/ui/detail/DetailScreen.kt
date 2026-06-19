package com.pay.eeaapp.ui.detail

import com.pay.eeaapp.ui.proponent.ProjectDetailViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pay.eeaapp.domain.models.ProjectDocument
import com.pay.eeaapp.domain.models.ProjectStatus
import com.pay.eeaapp.domain.models.ReviewComment
import com.pay.eeaapp.ui.components.StatusChip
import com.pay.eeaapp.ui.proponent.state.SubmitState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBack: () -> Unit,
    viewModel: ProjectDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val files = uris.map { uri ->
            val name = uri.lastPathSegment ?: "file"
            uri to name
        }
        if (files.isNotEmpty()) viewModel.addResubmitFiles(files)
    }

    LaunchedEffect(projectId) { viewModel.load(projectId) }

    LaunchedEffect(uiState.resubmitState) {
        if (uiState.resubmitState is SubmitState.Success) {
            viewModel.resetResubmitState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.project?.title ?: "Project") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val project = uiState.project
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                project == null -> Text(
                    uiState.error ?: "Project not found.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(project.title, style = MaterialTheme.typography.headlineSmall)
                            StatusChip(project.status)
                        }
                    }
                    item {
                        Column {
                            Text("Description", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(project.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    item {
                        Column {
                            Text("Company", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(project.companyName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    item {
                        Text(
                            "Location: ${project.latitude}, ${project.longitude}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    item { HorizontalDivider() }

                    item { Text("Documents", style = MaterialTheme.typography.titleMedium) }
                    items(uiState.documents, key = { it.id }) { doc -> DocumentRow(doc) }

                    item { HorizontalDivider() }

                    item { Text("Review History", style = MaterialTheme.typography.titleMedium) }
                    if (uiState.reviews.isEmpty()) {
                        item {
                            Text(
                                "No reviews yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.reviews, key = { it.id }) { review -> ReviewRow(review) }
                    }

                    if (project.status == ProjectStatus.AMENDMENTS_REQUIRED) {
                        item { HorizontalDivider() }
                        item {
                            Column {
                                Text("Resubmit Documents", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { filePicker.launch("*/*") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Attach Updated Documents")
                                }
                                uiState.pickedResubmitFiles.forEachIndexed { index, (_, name) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { viewModel.removeResubmitFile(index) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove")
                                        }
                                    }
                                }
                                if (uiState.resubmitState is SubmitState.Error) {
                                    Text(
                                        (uiState.resubmitState as SubmitState.Error).message,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.resubmit(projectId) },
                                    enabled = uiState.resubmitState !is SubmitState.Loading,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (uiState.resubmitState is SubmitState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Resubmit")
                                    }
                                }
                                if (uiState.resubmitState is SubmitState.Success) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Resubmitted successfully.",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(doc: ProjectDocument) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Description, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(doc.fileName, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ReviewRow(review: ReviewComment) {
    val formatter = remember { SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                formatter.format(Date(review.createdAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}