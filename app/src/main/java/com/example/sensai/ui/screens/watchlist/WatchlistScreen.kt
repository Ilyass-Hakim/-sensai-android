package com.example.sensai.ui.screens.watchlist

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sensai.data.network.dto.AnimeHistoryDto
import com.example.sensai.data.network.dto.FavoriteDto
import com.example.sensai.ui.theme.BgCard
import com.example.sensai.ui.theme.BgDeep
import com.example.sensai.ui.theme.BgElevated
import com.example.sensai.ui.theme.TextMuted
import com.example.sensai.ui.theme.TextPrimary
import com.example.sensai.ui.theme.TextSecondary
import com.example.sensai.ui.theme.VioletPrimary
import com.example.sensai.ui.theme.VioletLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Show toasts for errors and success messages
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
                    Text(
                        "My Watchlist",
                        fontWeight = FontWeight.Bold,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = uiState.activeTab.ordinal,
                containerColor = BgDeep,
                contentColor = VioletPrimary,
                indicator = { tabPositions ->
                    if (uiState.activeTab.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab.ordinal]),
                            color = VioletPrimary,
                            height = 3.dp
                        )
                    }
                }
            ) {
                Tab(
                    selected = uiState.activeTab == WatchlistTab.HISTORY,
                    onClick = { viewModel.setTab(WatchlistTab.HISTORY) },
                    icon = {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = if (uiState.activeTab == WatchlistTab.HISTORY) VioletPrimary else TextMuted
                        )
                    },
                    text = {
                        Text(
                            "History",
                            color = if (uiState.activeTab == WatchlistTab.HISTORY) VioletPrimary else TextMuted,
                            fontWeight = if (uiState.activeTab == WatchlistTab.HISTORY) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = uiState.activeTab == WatchlistTab.FAVORITES,
                    onClick = { viewModel.setTab(WatchlistTab.FAVORITES) },
                    icon = {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (uiState.activeTab == WatchlistTab.FAVORITES) VioletPrimary else TextMuted
                        )
                    },
                    text = {
                        Text(
                            "Favorites",
                            color = if (uiState.activeTab == WatchlistTab.FAVORITES) VioletPrimary else TextMuted,
                            fontWeight = if (uiState.activeTab == WatchlistTab.FAVORITES) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VioletPrimary)
                }
            } else {
                when (uiState.activeTab) {
                    WatchlistTab.HISTORY -> HistoryTab(
                        history = uiState.history,
                        onNavigateToDetail = onNavigateToDetail,
                        onRemove = { viewModel.removeFromHistory(it) }
                    )
                    WatchlistTab.FAVORITES -> FavoritesTab(
                        favorites = uiState.favorites,
                        onNavigateToDetail = onNavigateToDetail,
                        onRemove = { viewModel.removeFromFavorites(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(
    history: List<AnimeHistoryDto>,
    onNavigateToDetail: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    if (history.isEmpty()) {
        EmptyState(
            icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextMuted) },
            title = "No history yet",
            subtitle = "Anime you watch will appear here"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(history, key = { _, item -> item.id }) { index, item ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(200 + index * 40)) + slideInVertically(tween(200 + index * 40)) { it / 4 }
                ) {
                    HistoryItemCard(
                        item = item,
                        onView = { onNavigateToDetail(item.animeId) },
                        onRemove = { onRemove(item.animeId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: AnimeHistoryDto,
    onView: () -> Unit,
    onRemove: () -> Unit
) {
    val statusColor = when (item.status.lowercase()) {
        "completed" -> Color(0xFF4CAF50)
        "watching" -> VioletPrimary
        "dropped" -> Color(0xFFF44336)
        "plan_to_watch" -> Color(0xFFFF9800)
        else -> TextMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        onClick = onView
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add image here
            if (item.animeImageUrl != null) {
                coil.compose.AsyncImage(
                    model = item.animeImageUrl,
                    contentDescription = item.animeTitle,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 60.dp, height = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TextMuted)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.animeTitle ?: "Anime #${item.animeId}",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.status.replace("_", " ").replaceFirstChar { it.uppercase() },
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (item.episodesWatched > 0) {
                        Text(
                            text = "${item.episodesWatched} ep",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    item.userRating?.let { rating ->
                        Text(
                            text = "★ $rating",
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from history",
                    tint = Color(0xFFF44336).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FavoritesTab(
    favorites: List<FavoriteDto>,
    onNavigateToDetail: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    if (favorites.isEmpty()) {
        EmptyState(
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextMuted) },
            title = "No favorites yet",
            subtitle = "Tap the heart icon on any anime to save it here"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(favorites, key = { _, item -> item.id }) { index, item ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(200 + index * 40)) + slideInVertically(tween(200 + index * 40)) { it / 4 }
                ) {
                    FavoriteItemCard(
                        item = item,
                        onView = { onNavigateToDetail(item.animeId) },
                        onRemove = { onRemove(item.animeId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteItemCard(
    item: FavoriteDto,
    onView: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        onClick = onView
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.animeImageUrl != null) {
                coil.compose.AsyncImage(
                    model = item.animeImageUrl,
                    contentDescription = item.animeTitle,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 60.dp, height = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(listOf(VioletPrimary.copy(alpha = 0.4f), VioletLight.copy(alpha = 0.2f)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = VioletPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.animeTitle ?: "Anime #${item.animeId}",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to view details",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from favorites",
                    tint = Color(0xFFF44336).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Text(title, color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, color = TextMuted, fontSize = 14.sp)
        }
    }
}
