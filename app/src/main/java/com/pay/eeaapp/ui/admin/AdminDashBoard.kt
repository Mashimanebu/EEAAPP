package com.pay.eeaapp.ui.admin

import android.R.attr.type
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectStatus
import com.pay.eeaapp.domain.models.User
import com.pay.eeaapp.ui.admin.state.AdminFilter
import com.pay.eeaapp.ui.components.ProjectExporter
import com.pay.eeaapp.ui.components.StatusChip
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onProjectClick: (String) -> Unit,
    onAnalyticsClick: () -> Unit,
    onMapClick: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val userName  = uiState.user?.fullName ?: "Admin"
    val userEmail = uiState.user?.email    ?: ""
    val initials  = userName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2).joinToString("")

    val projects = uiState.allProjects

    var showAccountSheet by remember { mutableStateOf(false) }

    if (showAccountSheet && uiState.user != null) {
        AccountBottomSheet(
            user      = uiState.user!!,
            onDismiss = { showAccountSheet = false },
            onSave    = { name, company ->
                viewModel.updateProfile(name, company)
                showAccountSheet = false
            },
            onSignOut = {
                showAccountSheet = false
                viewModel.signOut()
                onSignOut()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(EeaGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "EEA Admin",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAnalyticsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = "Analytics")
                    }
                    IconButton(onClick = onMapClick) {
                        Icon(Icons.Default.Map, contentDescription = "Map")
                    }
                    IconButton(onClick = { showAccountSheet = true }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(EeaGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            if (userEmail.isNotBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Logged in as $userEmail",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                val stats = listOf(
                    StatItem("Total",      projects.size,                                                     Color(0xFF1565C0), AdminFilter.ALL,                 Icons.Outlined.Folder),
                    StatItem("Submitted",  projects.count { it.status == ProjectStatus.SUBMITTED },           Color(0xFF6A1B9A), AdminFilter.SUBMITTED,            Icons.Outlined.Upload),
                    StatItem("In Review",  projects.count { it.status == ProjectStatus.UNDER_REVIEW },        Color(0xFF0277BD), AdminFilter.UNDER_REVIEW,         Icons.Outlined.FindInPage),
                    StatItem("Amendments", projects.count { it.status == ProjectStatus.AMENDMENTS_REQUIRED }, Color(0xFFE65100), AdminFilter.AMENDMENTS_REQUIRED,  Icons.Outlined.Edit),
                    StatItem("Approved",   projects.count { it.status == ProjectStatus.APPROVED },            EeaGreen,         AdminFilter.APPROVED,             Icons.Outlined.CheckCircle),
                    StatItem("Rejected",   projects.count { it.status == ProjectStatus.REJECTED },            Color(0xFFC62828), AdminFilter.REJECTED,             Icons.Outlined.Cancel),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(stats) { stat ->
                        DashboardStatCard(
                            item       = stat,
                            isSelected = uiState.filter == stat.filter,
                            onClick    = { viewModel.setFilter(stat.filter) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            item {

                val context = androidx.compose.ui.platform.LocalContext.current
                val exportScope = rememberCoroutineScope()
                var isExporting by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (uiState.filter) {
                                AdminFilter.ALL                 -> "All applications"
                                AdminFilter.SUBMITTED           -> "Submitted"
                                AdminFilter.UNDER_REVIEW        -> "Under review"
                                AdminFilter.AMENDMENTS_REQUIRED -> "Amendments required"
                                AdminFilter.APPROVED            -> "Approved"
                                AdminFilter.REJECTED            -> "Rejected"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.filteredProjects.size} project(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            exportScope.launch {
                                isExporting = true
                                try {
                                    val uri = withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        ProjectExporter.export(
                                            context   = context,
                                            projects  = uiState.allProjects,
                                            documents = uiState.allDocuments,
                                            reviews   = uiState.allReviews
                                        )
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Export to Excel"))
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        enabled = !isExporting && uiState.allProjects.isNotEmpty(),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EeaGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EeaGreen)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = EeaGreen
                            )
                        } else {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Export to Excel", fontSize = 12.sp)
                        }
                    }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(AdminFilter.entries.toList()) { filter ->
                        FilterChip(
                            selected = uiState.filter == filter,
                            onClick  = { viewModel.setFilter(filter) },
                            label    = {
                                Text(
                                    filter.name.replace('_', ' ')
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
                HorizontalDivider()
            }

            when {
                uiState.isLoading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = EeaGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                "Loading projects…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                uiState.filteredProjects.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "No projects match this filter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> items(uiState.filteredProjects, key = { it.id }) { project ->
                    AdminProjectRow(
                        project = project,
                        onClick = { onProjectClick(project.id) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountBottomSheet(
    user: User,
    onDismiss: () -> Unit,
    onSave: (name: String, company: String) -> Unit,
    onSignOut: () -> Unit
) {
    var isEditing   by remember { mutableStateOf(false) }
    var editName    by remember { mutableStateOf(user.fullName) }
    var editCompany by remember { mutableStateOf(user.company ?: "") }

    val initials = user.fullName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2).joinToString("")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(EeaGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = initials,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text  = user.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            Surface(
                color        = EeaGreenLight,
                shape        = RoundedCornerShape(50),
                contentColor = EeaGreenDark
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text  = "EEA ${user.role.name.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            if (isEditing) {
                OutlinedTextField(
                    value         = editName,
                    onValueChange = { editName = it },
                    label         = { Text("Full name") },
                    leadingIcon   = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = editCompany,
                    onValueChange = { editCompany = it },
                    label         = { Text("Organisation / company") },
                    leadingIcon   = { Icon(Icons.Outlined.Business, contentDescription = null) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = user.email,
                    onValueChange = {},
                    label         = { Text("Email (cannot be changed)") },
                    leadingIcon   = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    modifier      = Modifier.fillMaxWidth(),
                    enabled       = false,
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = { isEditing = false },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp)
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (editName.isNotBlank()) {
                                onSave(editName.trim(), editCompany.trim())
                                isEditing = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = EeaGreen,
                            contentColor   = Color.White
                        )
                    ) { Text("Save changes") }
                }

            } else {
                ProfileInfoRow(Icons.Outlined.Person,   "Full name",    user.fullName)
                ProfileInfoRow(Icons.Outlined.Email,    "Email address", user.email)
                ProfileInfoRow(Icons.Outlined.Business, "Organisation", user.company ?: "—")
                ProfileInfoRow(Icons.Outlined.Badge,    "Role",         user.role.name.replaceFirstChar { it.uppercase() })

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick  = { isEditing = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = EeaGreen,
                        contentColor   = Color.White
                    )
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Edit profile")
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick  = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sign out")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EeaGreen,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

private data class StatItem(
    val label: String,
    val count: Int,
    val color: Color,
    val filter: AdminFilter,
    val icon: ImageVector
)

@Composable
private fun DashboardStatCard(
    item: StatItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape   = RoundedCornerShape(16.dp),
        colors  = CardDefaults.cardColors(
            containerColor = if (isSelected) item.color.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        border  = if (isSelected)
            androidx.compose.foundation.BorderStroke(1.5.dp, item.color)
        else
            CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text  = item.count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = item.color
            )
            Text(
                text  = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminProjectRow(project: Project, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(EeaGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = EeaGreen,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = project.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "${project.proponentName} \u2022 ${project.companyName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        StatusChip(project.status)
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}


private val EeaGreen       = Color(0xFF2E7D32)
private val EeaGreenLight  = Color(0xFFE8F5E9)
private val EeaGreenDark   = Color(0xFF1B5E20)
private val EeaGreenMid    = Color(0xFF388E3C)
