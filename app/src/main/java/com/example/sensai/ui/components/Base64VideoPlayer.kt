package com.example.sensai.ui.components

import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File
import java.io.FileOutputStream

@Composable
fun Base64VideoPlayer(
    base64Video: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_OFF
            playWhenReady = true
        }
    }

    LaunchedEffect(base64Video) {
        // Save base64 to temp file
        val videoBytes = Base64.decode(base64Video, Base64.DEFAULT)
        val tempFile = File(context.cacheDir, "temp_sensei_video.mp4")
        FileOutputStream(tempFile).use { it.write(videoBytes) }
        
        val mediaItem = MediaItem.fromUri(Uri.fromFile(tempFile))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
