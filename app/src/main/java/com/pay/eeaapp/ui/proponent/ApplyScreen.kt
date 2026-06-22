package com.pay.eeaapp.ui.proponent

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.pay.eeaapp.ui.proponent.state.SubmitState

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyProjectScreen(
    onSubmitted: () -> Unit,
    onBack: () -> Unit,
    currentUserName: String = "",
    currentUserInitials: String = "",
    viewModel: ApplyProjectViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLocating    by remember { mutableStateOf(false) }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLocating = true
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    isLocating = false
                    if (loc != null) {
                        viewModel.setLocation(loc.latitude, loc.longitude)
                        locationError = null
                    } else {

                        fusedClient.lastLocation
                            .addOnSuccessListener { last ->
                                if (last != null) {
                                    viewModel.setLocation(last.latitude, last.longitude)
                                } else {
                                    locationError = "Could not get location. Enter manually."
                                }
                            }
                            .addOnFailureListener {
                                locationError = "Location unavailable. Enter manually."
                            }
                    }
                }
                .addOnFailureListener {
                    isLocating = false
                    locationError = it.message ?: "Location failed."
                }
        } else {
            locationError = "Location permission denied."
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val files = uris.map { uri ->
            val name = queryFileName(context, uri) ?: uri.lastPathSegment ?: "file"
            uri to name
        }
        if (files.isNotEmpty()) viewModel.addFiles(files)
    }

    LaunchedEffect(uiState.submitState) {
        if (uiState.submitState is SubmitState.Success) {
            onSubmitted()
            viewModel.resetSubmitState()
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
                                "New Application",
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {

            if (currentUserName.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUserInitials,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUserName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Submitting as proponent",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF388E3C)
                            )
                        }
                        Icon(
                            Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            SectionHeader(
                icon  = Icons.Outlined.Description,
                title = "Project details"
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value         = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    label         = { Text("Project title") },
                    leadingIcon   = { Icon(Icons.Outlined.Title, contentDescription = null) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    singleLine    = true
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value         = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label         = { Text("Description") },
                    leadingIcon   = { Icon(Icons.Outlined.Notes, contentDescription = null) },
                    minLines      = 3,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value         = uiState.companyName,
                    onValueChange = viewModel::onCompanyNameChange,
                    label         = { Text("Company / organisation name") },
                    leadingIcon   = { Icon(Icons.Outlined.Business, contentDescription = null) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    singleLine    = true
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionHeader(
                icon  = Icons.Outlined.LocationOn,
                title = "Project location"
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    enabled  = !isLocating,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor   = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = Color.White
                        )
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isLocating) "Detecting location…"
                        else "Use my current location"
                    )
                }

                locationError?.let {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text  = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "  or enter manually  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value         = uiState.latitude,
                        onValueChange = viewModel::onLatitudeChange,
                        label         = { Text("Latitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(12.dp),
                        singleLine    = true
                    )
                    OutlinedTextField(
                        value         = uiState.longitude,
                        onValueChange = viewModel::onLongitudeChange,
                        label         = { Text("Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(12.dp),
                        singleLine    = true
                    )
                }

                if (uiState.latitude.isNotBlank() && uiState.longitude.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color        = Color(0xFFE8F5E9),
                        shape        = RoundedCornerShape(50),
                        contentColor = Color(0xFF1B5E20)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                "${uiState.latitude.take(9)}, ${uiState.longitude.take(9)}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionHeader(
                icon  = Icons.Outlined.AttachFile,
                title = "Supporting documents"
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                    Text("Attach documents")
                }

                if (uiState.pickedFiles.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "No documents attached yet. At least one is required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    uiState.pickedFiles.forEachIndexed { index, (_, name) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.InsertDriveFile,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text     = name,
                                    style    = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                IconButton(
                                    onClick  = { viewModel.removeFile(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.submitState is SubmitState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text  = (uiState.submitState as SubmitState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick  = { viewModel.submit() },
                enabled  = uiState.submitState !is SubmitState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor   = Color.White
                )
            ) {
                if (uiState.submitState is SubmitState.Loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Submitting…")
                } else {
                    Icon(
                        Icons.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Submit Application",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
}

private fun queryFileName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        }
    } catch (_: Exception) { null }
}