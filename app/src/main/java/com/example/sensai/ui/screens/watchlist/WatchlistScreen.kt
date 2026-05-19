package com.example.sensai.ui.screens.watchlist

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.sensai.data.network.dto.AnimeHistoryDto
import com.example.sensai.data.network.dto.FavoriteDto
import com.example.sensai.ui.theme.*

// ── Local design tokens ───────────────────────────────────────────────────────
private val CardBg       = Color(0xFF1A1635)
private val CardElevated = Color(0xFF211D3E)
private val GlassBorder  = Color(0xFF7C3AED).copy(alpha = 0.25f)
private val WatchingColor   = Color(0xFF7C3AED)
private val CompletedColor  = Color(0xFF10B981)
private val DroppedColor    = Color(0xFFEF4444)
private val PlanColor       = Color(0xFFF59E0B)

private fun statusColor(status: String) = when (status.lowercase()) {
    "completed"     -> CompletedColor
    "watching"      -> WatchingColor
    "dropped"       -> DroppedColor
    "plan_to_watch" -> PlanColor
    else            -> Color(0xFF6B7280)
}

private fun statusLabel(status: String) = status.replace("_", " ")
    .split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Watchlist", fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ── Premium pill tab bar ──────────────────────────────────────────
            PillTabRow(
                selectedTab   = uiState.activeTab,
                historyCount  = uiState.history.size,
                favCount      = uiState.favorites.size,
                onSelect      = { viewModel.setTab(it) }
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VioletPrimary)
                }
            } else {
                when (uiState.activeTab) {
                    WatchlistTab.HISTORY -> HistoryTab(
                        history            = uiState.history,
                        onNavigateToDetail = onNavigateToDetail,
                        onRemove           = { viewModel.removeFromHistory(it) }
                    )
                    WatchlistTab.FAVORITES -> FavoritesTab(
                        favorites          = uiState.favorites,
                        onNavigateToDetail = onNavigateToDetail,
                        onRemove           = { viewModel.removeFromFavorites(it) }
                    )
                }
            }
        }
    }
}

// ── Animated pill tab row ─────────────────────────────────────────────────────
@Composable
private fun PillTabRow(
    selectedTab: WatchlistTab,
    historyCount: Int,
    favCount: Int,
    onSelect: (WatchlistTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDeep)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(50))
            .background(CardBg)
            .padding(4.dp)
    ) {
        listOf(
            Triple(WatchlistTab.HISTORY,   Icons.Default.History,  historyCount),
            Triple(WatchlistTab.FAVORITES, Icons.Default.Favorite, favCount)
        ).forEach { (tab, icon, count) ->
            val selected = selectedTab == tab
            val label = if (tab == WatchlistTab.HISTORY) "History" else "Favorites"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (selected) Modifier.background(
                            Brush.horizontalGradient(listOf(Color(0xFF4F46E5), VioletPrimary))
                        ) else Modifier
                    )
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(icon, contentDescription = null,
                        tint = if (selected) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp))
                    Text(label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        letterSpacing = 0.4.sp,
                        color = if (selected) Color.White else TextSecondary)
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (selected) Color.White.copy(alpha = 0.25f)
                                    else VioletPrimary.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else VioletLight)
                        }
                    }
                }
            }
        }
    }
}

