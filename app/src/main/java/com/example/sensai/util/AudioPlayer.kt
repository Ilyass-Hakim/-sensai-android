package com.example.sensai.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import java.io.File
import java.io.FileOutputStream

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playBase64(context: Context, base64Audio: String) {
        try {
            stop()
            val audioBytes = Base64.decode(base64Audio, Base64.DEFAULT)
            
            // Create temporary file
            val tempFile = File.createTempFile("sensei_voice", ".mp3", context.cacheDir)
            tempFile.deleteOnExit()
            
            FileOutputStream(tempFile).use { it.write(audioBytes) }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }
}
