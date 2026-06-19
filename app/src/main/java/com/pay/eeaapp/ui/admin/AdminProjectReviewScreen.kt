package com.pay.eeaapp.ui.admin

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pay.eeaapp.domain.models.ProjectDocument
import com.pay.eeaapp.domain.models.ProjectStatus
import com.pay.eeaapp.domain.models.ReviewComment
import com.pay.eeaapp.ui.components.StatusChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProjectReviewScreen(
    projectId: String,
    onBack: () -> Unit,
    viewModel: AdminProjectReviewViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val files = uris.map { uri -> uri to (uri.lastPathSegment ?: "file") }
        if (files.isNotEmpty()) viewModel.addAttachments(files)
    }

    LaunchedEffect(projectId) { viewModel.load(projectId) }

    LaunchedEffect(uiState.actionState) {
        if (uiState.actionState is ReviewAction.Done) {
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.project?.title ?: "Review Project") },
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
                    "Project not found.",
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
                        Text(
                            "${project.proponentName} \u2022 ${project.companyName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        Column {
                            Text("Description", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(project.description, style = MaterialTheme.typography.bodyMedium)
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
                    items(uiState.documents, key = { it.id }) { doc -> DocRow(doc) }

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

                    item { HorizontalDivider() }
                    item {
                        Column {
                            Text("Add Comment", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.comment,
                                onValueChange = viewModel::onCommentChange,
                                label = { Text("Comment") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { filePicker.launch("*/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Attach Files")
                            }
                            uiState.attachments.forEachIndexed { index, (_, name) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.removeAttachment(index) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove")
                                    }
                                }
                            }

                            if (uiState.actionState is ReviewAction.Error) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    (uiState.actionState as ReviewAction.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            val isLoading = uiState.actionState is ReviewAction.Loading

                            if (project.status == ProjectStatus.SUBMITTED) {
                                Button(
                                    onClick = { viewModel.startReview(projectId) },
                                    enabled = !isLoading,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Start Review") }
                                Spacer(Modifier.height(8.dp))
                            }

                            if (project.status == ProjectStatus.UNDER_REVIEW) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.approve(projectId) },
                                        enabled = !isLoading,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Approve") }
                                    OutlinedButton(
                                        onClick = { viewModel.requestAmendments(projectId) },
                                        enabled = !isLoading,
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Request Amendments") }
                                }
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.reject(projectId) },
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Reject") }
                            }

                            if (isLoading) {
                                Spacer(Modifier.height(12.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
private fun DocRow(doc: ProjectDocument) {
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