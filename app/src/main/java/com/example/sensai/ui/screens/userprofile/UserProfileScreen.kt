package com.example.sensai.ui.screens.userprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.sensai.data.network.dto.UserProfileDto
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private val BgDeep = Color(0xFF0D0B1A)
private val BgCard = Color(0xFF1A1635)
private val VioletPrimary = Color(0xFF7C3AED)
private val TextPrimary = Color(0xFFF0EBFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        },
        containerColor = BgDeep
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = VioletPrimary)
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                uiState.profile?.let { profile ->
                    UserProfileContent(profile)
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(profile: UserProfileDto) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        val resolvedAvatarUrl = when {
            profile.avatarUrl == null -> "https://api.dicebear.com/7.x/avataaars/png?seed=${profile.username}"
            profile.avatarUrl.startsWith("http") -> profile.avatarUrl
            else -> "http://10.0.2.2:8081${profile.avatarUrl}"
        }

        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(resolvedAvatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(BgCard)
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 2.dp, color = VioletPrimary)
                    }
                }
                is AsyncImagePainter.State.Error -> {
                    Box(modifier = Modifier.fillMaxSize().background(BgCard), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.BrokenImage, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    }
                }
                else -> SubcomposeAsyncImageContent()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = profile.username,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Card(
            modifier = Modifier.padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = VioletPrimary.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = profile.rank,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = VioletPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Completed", value = profile.animeCompleted.toString())
            StatItem(label = "Favorites", value = profile.favoritesCount.toString())
            StatItem(label = "Total XP", value = profile.xp.toString())
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bio Section
        if (!profile.bio.isNullOrBlank()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Bio", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = profile.bio,
                        modifier = Modifier.padding(16.dp),
                        color = TextPrimary.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Location Section with Google Maps
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = VioletPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Location", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            if (profile.latitude != null && profile.longitude != null) {
                val userLocation = LatLng(profile.latitude, profile.longitude)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(userLocation, 12f)
                }

                // Update camera when userLocation changes
                LaunchedEffect(userLocation) {
                    cameraPositionState.animate(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(userLocation, 12f)
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp), // Increased height
                    shape = RoundedCornerShape(24.dp), // More rounded corners for premium feel
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = false, // We show the user's saved location, not our current one
                            mapType = MapType.NORMAL
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            scrollGesturesEnabled = true,
                            zoomGesturesEnabled = true,
                            rotationGesturesEnabled = true,
                            tiltGesturesEnabled = true
                        )
                    ) {
                        Marker(
                            state = MarkerState(position = userLocation),
                            title = profile.username,
                            snippet = "Current Location"
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No location shared", color = Color.Gray)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VioletPrimary)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}
