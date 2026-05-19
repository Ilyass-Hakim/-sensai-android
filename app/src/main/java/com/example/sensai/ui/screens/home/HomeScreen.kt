package com.example.sensai.ui.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import com.example.sensai.data.network.dto.AnimeDto
import com.example.sensai.ui.components.AnimeCard
import kotlinx.coroutines.delay

// All images served from the Spring Boot static folder
private val BANNER_IMAGES = listOf(
    "http://10.0.2.2:8081/imu.jpg",
    "http://10.0.2.2:8081/luffy_gear5.png",
    "http://10.0.2.2:8081/guts_eye.jpg",
    "http://10.0.2.2:8081/berserk.jpg",
    "http://10.0.2.2:8081/luffy.jpeg",
    "http://10.0.2.2:8081/zorooo.jpeg",
    "http://10.0.2.2:8081/download_1.jpeg",
    "http://10.0.2.2:8081/download_2.jpeg",
    "http://10.0.2.2:8081/home_banner.jpeg"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (Int) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val token by viewModel.token.collectAsState(initial = null)
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Banner — auto-cycling slideshow
                BannerSlideshow(
                    background = MaterialTheme.colorScheme.background
                )



                SectionTitle(title = "Pour toi")
                AnimeRow(animes = uiState.recommendations, onClick = onNavigateToDetail)

                Spacer(modifier = Modifier.height(16.dp))

                // Top Anime Section
                SectionTitle(title = "Top Anime")
                AnimeRow(animes = uiState.topAnime, onClick = onNavigateToDetail)

                Spacer(modifier = Modifier.height(16.dp))

                // Saison en cours Section
                SectionTitle(title = "Saison en cours")
                AnimeRow(animes = uiState.seasonalAnime, onClick = onNavigateToDetail)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun AnimeRow(animes: List<AnimeDto>, onClick: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(animes) { anime ->
            AnimeCard(anime = anime, onClick = onClick)
        }
    }
}

@Composable
fun SenseiBubble(message: String) {
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("S", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
          }
      }
  }


@Composable
fun BannerSlideshow(background: Color) {
    var currentIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4_000L)
            currentIndex = (currentIndex + 1) % BANNER_IMAGES.size
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(384.dp)) {
        Crossfade(targetState = currentIndex, animationSpec = tween(durationMillis = 800), label = "banner_crossfade") { idx ->
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(BANNER_IMAGES[idx]).crossfade(false).build(),
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> Box(Modifier.fillMaxSize().background(Color.DarkGray))
                    is AsyncImagePainter.State.Error -> Box(Modifier.fillMaxSize().background(Color.DarkGray), Alignment.Center) {
                        Icon(Icons.Default.BrokenImage, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }
        }
        // Bottom gradient bleed
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, background), startY = 320f, endY = 960f)
        ))
    }
}
