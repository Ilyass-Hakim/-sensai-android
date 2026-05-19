package com.example.sensai.ui.screens.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.sensai.data.network.dto.AnimeDto
import com.example.sensai.ui.theme.*

private val BgDeepLocal    = Color(0xFF0D0B1A)
private val BgCardLocal    = Color(0xFF1A1635)
private val BgGlassLocal   = Color(0xFF221E40)
private val VioletLocal    = Color(0xFF7C3AED)
private val VioletLtLocal  = Color(0xFF9D6FFF)
private val TextPri        = Color(0xFFF0EBFF)
private val TextSec        = Color(0xFFB8AEDE)
private val TextMut        = Color(0xFF7A6FA8)
private val DividerLocal   = Color(0xFF2E2850)
private val Gold           = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    onNavigateBack: () -> Unit,
    onJoinDiscussion: (Int, String) -> Unit,
    viewModel: AnimeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, navigationIconContentColor = Color.White)
            )
        },
        containerColor = BgDeepLocal
    ) { pv ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = VioletLocal) }
        } else {
            uiState.anime?.let { anime ->
                Box(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BgDeepLocal)
                            .verticalScroll(scrollState)
                            .padding(bottom = pv.calculateBottomPadding() + 100.dp)
                    ) {
                        HeroBanner(anime)
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            Spacer(Modifier.height(12.dp))
                            GenreChipsRow(anime)
                            Spacer(Modifier.height(16.dp))
                            StatsGlassCard(anime)
                            Spacer(Modifier.height(16.dp))
                            DiscussionButton(anime, onJoinDiscussion)
                            Spacer(Modifier.height(8.dp))
                            StreamingRow(anime, context)
                            Spacer(Modifier.height(20.dp))
                            InfoGrid(anime)
                            Spacer(Modifier.height(20.dp))
                            TrailerCard(anime, context)
                            Spacer(Modifier.height(20.dp))
                            SynopsisSection(anime)
                            Spacer(Modifier.height(20.dp))
                            MusicSection(anime)
                            Spacer(Modifier.height(20.dp))
                            RelatedAnimeRow(anime, context)
                        }
                    }
                    // Floating action pill bar
                    FloatingActionBar(
                        uiState = uiState,
                        onFavorite = { viewModel.toggleFavorite() },
                        onWatching = { viewModel.setWatchStatus("watching") },
                        onCompleted = { viewModel.setWatchStatus("completed") },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

// ── Hero Banner ───────────────────────────────────────────────────────────────
@Composable
private fun HeroBanner(anime: AnimeDto) {
    Box(Modifier.fillMaxWidth().height(380.dp)) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(anime.images?.jpg?.largeImageUrl ?: anime.images?.jpg?.imageUrl)
                .crossfade(true).build(),
            contentDescription = anime.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading ->
                    Box(Modifier.fillMaxSize().background(BgCardLocal), Alignment.Center) {
                        CircularProgressIndicator(color = VioletLtLocal)
                    }
                is AsyncImagePainter.State.Error ->
                    Box(Modifier.fillMaxSize().background(BgGlassLocal), Alignment.Center) {
                        Icon(Icons.Default.BrokenImage, null, tint = TextMut, modifier = Modifier.size(56.dp))
                    }
                else -> SubcomposeAsyncImageContent()
            }
        }
        // Cinematic gradient
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Black.copy(.15f), Color.Transparent, BgDeepLocal), 0f, 380f * 3)
        ))
        // Title overlay
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            anime.rating?.let { r ->
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(VioletLocal.copy(.85f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(r.substringBefore(" "), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
            }
            Text(anime.title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 30.sp)
            anime.titleJapanese?.let { Text(it, color = Color.White.copy(.6f), fontSize = 13.sp) }
            anime.titleEnglish?.takeIf { it != anime.title }?.let {
                Text(it, color = Color.White.copy(.5f), fontSize = 12.sp)
            }
        }
    }
}

