package com.campusverse.app.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusverse.app.data.model.UserRole
import com.campusverse.app.data.repository.AuthRepositoryImpl
import com.campusverse.app.domain.auth.AuthRepository
import com.campusverse.app.navigation.Screen
import kotlinx.coroutines.delay

/**
 * High-fidelity, futuristic Splash Screen styled exactly as specified in the design reference.
 * Features concentric orbital rings, glowing glassmorphic icon badge, animated progress bar,
 * and automatic session restoration.
 */
@Composable
fun SplashScreen(
    onNavigateToDestination: (String) -> Unit,
    modifier: Modifier = Modifier,
    authRepository: AuthRepository = AuthRepositoryImpl.instance
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        val startTime = System.currentTimeMillis()
        val sessionResult = authRepository.restoreSession()
        val elapsedTime = System.currentTimeMillis() - startTime
        val remainingDelay = (1800L - elapsedTime).coerceAtLeast(0L)
        delay(remainingDelay)

        sessionResult.fold(
            onSuccess = { user ->
                if (user != null) {
                    val destination = when (user.role) {
                        UserRole.ASPIRANT -> Screen.AspirantHome.route
                        UserRole.STUDENT  -> Screen.StudentHome.route
                        UserRole.ALUMNI   -> Screen.AlumniHome.route
                        UserRole.ADMIN    -> if (user.isAdminAuthorized) {
                            Screen.AdminDashboard.route
                        } else {
                            Screen.AdminLogin.route
                        }
                    }
                    onNavigateToDestination(destination)
                } else {
                    onNavigateToDestination(Screen.Welcome.route)
                }
            },
            onFailure = {
                onNavigateToDestination(Screen.Welcome.route)
            }
        )
    }

    // Color definitions matching the design image
    val darkBackgroundStart = Color(0xFF090919)
    val darkBackgroundCenter = Color(0xFF110E30)
    val darkBackgroundEnd = Color(0xFF1B164A)
    val brightPurple = Color(0xFFA855F7)
    val glowPurple = Color(0xFF8B5CF6)
    val softSubtext = Color(0xFF94A3B8)
    val statusDotColor = Color(0xFF6366F1)

    // Infinite pulsing animation for ambient rings
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        darkBackgroundStart,
                        darkBackgroundCenter,
                        darkBackgroundEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Concentric Orbital Rings Pattern behind Logo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = glowPurple.copy(alpha = pulseAlpha * 0.8f),
                radius = 120.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = glowPurple.copy(alpha = pulseAlpha * 0.5f),
                radius = 180.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = glowPurple.copy(alpha = pulseAlpha * 0.25f),
                radius = 240.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Main Branding Group
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                animationSpec = tween(1000),
                initialOffsetY = { 40 }
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                // Outer Glow Badge Container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    // Soft Radial Glow
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .shadow(28.dp, RoundedCornerShape(32.dp), spotColor = brightPurple, ambientColor = glowPurple)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        glowPurple.copy(alpha = 0.4f),
                                        Color(0xFF4C1D95).copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(32.dp)
                            )
                    )

                    // Core Glassmorphic Badge
                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.4f),
                                        glowPurple.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(26.dp)
                            ),
                        shape = RoundedCornerShape(26.dp),
                        color = Color(0xFF6D28D9)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6),
                                        Color(0xFF6D28D9)
                                    )
                                )
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = "CampusVerse Graduation Icon",
                                tint = Color.White,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Two-Toned App Name: "Campus" (White) + "Verse" (Purple)
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append("Campus")
                        }
                        withStyle(style = SpanStyle(color = brightPurple, fontWeight = FontWeight.Bold)) {
                            append("Verse")
                        }
                    },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 38.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tagline
                Text(
                    text = "Your Campus. Your Future.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = softSubtext,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.4.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom Loading & Version Info Area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sleek Progress Bar
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = brightPurple,
                    trackColor = Color.Transparent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sync Status Text
            Text(
                text = "SYNCHRONIZING AI NODE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = softSubtext.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Version Info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(statusDotColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "v2.0.4 - Premium Access",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = softSubtext.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
