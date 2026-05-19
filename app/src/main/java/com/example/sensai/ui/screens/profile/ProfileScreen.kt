package com.example.sensai.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sensai.ui.auth.AuthViewModel
import java.io.File

// ── Design tokens ────────────────────────────────────────────────────────────
private val BgDeep       = Color(0xFF0D0B1A)
private val BgCard       = Color(0xFF1A1635)
private val BgGlass      = Color(0xFF221E40)
private val VioletPrimary = Color(0xFF7C3AED)
private val VioletLight  = Color(0xFF9D6FFF)
private val VioletGlow   = Color(0x557C3AED)
private val TextPrimary  = Color(0xFFF0EBFF)
private val TextMuted    = Color(0xFF8B80B0)
private val Divider      = Color(0xFF2E2850)

private val AvatarGradient = Brush.sweepGradient(
    listOf(VioletPrimary, VioletLight, Color(0xFF4F46E5), VioletPrimary)
)
private val XpBarGradient = Brush.horizontalGradient(
    listOf(Color(0xFF4F46E5), VioletPrimary, VioletLight)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showBottomSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(it) }
        showBottomSheet = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempCameraUri?.let { viewModel.uploadAvatar(it) }
        }
        showBottomSheet = false
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        state.error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDeep
                )
            )
        },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Avatar with glowing gradient ring ────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(148.dp)
                    .clickable { showBottomSheet = true }
            ) {
                // Outer glow blur layer
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(CircleShape)
                        .background(VioletGlow)
                        .blur(18.dp)
                )
                // Gradient ring
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(CircleShape)
                        .background(AvatarGradient),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner avatar circle
                    Box(
                        modifier = Modifier
                            .size(138.dp)
                            .clip(CircleShape)
                            .background(BgCard),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.profile?.avatarUrl != null) {
                            val imageUrl = if (state.profile!!.avatarUrl!!.startsWith("http"))
                                state.profile!!.avatarUrl
                            else
                                "http://10.0.2.2:8081" + state.profile!!.avatarUrl

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(138.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(68.dp),
                                tint = VioletPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Username ─────────────────────────────────────────────────────
            Text(
                text = state.username.ifEmpty { "Sensei User" },
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Rank badge (sleek pill) ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(VioletPrimary.copy(alpha = 0.25f), VioletLight.copy(alpha = 0.15f))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(VioletPrimary, VioletLight)),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    text = state.profile?.rank ?: "Genin",
                    color = VioletLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Stats glassmorphism card ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgGlass)
                    .border(1.dp, Divider, RoundedCornerShape(20.dp))
                    .padding(vertical = 20.dp, horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        label = "Completed",
                        value = state.profile?.animeCompleted?.toString() ?: "0"
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Divider)
                    )

                    StatItem(
                        label = "Favorites",
                        value = state.profile?.favoritesCount?.toString() ?: "0"
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Divider)
                    )

                    StatItem(
                        label = "Total XP",
                        value = state.profile?.xp?.toString() ?: "0"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── XP Progress bar ───────────────────────────────────────────────
            val currentXp = state.profile?.xp ?: 0
            val nextRankXp = 1000f
            val progress = (currentXp / nextRankXp).coerceIn(0f, 1f)

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("XP to next rank", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "$currentXp / ${nextRankXp.toInt()}",
                        color = VioletLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(BgCard)
                ) {
                    // Filled portion with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(XpBarGradient)
                    )
                    // Soft glow on top of fill
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Editable Fields ───────────────────────────────────────────────
            OutlinedTextField(
                value = state.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioletPrimary,
                    unfocusedBorderColor = Divider,
                    focusedLabelColor = VioletLight,
                    unfocusedLabelColor = TextMuted,
                    unfocusedTextColor = TextPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedContainerColor = BgCard,
                    focusedContainerColor = BgCard,
                    cursorColor = VioletLight
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.bio,
                onValueChange = { viewModel.updateBio(it) },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioletPrimary,
                    unfocusedBorderColor = Divider,
                    focusedLabelColor = VioletLight,
                    unfocusedLabelColor = TextMuted,
                    unfocusedTextColor = TextPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedContainerColor = BgCard,
                    focusedContainerColor = BgCard,
                    cursorColor = VioletLight
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Preferred Genres ──────────────────────────────────────────────
            Text(
                "Preferred Genres",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.availableGenres.forEach { genre ->
                    val isSelected = state.preferredGenres.contains(genre)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleGenre(genre) },
                        label = {
                            Text(
                                genre,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = BgGlass,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) VioletPrimary else Divider,
                            selectedBorderColor = VioletLight
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Save Profile button ───────────────────────────────────────────
            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = VioletPrimary.copy(alpha = 0.4f),
                        spotColor = VioletPrimary.copy(alpha = 0.4f)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp),
                enabled = !state.isSaving
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (!state.isSaving)
                                Brush.horizontalGradient(listOf(Color(0xFF4F46E5), VioletPrimary))
                            else
                                Brush.horizontalGradient(listOf(BgCard, BgCard))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isSaving) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = VioletLight,
                                strokeWidth = 2.dp
                            )
                            Text("Saving...", fontWeight = FontWeight.Bold, color = TextMuted)
                        }
                    } else {
                        Text("Save Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Share Location button ─────────────────────────────────────────
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions.values.all { it }) {
                    val fusedLocationClient =
                        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                    try {
                        fusedLocationClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                            null
                        ).addOnSuccessListener { location ->
                            location?.let {
                                viewModel.updateLocation(it.latitude, it.longitude)
                            } ?: run {
                                Toast.makeText(context, "Could not get location. Make sure GPS is on.", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener { e ->
                            Toast.makeText(context, "Location error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            OutlinedButton(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(VioletPrimary.copy(alpha = 0.6f), VioletLight.copy(alpha = 0.6f)))
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = VioletPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Share Location", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ── Bottom Sheet for Image Selection ─────────────────────────────────────
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = BgCard,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "Update Avatar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val tempFile = File.createTempFile("camera_image", ".jpg", context.cacheDir)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VioletPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Take a photo", color = TextPrimary, fontWeight = FontWeight.Medium)
                }

                HorizontalDivider(color = Divider, thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { galleryLauncher.launch("image/*") }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VioletPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Choose from gallery", color = TextPrimary, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ── Logout Confirmation Dialog ────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout?", color = TextMuted) },
            containerColor = BgCard,
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                    onLogout()
                }) {
                    Text("Yes, Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = VioletPrimary)
                }
            }
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = VioletLight
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted,
            letterSpacing = 0.3.sp
        )
    }
}
