package com.pay.eeaapp.ui.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pay.eeaapp.domain.models.ProjectDocument
import com.pay.eeaapp.domain.models.ProjectStatus
import com.pay.eeaapp.domain.models.ReviewComment
import com.pay.eeaapp.ui.components.StatusChip
import com.pay.eeaapp.ui.proponent.state.ProjectDetailViewModel
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

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = uiState.project?.title ?: "Project Detail",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Eswatini Environment Authority",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val project = uiState.project
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color      = Color(0xFF2E7D32),
                            strokeWidth = 3.dp,
                            modifier   = Modifier.size(48.dp)
                        )
                        Text(
                            "Loading project…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                project == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.FolderOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            uiState.error ?: "Project not found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE8F5E9)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text  = project.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B5E20)
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text  = project.companyName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF388E3C)
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        StatusChip(project.status)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        InfoPill(
                                            icon  = Icons.Outlined.Person,
                                            label = project.proponentName
                                        )
                                        InfoPill(
                                            icon  = Icons.Outlined.LocationOn,
                                            label = "${project.latitude.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)}, " +
                                                    "${project.longitude.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)}"
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            DetailSection(
                                icon  = Icons.Outlined.Description,
                                title = "Description"
                            ) {
                                Text(
                                    text  = project.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        item {
                            DetailSection(
                                icon  = Icons.Outlined.FolderOpen,
                                title = "Documents (${uiState.documents.size})"
                            ) {
                                if (uiState.documents.isEmpty()) {
                                    Text(
                                        "No documents attached.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        uiState.documents.forEach { doc ->
                                            DocumentRow(doc)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            DetailSection(
                                icon  = Icons.Outlined.RateReview,
                                title = "Review history (${uiState.reviews.size})"
                            ) {
                                if (uiState.reviews.isEmpty()) {
                                    Text(
                                        "No reviews yet.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        uiState.reviews.forEach { review ->
                                            ReviewRow(review)
                                        }
                                    }
                                }
                            }
                        }

                        if (project.status == ProjectStatus.AMENDMENTS_REQUIRED) {
                            item {
                                DetailSection(
                                    icon  = Icons.Outlined.Upload,
                                    title = "Resubmit updated documents"
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFFFF3E0)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Outlined.Info,
                                                contentDescription = null,
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                "Amendments are required. Please attach updated documents and resubmit.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick  = { filePicker.launch("*/*") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape    = RoundedCornerShape(12.dp),
                                        colors   = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF2E7D32)
                                        )
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Attach updated documents")
                                    }

                                    if (uiState.pickedResubmitFiles.isNotEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        uiState.pickedResubmitFiles.forEachIndexed { index, (_, name) ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(
                                                        horizontal = 12.dp,
                                                        vertical = 8.dp
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.InsertDriveFile,
                                                        contentDescription = null,
                                                        tint = Color(0xFF2E7D32),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text     = name,
                                                        style    = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.weight(1f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    IconButton(
                                                        onClick  = { viewModel.removeResubmitFile(index) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Close,
                                                            contentDescription = "Remove",
                                                            modifier = Modifier.size(14.dp),
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (uiState.resubmitState is SubmitState.Error) {
                                        Spacer(Modifier.height(8.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Outlined.ErrorOutline,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text  = (uiState.resubmitState as SubmitState.Error).message,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }

                                    if (uiState.resubmitState is SubmitState.Success) {
                                        Spacer(Modifier.height(8.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFE8F5E9)
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Outlined.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    "Resubmitted successfully.",
                                                    color = Color(0xFF1B5E20),
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Button(
                                        onClick  = { viewModel.resubmit(projectId) },
                                        enabled  = uiState.resubmitState !is SubmitState.Loading,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape    = RoundedCornerShape(12.dp),
                                        colors   = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2E7D32),
                                            contentColor   = Color.White
                                        )
                                    ) {
                                        if (uiState.resubmitState is SubmitState.Loading) {
                                            CircularProgressIndicator(
                                                modifier    = Modifier.size(20.dp),
                                                color       = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Resubmitting…")
                                        } else {
                                            Icon(
                                                Icons.Outlined.Send,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Resubmit Application",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text  = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32)
            )
            Spacer(Modifier.width(4.dp))
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color    = Color(0xFF2E7D32).copy(alpha = 0.2f)
            )
        }
        content()
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        color    = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun InfoPill(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF388E3C),
            modifier = Modifier.size(13.dp)
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF388E3C),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DocumentRow(doc: ProjectDocument) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.InsertDriveFile,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text     = doc.fileName,
                style    = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReviewRow(review: ReviewComment) {
    val formatter = remember { SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text  = "EEA Officer",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text  = formatter.format(Date(review.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(color = Color(0xFF2E7D32).copy(alpha = 0.1f))
            Spacer(Modifier.height(8.dp))
            Text(
                text  = review.comment,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}