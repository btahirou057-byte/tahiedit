package com.tahiedit.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private val Dark = Color(0xFF151515)
private val Panel = Color(0xFF252525)
private val Accent = Color(0xFF9C70FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TahiEditApp()
            }
        }
    }
}

@Composable
fun TahiEditApp() {
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) videoUri = uri
    }

    val player = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(videoUri) {
        videoUri?.let {
            player.setMediaItem(MediaItem.fromUri(it))
            player.prepare()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Panel)
                    .padding(16.dp),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    "TAHIEDIT",
                    color = Color.White,
                    fontSize = 23.sp
                )
                Text(
                    "EXPORT",
                    color = Accent
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (videoUri != null) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        factory = {
                            PlayerView(it).apply {
                                player = player
                                useController = true
                            }
                        }
                    )
                } else {
                    Text(
                        "Votre aperçu vidéo apparaîtra ici",
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement =
                    Arrangement.SpaceEvenly
            ) {
                EditorButton("Importer") {
                    picker.launch(arrayOf("video/*"))
                }

                EditorButton("Lecture") {
                    if (videoUri != null) player.play()
                }

                EditorButton("Pause") {
                    player.pause()
                }
            }

            Text(
                "TIMELINE",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Panel)
                    .padding(16.dp)
            ) {
                Text("Piste vidéo", color = Color.White)

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            Color(0xFF45405A),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (videoUri == null)
                            "Importez une vidéo"
                        else
                            "CLIP VIDÉO",
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text("Piste audio", color = Color.White)

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(
                            Color(0xFF315D48),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Piste audio", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EditorButton(
    title: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent
        )
    ) {
        Text(title)
    }
}
