package com.pay.eeaapp.ui.proponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProponentDashboardScreen(
    onApplyClick: () -> Unit,
    onProjectClick: (String) -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProponentDashboardViewModel = viewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val hasProjects = !uiState.isLoading && uiState.projects.isNotEmpty()

    val userName = uiState.user?.fullName ?: "Proponent"
    val userEmail = uiState.user?.email   ?: ""
    val initials  = userName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2).joinToString("")

    var showAccountSheet by remember { mutableStateOf(false) }

    if (showAccountSheet && uiState.user != null) {
        ProponentAccountSheet(
            user      = uiState.user!!,
            onDismiss = { showAccountSheet = false },
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "EEA Portal",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Eswatini Environment Authority",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { showAccountSheet = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = initials,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text  = userName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text  = "Proponent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (hasProjects) {
                ExtendedFloatingActionButton(
                    onClick            = onApplyClick,
                    icon               = { Icon(Icons.Default.Add, contentDescription = null) },
                    text               = { Text("New Application") },
                    containerColor     = Color(0xFF2E7D32),
                    contentColor       = Color.White
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (userEmail.isNotBlank()) {
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
                        text  = "Logged in as $userEmail",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when {
                uiState.isLoading -> LoadingState()
                uiState.projects.isEmpty() -> EmptyProjectsState(onApplyClick = onApplyClick)
                else -> {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text  = "My Applications",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text  = "${uiState.projects.size} project(s) submitted",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider()
                    ProjectListContent(
                        projects       = uiState.projects,
                        onProjectClick = onProjectClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProponentAccountSheet(
    user: User,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
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
            // Avatar
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32)),
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color        = Color(0xFFE8F5E9),
                    shape        = RoundedCornerShape(50),
                    contentColor = Color(0xFF1B5E20)
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
                            text  = "EEA Proponent",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            ProfileInfoRow(
                icon  = Icons.Outlined.Person,
                label = "Full name",
                value = user.fullName
            )
            ProfileInfoRow(
                icon  = Icons.Outlined.Email,
                label = "Email address",
                value = user.email
            )
            ProfileInfoRow(
                icon  = Icons.Outlined.Business,
                label = "Organisation",
                value = user.company ?: "—"
            )
            ProfileInfoRow(
                icon  = Icons.Outlined.Badge,
                label = "Role",
                value = user.role.name.replaceFirstChar { it.uppercase() }
            )

            Spacer(Modifier.height(24.dp))

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
            tint = Color(0xFF2E7D32),
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

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier    = Modifier.size(52.dp),
            strokeWidth = 4.dp,
            color       = Color(0xFF2E7D32),
            trackColor  = Color(0xFF2E7D32).copy(alpha = 0.15f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Loading your projects…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyProjectsState(onApplyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        color = Color(0xFF2E7D32).copy(alpha = 0.08f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(color = Color(0xFFE8F5E9), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = Color(0xFF2E7D32)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text  = "No applications yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Submit your first environmental project application and track its review progress here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(8.dp))

        Surface(
            color        = Color(0xFFE8F5E9),
            shape        = RoundedCornerShape(50),
            contentColor = Color(0xFF1B5E20)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Eco, contentDescription = null, modifier = Modifier.size(14.dp))
                Text("Eswatini Environment Authority", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick        = onApplyClick,
            shape          = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
            modifier       = Modifier.fillMaxWidth(0.85f),
            colors         = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32),
                contentColor   = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text  = "Apply for a Project",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProjectListContent(
    projects: List<Project>,
    onProjectClick: (String) -> Unit
) {
    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(projects, key = { it.id }) { project ->
            ProjectRow(project = project, onClick = { onProjectClick(project.id) })
        }
    }
}

@Composable
private fun ProjectRow(project: Project, onClick: () -> Unit) {
    val visuals = statusVisuals(project.status.toString())

    Card(
        onClick    = onClick,
        modifier   = Modifier.fillMaxWidth(),
        shape      = RoundedCornerShape(18.dp),
        colors     = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation  = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(visuals.containerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visuals.icon,
                    contentDescription = null,
                    tint = visuals.contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = project.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = project.companyName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                StatusChip(visuals = visuals)
            }

            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatusChip(visuals: StatusVisuals) {
    Surface(
        color        = visuals.containerColor,
        contentColor = visuals.contentColor,
        shape        = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = visuals.icon,
                contentDescription = null,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text  = visuals.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class StatusVisuals(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector
)

@Composable
private fun statusVisuals(rawStatus: String): StatusVisuals {
    val normalized = rawStatus.uppercase()
    return when {
        normalized.contains("APPROVE") -> StatusVisuals(
            label          = "Approved",
            containerColor = Color(0xFFE3F5E8),
            contentColor   = Color(0xFF1E7E34),
            icon           = Icons.Outlined.CheckCircle
        )
        normalized.contains("REJECT") -> StatusVisuals(
            label          = "Rejected",
            containerColor = Color(0xFFFCE8E8),
            contentColor   = Color(0xFFC62828),
            icon           = Icons.Outlined.Cancel
        )
        normalized.contains("REVIEW") -> StatusVisuals(
            label          = "Under Review",
            containerColor = Color(0xFFE6ECFD),
            contentColor   = Color(0xFF3050C8),
            icon           = Icons.Outlined.FindInPage
        )
        normalized.contains("AMENDMENT") -> StatusVisuals(
            label          = "Amendments Required",
            containerColor = Color(0xFFFFF3E0),
            contentColor   = Color(0xFFE65100),
            icon           = Icons.Outlined.Edit
        )
        normalized.contains("SUBMIT") -> StatusVisuals(
            label          = "Submitted",
            containerColor = Color(0xFFFFF1DB),
            contentColor   = Color(0xFFB76E00),
            icon           = Icons.Outlined.Schedule
        )
        else -> StatusVisuals(
            label          = rawStatus.replaceFirstChar { it.uppercase() },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
            icon           = Icons.Outlined.Info
        )
    }
}