package com.example.sisvvapp.ui.screens.splash

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sisvvapp.R
import com.example.sisvvapp.ui.theme.*
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType
import com.example.sisvvapp.ui.viewmodel.SplashViewModel
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    viewModel: SplashViewModel = viewModel(
        factory = SisvvViewModelFactory(androidx.compose.ui.platform.LocalContext.current)
    )
) {
    val isTablet = LocalDeviceType.current == DeviceType.TABLET

    // --- LOGICA DE NAVEGACION ---
    LaunchedEffect(Unit) {
        viewModel.destination.collect { route ->
            onNavigate(route)
        }
    }

    // Logo Animations
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }

    // Pelota Animation
    val ballProgress = remember { Animatable(0f) }

    // Anillos pulsantes
    val infinite = rememberInfiniteTransition(label = "rings")
    val ring1Alpha by infinite.animateFloat(
        initialValue = 0.05f, targetValue = 0.16f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "r1"
    )
    val ring2Alpha by infinite.animateFloat(
        initialValue = 0.10f, targetValue = 0.03f,
        animationSpec = infiniteRepeatable(tween(3000, 900), RepeatMode.Reverse), label = "r2"
    )

    // Texto de estado
    val statusMessages = listOf(
        stringResource(R.string.splash_status_init),
        stringResource(R.string.splash_status_loading),
        stringResource(R.string.splash_status_config),
        stringResource(R.string.splash_status_welcome)
    )
    var statusIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(700)) }
        logoScale.animateTo(1f, tween(900, easing = { t ->
            val p = t - 1f; p * p * (2.8f * p + 1.8f) + 1f
        }))
        launch {
            delay(300)
            ballProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(2600, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
            )
        }
        launch {
            repeat(statusMessages.size - 1) {
                delay(850)
                statusIndex++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        val ringColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.45f
            val baseRadius = if (size.width > 600.dp.toPx()) 200.dp.toPx() else 140.dp.toPx()

            listOf(
                baseRadius to ring1Alpha,
                (baseRadius * 1.5f) to ring2Alpha,
            ).forEach { (r, a) ->
                drawCircle(
                    color = ringColor.copy(alpha = a),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 0.8.dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.common_logo_desc),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = if (isTablet) 420.dp else 280.dp)
                    .aspectRatio(3.4f)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha  = logoAlpha.value
                    }
            )

            Spacer(Modifier.height(if (isTablet) 80.dp else 56.dp))

            GolfBallLoader(
                progress = ballProgress.value,
                isTablet = isTablet,
                modifier = Modifier
                    .width(if (isTablet) 380.dp else 220.dp)
                    .height(if (isTablet) 64.dp else 48.dp)
                    .alpha(if (ballProgress.value > 0f) 1f else 0f)
            )

            Spacer(Modifier.height(24.dp))

            // Texto de estado con transición suave
            AnimatedContent(
                targetState = statusMessages[statusIndex],
                transitionSpec = {
                    (fadeIn(tween(400)) + slideInVertically { it / 2 })
                        .togetherWith(fadeOut(tween(400)) + slideOutVertically { -it / 2 })
                },
                label = "statusText"
            ) { text ->
                Text(
                    text = text,
                    fontSize = if (isTablet) 12.sp else 10.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 2.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun GolfBallLoader(progress: Float, isTablet: Boolean = false, modifier: Modifier = Modifier) {
    val bounceY = abs(sin(progress * PI.toFloat() * 3f))

    Canvas(modifier = modifier) {
        val trackY = size.height
        val trackH = (if (isTablet) 6.dp else 5.dp).toPx()
        val ballR = (if (isTablet) 20.dp else 14.dp).toPx()
        val trackW = size.width
        val ballX = progress * (trackW - ballR * 2f) + ballR
        val maxBounce = (if (isTablet) 32.dp else 22.dp).toPx()
        val ballY = trackY - trackH - ballR - bounceY * maxBounce

        // ── Césped ──
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(SplashGreenDeep, SplashGreenMid, SplashGreenDeep)
            ),
            topLeft = Offset(0f, trackY - trackH),
            size = Size(trackW, trackH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f)
        )

        // ── Estela ──
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, SplashGreenLime.copy(alpha = 0.4f))
            ),
            start = Offset(0f, trackY - trackH - ballR),
            end = Offset((ballX - ballR).coerceAtLeast(0f), trackY - trackH - ballR),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // ── Sombra de la pelota ──
        val shadowAlpha = 0.18f - bounceY * 0.10f
        val shadowScaleX = 1f - bounceY * 0.4f
        drawOval(
            color = Color.Black.copy(alpha = shadowAlpha),
            topLeft = Offset(ballX - ballR * shadowScaleX, trackY - 5.dp.toPx()),
            size = Size(ballR * 2f * shadowScaleX, 6.dp.toPx())
        )

        // ── Pelota de golf (mejorada) ──
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFF5F5F5),
                    SplashBallGrayLight,
                    SplashBallGrayDark
                ),
                center = Offset(ballX - ballR * 0.3f, ballY - ballR * 0.3f),
                radius = ballR * 1.4f
            ),
            radius = ballR,
            center = Offset(ballX, ballY)
        )

        // Brillo principal
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = ballR * 0.28f,
            center = Offset(ballX - ballR * 0.3f, ballY - ballR * 0.35f)
        )
        // Brillo secundario
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = ballR * 0.15f,
            center = Offset(ballX - ballR * 0.1f, ballY - ballR * 0.55f)
        )

        // Hoyuelos (patrón hexagonal)
        val dimpleColor = Color.Black.copy(alpha = 0.07f)
        val dimpleHighlight = Color.White.copy(alpha = 0.15f)
        val dimpleR = (if (isTablet) 1.6.dp else 1.2.dp).toPx()
        val spacing = ballR * 0.32f

        val dimpleOffsets = listOf(
            0f to 0f,
            spacing to 0f, -spacing to 0f,
            spacing * 0.5f to spacing * 0.87f, -spacing * 0.5f to spacing * 0.87f,
            spacing * 0.5f to -spacing * 0.87f, -spacing * 0.5f to -spacing * 0.87f,
            spacing * 1.5f to spacing * 0.87f, -spacing * 1.5f to spacing * 0.87f,
            spacing * 1.5f to -spacing * 0.87f, -spacing * 1.5f to -spacing * 0.87f,
            spacing * 2f to 0f, -spacing * 2f to 0f,
            0f to spacing * 1.74f, 0f to -spacing * 1.74f,
            spacing to spacing * 1.74f, -spacing to spacing * 1.74f,
            spacing to -spacing * 1.74f, -spacing to -spacing * 1.74f,
        )

        for ((dx, dy) in dimpleOffsets) {
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < ballR * 0.85f) {
                drawCircle(
                    color = dimpleColor,
                    radius = dimpleR,
                    center = Offset(ballX + dx, ballY + dy)
                )
                drawCircle(
                    color = dimpleHighlight,
                    radius = dimpleR * 0.6f,
                    center = Offset(ballX + dx - 0.3f, ballY + dy - 0.3f)
                )
            }
        }
    }
}