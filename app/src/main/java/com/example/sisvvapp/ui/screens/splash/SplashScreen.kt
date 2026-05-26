package com.example.sisvvapp.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.sisvvapp.R
import com.example.sisvvapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {

    // Logo
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }

    // Pelota: progreso 0f→1f sobre la pista
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
    var statusIndex by remember { mutableStateOf(0) }
    val statusAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(700)) }
        logoScale.animateTo(1f, tween(900, easing = { t ->
            val p = t - 1f; p * p * (2.8f * p + 1.8f) + 1f
        }))
        launch {
            delay(300)
            ballProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(2800, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
            )
        }
        launch {
            repeat(statusMessages.size - 1) {
                delay(900)
                statusAlpha.animateTo(0f, tween(250))
                statusIndex++
                statusAlpha.animateTo(1f, tween(250))
            }
        }
        delay(3200)
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Anillos decorativos
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.44f
            listOf(
                140.dp.toPx() to ring1Alpha,
                210.dp.toPx() to ring2Alpha,
            ).forEach { (r, a) ->
                drawCircle(
                    color = SplashGreenDeep.copy(alpha = a),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 0.8.dp.toPx())
                )
            }
        }


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.common_logo_desc),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3.4f)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha  = logoAlpha.value
                    }
            )

            Spacer(Modifier.height(56.dp))

            // Pista con pelota
            GolfBallLoader(
                progress = ballProgress.value,
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp)   // espacio para el rebote
                    .alpha(if (ballProgress.value > 0f) 1f else 0f)
            )

            Spacer(Modifier.height(16.dp))

            // Texto dinámico
            Text(
                text = statusMessages[statusIndex],
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.5.sp,
                color = SplashGreenDeep.copy(alpha = statusAlpha.value * 0.45f),
            )
        }
    }
}

@Composable
fun GolfBallLoader(progress: Float, modifier: Modifier = Modifier) {

    // Rebote: función senoidal sobre el progreso, 3 botes completos
    val bounceY = abs(sin(progress * PI.toFloat() * 3f))

    Canvas(modifier = modifier) {
        val trackY    = size.height               // base del césped
        val trackH    = 5.dp.toPx()
        val ballR     = 16.dp.toPx()              // radio pelota (32dp diámetro)
        val trackW    = size.width
        val ballX     = progress * (trackW - ballR * 2f) + ballR
        val maxBounce = 24.dp.toPx()
        val ballY     = trackY - trackH - ballR - bounceY * maxBounce

        // ── Estela ──────────────────────────────────────────────────────
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, SplashGreenLime.copy(alpha = 0.4f)),
                startX = 0f, endX = ballX
            ),
            start = Offset(0f, trackY - trackH - ballR),
            end   = Offset(ballX - ballR, trackY - trackH - ballR),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // ── Sombra de la pelota ─────────────────────────────────────────
        val shadowAlpha  = 0.18f - bounceY * 0.10f
        val shadowScaleX = 1f - bounceY * 0.4f
        drawOval(
            color = Color.Black.copy(alpha = shadowAlpha),
            topLeft = Offset(ballX - ballR * shadowScaleX, trackY - 6.dp.toPx()),
            size    = Size(ballR * 2f * shadowScaleX, 7.dp.toPx())
        )

        // ── Césped (pista) ──────────────────────────────────────────────
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(SplashGreenDeep, SplashGreenMid, SplashGreenDeep)
            ),
            topLeft     = Offset(0f, trackY - trackH),
            size        = Size(trackW, trackH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f)
        )

        // ── Pelota de golf ──────────────────────────────────────────────
        // Sombra interior / volumen
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    SplashBallGrayLight,
                    SplashBallGrayDark
                ),
                center = Offset(ballX - ballR * 0.25f, ballY - ballR * 0.25f),
                radius = ballR * 1.3f
            ),
            radius = ballR,
            center = Offset(ballX, ballY)
        )

        // Brillo superior
        drawCircle(
            color  = Color.White.copy(alpha = 0.75f),
            radius = ballR * 0.35f,
            center = Offset(ballX - ballR * 0.28f, ballY - ballR * 0.28f)
        )




// Hoyuelos (dimples) — esparcidos uniformemente para simular una textura esférica
        val dimpleColor = Color.Black.copy(alpha = 0.12f) // Un poco más sutil
        val dimpleR     = 1.8.dp.toPx() // Reducido ligeramente para evitar que se encimen

        val dimpleOffsets = listOf(
            // Zona Central (bien espaciados)
            Offset(ballX - ballR * 0.15f, ballY - ballR * 0.10f),
            Offset(ballX + ballR * 0.20f, ballY - ballR * 0.15f),
            Offset(ballX - ballR * 0.20f, ballY + ballR * 0.25f),
            Offset(ballX + ballR * 0.15f, ballY + ballR * 0.20f),

            // Zona Intermedia (distribución radial)
            Offset(ballX - ballR * 0.45f, ballY - ballR * 0.30f),
            Offset(ballX + ballR * 0.40f, ballY - ballR * 0.40f),
            Offset(ballX - ballR * 0.40f, ballY + ballR * 0.40f),
            Offset(ballX + ballR * 0.45f, ballY + ballR * 0.35f),

            // Bordes Superiores e Inferiores
            Offset(ballX - ballR * 0.05f, ballY - ballR * 0.65f),
            Offset(ballX + ballR * 0.25f, ballY - ballR * 0.60f),
            Offset(ballX - ballR * 0.25f, ballY + ballR * 0.65f),
            Offset(ballX + ballR * 0.02f, ballY + ballR * 0.68f),

            // Bordes Laterales Extremos
            Offset(ballX - ballR * 0.65f, ballY - ballR * 0.05f),
            Offset(ballX - ballR * 0.68f, ballY + ballR * 0.18f),
            Offset(ballX + ballR * 0.65f, ballY - ballR * 0.12f),
            Offset(ballX + ballR * 0.68f, ballY + ballR * 0.10f)
        )

        dimpleOffsets.forEach { center ->
            drawCircle(color = dimpleColor, radius = dimpleR, center = center)
        }
    }
}
