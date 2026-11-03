package com.tahiedit.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

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
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember {
        mutableStateOf<FirebaseUser?>(auth.currentUser)
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            currentUser = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(auth)
    } else {
        VideoEditorScreen(
            email = currentUser?.email ?: "",
            onLogout = { auth.signOut() }
        )
    }
}

@Composable
fun AuthScreen(auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "TAHIEDIT",
                color = Accent,
                fontSize = 34.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                if (isRegister) "Créer un compte" else "Connexion",
                color = Color.White,
                fontSize = 23.sp
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = ""
                },
                label = { Text("Adresse e-mail") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                label = { Text("Mot de passe") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red)
            }

            if (successMessage.isNotEmpty()) {
                Text(successMessage, color = Color.Green)
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Remplis tous les champs."
                    } else if (password.length < 6) {
                        errorMessage = "Le mot de passe doit avoir au moins 6 caractères."
                    } else {
                        loading = true
                        errorMessage = ""
                        successMessage = ""

                        val task = if (isRegister) {
                            auth.createUserWithEmailAndPassword(
                                email.trim(),
                                password
                            )
                        } else {
                            auth.signInWithEmailAndPassword(
                                email.trim(),
                                password
                            )
                        }

                        task.addOnCompleteListener { result ->
                            loading = false
                            if (result.isSuccessful) {
                                successMessage = "Connexion réussie."
                            } else {
                                errorMessage =
                                    result.exception?.localizedMessage
                                        ?: "Une erreur est survenue."
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent
                )
            ) {
                Text(
                    if (loading) "Patiente..." 
                    else if (isRegister) "Créer mon compte"
                    else "Se connecter"
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = {
                    isRegister = !isRegister
                    errorMessage = ""
                    successMessage = ""
                }
            ) {
                Text(
                    if (isRegister)
                        "J'ai déjà un compte : me connecter"
                    else
                        "Créer un nouveau compte",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun VideoEditorScreen(
    email: String,
    onLogout: () -> Unit
) {
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) videoUri = uri
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(videoUri) {
        videoUri?.let {
            exoPlayer.setMediaItem(MediaItem.fromUri(it))
            exoPlayer.prepare()
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
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TAHIEDIT",
                    color = Color.White,
                    fontSize = 23.sp
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        email,
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                    TextButton(onClick = onLogout) {
                        Text("Déconnexion", color = Accent)
                    }
                }
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
                        factory = { viewContext ->
                            PlayerView(viewContext).apply {
                                player = exoPlayer
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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EditorButton("Importer") {
                    picker.launch(arrayOf("video/*"))
                }

                EditorButton("Lecture") {
                    if (videoUri != null) exoPlayer.play()
                }

                EditorButton("Pause") {
                    exoPlayer.pause()
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