// ── History tab ───────────────────────────────────────────────────────────────
@Composable
private fun HistoryTab(
    history: List<AnimeHistoryDto>,
    onNavigateToDetail: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    if (history.isEmpty()) {
        EmptyState(
            icon = Icons.Default.History,
            title = "Your history is empty",
            subtitle = "Anime you watch will appear here",
            accentColor = VioletPrimary
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(history, key = { _, it -> it.id }) { index, item ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 60L)
                    visible = true
                }
                val cardAlpha by animateFloatAsState(
                    targetValue = if (visible) 1f else 0f,
                    animationSpec = tween(380), label = "h_alpha_$index"
                )
                val cardOffset by animateFloatAsState(
                    targetValue = if (visible) 0f else 28f,
                    animationSpec = tween(380, easing = FastOutSlowInEasing), label = "h_off_$index"
                )
                Box(modifier = Modifier.offset(y = cardOffset.dp).alpha(cardAlpha)) {
                    HistoryItemCard(
                        item     = item,
                        onView   = { onNavigateToDetail(item.animeId) },
                        onRemove = { onRemove(item.animeId) }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── History card ──────────────────────────────────────────────────────────────
@Composable
private fun HistoryItemCard(
    item: AnimeHistoryDto,
    onView: () -> Unit,
    onRemove: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val accent = statusColor(item.status)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = CardElevated,
            title = { Text("Remove from history?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "\"${item.animeTitle ?: "This anime"}\" will be removed from your history.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onRemove() }) {
                    Text("Remove", color = DroppedColor, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = VioletLight)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp),
                ambientColor = accent.copy(alpha = 0.12f),
                spotColor    = accent.copy(alpha = 0.16f))
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap      = { onView() },
                    onLongPress = { showDeleteDialog = true }
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Cover art with gradient overlay ───────────────────────────────
            Box(
                modifier = Modifier
                    .size(width = 88.dp, height = 124.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardElevated)
            ) {
                if (item.animeImageUrl != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.animeImageUrl).crossfade(true).build(),
                        contentDescription = item.animeTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading ->
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = VioletLight)
                                }
                            is AsyncImagePainter.State.Error ->
                                Box(Modifier.fillMaxSize().background(CardElevated), Alignment.Center) {
                                    Icon(Icons.Default.BrokenImage, null, tint = TextMuted, modifier = Modifier.size(24.dp))
                                }
                            else -> SubcomposeAsyncImageContent()
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize().background(
                        Brush.linearGradient(listOf(accent.copy(.25f), BgDeep))
                    ), Alignment.Center) {
                        Icon(Icons.Default.History, null, tint = accent, modifier = Modifier.size(28.dp))
                    }
                }
                // Bottom gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, CardBg))
                        )
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.animeTitle ?: "Anime #${item.animeId}",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(8.dp))

                // Status badge
                StatusBadge(status = item.status, accent = accent)

                Spacer(Modifier.height(8.dp))

                // Secondary metadata
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (item.episodesWatched > 0) {
                        MetaChip(text = "${item.episodesWatched} ep", icon = Icons.Default.PlayCircle)
                    }
                    item.userRating?.let { r ->
                        MetaChip(text = "★ ${"%.1f".format(r)}", color = Color(0xFFFFD700))
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Long-press hint icon
            Icon(
                Icons.Default.MoreVert, contentDescription = "Hold to delete",
                tint = TextMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Favorites tab ─────────────────────────────────────────────────────────────
@Composable
private fun FavoritesTab(
    favorites: List<FavoriteDto>,
    onNavigateToDetail: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    if (favorites.isEmpty()) {
        EmptyState(
            icon = Icons.Default.FavoriteBorder,
            title = "No favorites yet",
            subtitle = "Tap the ♥ on any anime to save it here",
            accentColor = Color(0xFFEF4444)
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(favorites, key = { _, it -> it.id }) { index, item ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 60L)
                    visible = true
                }
                val cardAlpha by animateFloatAsState(
                    targetValue = if (visible) 1f else 0f,
                    animationSpec = tween(380), label = "f_alpha_$index"
                )
                val cardOffset by animateFloatAsState(
                    targetValue = if (visible) 0f else 28f,
                    animationSpec = tween(380, easing = FastOutSlowInEasing), label = "f_off_$index"
                )
                Box(modifier = Modifier.offset(y = cardOffset.dp).alpha(cardAlpha)) {
                    FavoriteItemCard(
                        item     = item,
                        onView   = { onNavigateToDetail(item.animeId) },
                        onRemove = { onRemove(item.animeId) }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── Favorite card ─────────────────────────────────────────────────────────────
@Composable
private fun FavoriteItemCard(
    item: FavoriteDto,
    onView: () -> Unit,
    onRemove: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val accent = Color(0xFFEF4444)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = CardElevated,
            title = { Text("Remove from favorites?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "\"${item.animeTitle ?: "This anime"}\" will be removed from your favorites.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onRemove() }) {
                    Text("Remove", color = DroppedColor, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = VioletLight)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp),
                ambientColor = accent.copy(alpha = 0.10f),
                spotColor    = VioletPrimary.copy(alpha = 0.14f))
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap       = { onView() },
                    onLongPress = { showDeleteDialog = true }
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover art
            Box(
                modifier = Modifier
                    .size(width = 88.dp, height = 124.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardElevated)
            ) {
                if (item.animeImageUrl != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.animeImageUrl).crossfade(true).build(),
                        contentDescription = item.animeTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading ->
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = VioletLight)
                                }
                            is AsyncImagePainter.State.Error ->
                                Box(Modifier.fillMaxSize().background(CardElevated), Alignment.Center) {
                                    Icon(Icons.Default.BrokenImage, null, tint = TextMuted, modifier = Modifier.size(24.dp))
                                }
                            else -> SubcomposeAsyncImageContent()
                        }
                    }
                } else {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.linearGradient(listOf(VioletPrimary.copy(.3f), accent.copy(.15f)))
                        ), Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, null, tint = accent, modifier = Modifier.size(30.dp))
                    }
                }
                // Bottom gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(48.dp)
                        .align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, CardBg)))
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.animeTitle ?: "Anime #${item.animeId}",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(10.dp))
                // Favorite pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accent.copy(alpha = 0.15f))
                        .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Favorite, null, tint = accent, modifier = Modifier.size(10.dp))
                        Text("Favorite", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = accent)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Hold to remove  •  Tap to view", color = TextMuted.copy(alpha = 0.5f), fontSize = 10.sp)
            }

            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.MoreVert, contentDescription = "Hold to delete",
                tint = TextMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Status badge with pulse on "Watching" ─────────────────────────────────────
@Composable
private fun StatusBadge(status: String, accent: Color) {
    val isWatching = status.lowercase() == "watching"

    val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.7f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse_a"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = if (isWatching) pulseAlpha else 0.15f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (isWatching) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
            }
            Text(statusLabel(status), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = accent)
        }
    }
}

// ── Metadata chip ─────────────────────────────────────────────────────────────
@Composable
private fun MetaChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color = TextSecondary
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        icon?.let { Icon(it, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(11.dp)) }
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.10f))
                    .border(1.dp, accentColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor.copy(alpha = 0.6f), modifier = Modifier.size(44.dp))
            }
            Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp, textAlign = TextAlign.Center)
            Text(subtitle, color = TextMuted, fontSize = 13.sp,
                textAlign = TextAlign.Center, lineHeight = 20.sp)
        }
    }
}
