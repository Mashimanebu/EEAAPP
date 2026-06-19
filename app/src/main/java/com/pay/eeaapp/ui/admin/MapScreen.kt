package com.pay.eeaapp.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.pay.eeaapp.data.dao.ProjectLocationRow
import com.pay.eeaapp.domain.models.ProjectStatus

private val DEFAULT_CENTER = LatLng(-26.5225, 31.4659) // Mbabane / Eswatini

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onProjectClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_CENTER, 9f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize().padding(padding),
                cameraPositionState = cameraPositionState
            ) {
                uiState.locations.forEach { location ->
                    LocationMarker(location = location, onClick = { onProjectClick(location.id) })
                }
            }
        }
    }
}

@Composable
private fun LocationMarker(location: ProjectLocationRow, onClick: () -> Unit) {
    val markerState = remember(location.id) {
        MarkerState(position = LatLng(location.latitude, location.longitude))
    }
    val status = remember(location.status) { ProjectStatus.fromName(location.status) }
    Marker(
        state = markerState,
        title = location.title,
        snippet = status.label,
        onClick = {
            onClick()
            true
        }
    )
}