// ── Genre / Theme chips ───────────────────────────────────────────────────────
@Composable
private fun GenreChipsRow(anime: AnimeDto) {
    val all = ((anime.genres ?: emptyList()) + (anime.themes ?: emptyList())).distinctBy { it.malId }
    if (all.isEmpty()) return
    val colors = listOf(VioletLocal, Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFEF4444), Color(0xFFF59E0B))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(all) { g ->
            val c = colors[g.malId % colors.size]
            Box(
                Modifier.clip(RoundedCornerShape(50))
                    .background(c.copy(.15f))
                    .border(1.dp, c.copy(.35f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) { Text(g.name, color = c, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}

// ── Glass stats card ──────────────────────────────────────────────────────────
@Composable
private fun StatsGlassCard(anime: AnimeDto) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(BgGlassLocal).border(1.dp, DividerLocal, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            // Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Gold, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(anime.score?.toString() ?: "N/A", color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                }
                anime.scoredBy?.let {
                    Text("${formatCount(it)} users", color = TextMut, fontSize = 10.sp)
                }
                Text("Score", color = TextMut, fontSize = 10.sp)
            }
            Box(Modifier.width(1.dp).height(52.dp).background(DividerLocal))
            // Rank
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (anime.rank != null) "#${anime.rank}" else "N/A", color = TextPri, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                anime.popularity?.let { Text("Pop #$it", color = TextMut, fontSize = 10.sp) }
                Text("Rank", color = TextMut, fontSize = 10.sp)
            }
            Box(Modifier.width(1.dp).height(52.dp).background(DividerLocal))
            // Members
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (anime.members != null) formatCount(anime.members) else "N/A", color = TextPri, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Text("Members", color = TextMut, fontSize = 10.sp)
            }
        }
    }
}

private fun formatCount(n: Int): String = when {
    n >= 1_000_000 -> "${"%.1f".format(n / 1_000_000.0)}M"
    n >= 1_000 -> "${"%.1f".format(n / 1_000.0)}K"
    else -> "$n"
}

// ── Discussion button with pulse glow ─────────────────────────────────────────
@Composable
private fun DiscussionButton(anime: AnimeDto, onJoinDiscussion: (Int, String) -> Unit) {
    val inf = rememberInfiniteTransition(label = "glow")
    val glowAlpha by inf.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "ga")
    Box(
        Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(16.dp),
            spotColor = VioletLocal.copy(glowAlpha), ambientColor = VioletLocal.copy(glowAlpha))
    ) {
        Button(
            onClick = { onJoinDiscussion(anime.malId, anime.title) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color(0xFF4F46E5), VioletLocal))
            ), Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Chat, null, tint = Color.White)
                    Text("Rejoindre la discussion", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ── Streaming row ─────────────────────────────────────────────────────────────
@Composable
private fun StreamingRow(anime: AnimeDto, context: android.content.Context) {
    val links = anime.streaming ?: return
    if (links.isEmpty()) return
    Spacer(Modifier.height(12.dp))
    Text("Watch On", color = TextSec, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(links) { s ->
            val c = platformColor(s.name)
            Box(
                Modifier.clip(RoundedCornerShape(12.dp)).background(c.copy(.12f))
                    .border(1.dp, c.copy(.3f), RoundedCornerShape(12.dp))
                    .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(s.url))) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) { Text(s.name, color = c, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}

private fun platformColor(name: String) = when {
    name.contains("Crunchyroll", true) -> Color(0xFFFF6500)
    name.contains("Netflix", true) -> Color(0xFFE50914)
    name.contains("Funimation", true) -> Color(0xFF5B0BB5)
    name.contains("Amazon", true) -> Color(0xFF00A8E1)
    else -> Color(0xFF7C3AED)
}

// ── Info grid ─────────────────────────────────────────────────────────────────
@Composable
private fun InfoGrid(anime: AnimeDto) {
    val items = listOf(
        Triple("🎬", "Episodes", anime.episodes?.toString() ?: "?"),
        Triple("⏱", "Duration", anime.duration ?: "?"),
        Triple("🏢", "Studio", anime.studios?.firstOrNull()?.name ?: "?"),
        Triple("📅", "Year", anime.year?.toString() ?: "?"),
    ) + listOfNotNull(
        anime.source?.let { Triple("📖", "Source", it) }
    )
    val rows = items.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (emoji, label, value) ->
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(BgGlassLocal).border(1.dp, DividerLocal, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("$emoji  $label", color = TextMut, fontSize = 11.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(value, color = TextPri, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ── Trailer card ──────────────────────────────────────────────────────────────
@Composable
private fun TrailerCard(anime: AnimeDto, context: android.content.Context) {
    val trailer = anime.trailer ?: return
    val youtubeId = trailer.youtubeId ?: return
    val thumb = trailer.images?.largeImageUrl ?: trailer.images?.mediumImageUrl ?: "https://img.youtube.com/vi/$youtubeId/hqdefault.jpg"
    Column {
        Text("▶  Trailer", color = TextPri, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp))
                .clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$youtubeId")))
                }
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context).data(thumb).crossfade(true).build(),
                contentDescription = "Trailer",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> Box(Modifier.fillMaxSize().background(BgGlassLocal))
                    is AsyncImagePainter.State.Error -> Box(Modifier.fillMaxSize().background(BgGlassLocal), Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = TextMut, modifier = Modifier.size(48.dp))
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }
            Box(Modifier.fillMaxSize().background(Color.Black.copy(.35f)))
            Box(Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(.9f)).align(Alignment.Center), Alignment.Center) {
                Icon(Icons.Default.PlayArrow, "Play", tint = VioletLocal, modifier = Modifier.size(30.dp))
            }
        }
    }
}

// ── Synopsis ──────────────────────────────────────────────────────────────────
@Composable
private fun SynopsisSection(anime: AnimeDto) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Synopsis", color = TextPri, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = anime.synopsis ?: "No synopsis available.",
            color = TextSec,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 22.sp,
            modifier = Modifier.animateContentSize(tween(300))
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (expanded) "See less ▲" else "See more ▼",
            color = VioletLtLocal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.clickable { expanded = !expanded }.padding(vertical = 4.dp)
        )
    }
}

