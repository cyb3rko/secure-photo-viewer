package com.gtp.showapicturetoyourfriend

import android.app.KeyguardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.IntentSanitizer
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.rememberAsyncImagePainter
import com.gtp.showapicturetoyourfriend.ui.Page
import com.gtp.showapicturetoyourfriend.ui.theme.SecurePhotoViewerTheme
import io.sanghun.compose.video.VideoPlayer
import io.sanghun.compose.video.controller.VideoPlayerControllerConfig
import io.sanghun.compose.video.uri.VideoPlayerMediaItem
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

class ReceiverPictureActivity : ComponentActivity() {
    private lateinit var handly: Handler
    private lateinit var goahead: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        setContent {
            var page: Page by remember { mutableStateOf(Page.Receiver) }
            SecurePhotoViewerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    when (page) {
                        Page.Receiver -> MainContent(
                            padding = innerPadding,
                            navigate = { newPage -> page = newPage }
                        )
                        Page.Demo -> DemoContent(padding = innerPadding)
                    }
                }
            }

            // periodically checks if the screen is locked, if it is calls screenIsLocked()
            handly = Handler(Looper.getMainLooper())
            goahead = Runnable {
                val myKM = application.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                if (myKM.inKeyguardRestrictedInputMode()) {
                    page = Page.Demo
                } else {
                    handly.postDelayed(goahead, 40)
                }
            }
            goahead.run()
        }
    }

    override fun onNewIntent(intent: Intent) {
        try {
            val sanitizedIntent = IntentSanitizer.Builder()
                .allowType("image/*")
                .allowType("video/*")
                .build()
                .sanitizeByThrowing(intent)
            super.onNewIntent(sanitizedIntent)
            finish()
            startActivity(sanitizedIntent)
        } catch (_: SecurityException) {
            Log.e("IntentSanitizer", "Security Exception occured while sanitizing new intent")
            finish()
        }
    }

    @Composable
    private fun MainContent(padding: PaddingValues, navigate: (Page) -> Unit) {
        Column(modifier = Modifier.padding(start = 8.dp, top = 64.dp, end = 8.dp)) {
            Text(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth(),
                text = stringResource(R.string.lock_your_device),
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.pin),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
            )
            Button(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally),
                onClick = { navigate(Page.Demo) },
                content = { Text(stringResource(R.string.view)) }
            )
        }
    }

    @Composable
    private fun DemoContent(padding: PaddingValues) {
        val imageUris = remember { getImages() }
        val pagerState = rememberPagerState(pageCount = { imageUris.size })
        val zoomState = rememberZoomState(maxScale = 10f)
        LaunchedEffect(pagerState.currentPage) {
            zoomState.reset()
        }
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState
            ) { pageIndex ->
                ImageDemo(zoomState, imageUris[pageIndex].toString())
            }
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.LightGray else Color.DarkGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun BoxScope.ImageDemo(zoomState: ZoomState, uri: String) {
        val uriNormal = uri.toUri()
        val startWith = contentResolver.getType(uriNormal)
        if (startWith != null) {
            if (startWith.startsWith("image/")) {
                Image(
                    painter = rememberAsyncImagePainter(uriNormal),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomable(zoomState, enableOneFingerZoom = false)
                )
            } else if (startWith.startsWith("video/")) {
                VideoPlayer(
                    mediaItems = listOf(
                       VideoPlayerMediaItem.StorageMediaItem(storageUri = uriNormal)
                    ),
                    enablePip = false,
                    controllerConfig = VideoPlayerControllerConfig(
                        showSpeedAndPitchOverlay = false,
                        showSubtitleButton = false,
                        showCurrentTimeAndTotalTime = true,
                        showBufferingProgress = true,
                        showForwardIncrementButton = true,
                        showBackwardIncrementButton = true,
                        showBackTrackButton = false,
                        showNextTrackButton = false,
                        showRepeatModeButton = false,
                        controllerShowTimeMilliSeconds = 3_000,
                        controllerAutoShow = true,
                        showFullScreenButton = false
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                )
            }
        }
    }

    private fun getImages(): ArrayList<Uri> {
        val action = intent.action
        var imageUris: ArrayList<Uri>? = null

        if (Intent.ACTION_SEND == action) {
            val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            imageUris = ArrayList()
            if (imageUri != null) {
                imageUris.add(imageUri)
            }
        } else if (Intent.ACTION_SEND_MULTIPLE == action) {
            imageUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            }
        }
        return imageUris ?: arrayListOf()
    }

    override fun onDestroy() {
        handly.removeCallbacks(goahead)
        super.onDestroy()
    }
}
