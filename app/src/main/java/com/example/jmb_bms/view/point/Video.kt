package com.example.jmb_bms.view.point

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlay( uri: Uri)
{
    val ctx = LocalContext.current
    val mediaItem = MediaItem.Builder().setUri(uri).build()

    val exoPlayer = remember(ctx,mediaItem){
        ExoPlayer.Builder(ctx).build().also { exoPl ->
            exoPl.setMediaItem(mediaItem)
            exoPl.prepare()
            exoPl.playWhenReady = false
            exoPl.repeatMode = ExoPlayer.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(
        AndroidView(modifier = Modifier.fillMaxSize() ,factory = {
        PlayerView(it).apply {
            player = exoPlayer
        }
    })){
        onDispose { exoPlayer.release() }
    }

}