// ── Music & Themes ────────────────────────────────────────────────────────────
@Composable
private fun MusicSection(anime: AnimeDto) {
    val openings = anime.theme?.openings ?: return
    val endings = anime.theme.endings ?: emptyList()
    if (openings.isEmpty() && endings.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(Modifier.clickable { expanded = !expanded }.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("🎵  Music & Themes", color = TextPri, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = TextMut)
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (openings.isNotEmpty()) {
                    Text("Opening Themes", color = TextMut, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    openings.forEach { op ->
                        TrackRow("OP", op, VioletLocal)
                    }
                }
                if (endings.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Ending Themes", color = TextMut, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    endings.forEach { ed ->
                        TrackRow("ED", ed, Color(0xFF10B981))
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(tag: String, track: String, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.clip(RoundedCornerShape(4.dp)).background(accent.copy(.18f)).padding(horizontal = 5.dp, vertical = 2.dp)) {
            Text(tag, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text(track, color = TextSec, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
}

// ── Related Anime ─────────────────────────────────────────────────────────────
@Composable
private fun RelatedAnimeRow(anime: AnimeDto, context: android.content.Context) {
    val relations = anime.relations ?: return
    val entries = relations.flatMap { r -> r.entry.map { e -> Pair(r.relation, e) } }
        .filter { it.second.type == "anime" }
    if (entries.isEmpty()) return
    Column {
        Text("Related Anime", color = TextPri, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(entries) { (relation, entry) ->
                Box(
                    Modifier.width(120.dp).height(170.dp).clip(RoundedCornerShape(14.dp))
                        .background(BgGlassLocal).border(1.dp, DividerLocal, RoundedCornerShape(14.dp))
                        .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(entry.url))) }
                ) {
                    Box(Modifier.fillMaxSize().background(BgGlassLocal), Alignment.Center) {
                        Icon(Icons.Default.Movie, null, tint = TextMut, modifier = Modifier.size(36.dp))
                    }
                    Box(Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.8f)))
                    ))
                    Column(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                        Box(Modifier.clip(RoundedCornerShape(4.dp)).background(VioletLocal.copy(.75f)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                            Text(relation, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(entry.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 14.sp)
                    }
                }
            }
        }
    }
}

// ── Floating action pill bar ──────────────────────────────────────────────────
@Composable
private fun FloatingActionBar(
    uiState: AnimeDetailUiState,
    onFavorite: () -> Unit,
    onWatching: () -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.anime == null) return
    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(50)).background(
                Brush.horizontalGradient(listOf(BgGlassLocal.copy(.97f), BgGlassLocal.copy(.97f)))
            ).border(1.dp, DividerLocal, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            PillAction(
                icon = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = "Favorite",
                tint = if (uiState.isFavorite) Color(0xFFEF4444) else TextMut,
                active = uiState.isFavorite,
                activeColor = Color(0xFFEF4444),
                onClick = onFavorite
            )
            PillAction(
                icon = Icons.Default.PlayArrow,
                label = "Watching",
                tint = if (uiState.watchStatus == "watching") Color(0xFFFFA000) else TextMut,
                active = uiState.watchStatus == "watching",
                activeColor = Color(0xFFFFA000),
                onClick = onWatching
            )
            PillAction(
                icon = Icons.Default.CheckCircle,
                label = "Completed",
                tint = if (uiState.watchStatus == "completed") Color(0xFF4CAF50) else TextMut,
                active = uiState.watchStatus == "completed",
                activeColor = Color(0xFF4CAF50),
                onClick = onCompleted
            )
        }
    }
}

@Composable
private fun PillAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color, active: Boolean, activeColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .then(if (active) Modifier.background(activeColor.copy(.15f)) else Modifier)
        .clickable { onClick() }.padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Text(label, color = tint, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
