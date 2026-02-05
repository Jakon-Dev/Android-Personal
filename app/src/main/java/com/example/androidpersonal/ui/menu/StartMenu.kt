package com.example.androidpersonal.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidpersonal.ui.theme.OrangeAccent
import kotlinx.coroutines.delay

@Composable
fun StartMenu(
    userName: String,
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val showText = remember { mutableStateOf(false) }
    val showButtons = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showText.value = true
        delay(300)
        showButtons.value = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle Background Animation (Floating Orb)
        FloatingOrb(
            color = OrangeAccent.copy(alpha = 0.1f),
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-50).dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Big Bold Typography
            AnimatedVisibility(
                visible = showText.value,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 50 }
            ) {
                Column {
                    Text(
                        text = "HELLO ${userName.uppercase()}.",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 60.sp, // Slightly smaller to fit longer names
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "WELCOME BACK",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Modern Minimalist Buttons
            AnimatedVisibility(
                visible = showButtons.value,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 50 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    MinimalistButton(
                        text = "Get Started",
                        isPrimary = true,
                        onClick = onStartClick
                    )
                    MinimalistButton(
                        text = "Settings",
                        isPrimary = false,
                        onClick = onSettingsClick
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalistButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
                color = if (isPrimary) OrangeAccent else MaterialTheme.colorScheme.onBackground
            )
        )
        
        // Arrow Icon
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = if (isPrimary) OrangeAccent else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )
    }
    // Divider line
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
    )
}

@Composable
fun FloatingOrb(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        infiniteTransition.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = infiniteTransition.value * 50f
                scaleX = 1f + (infiniteTransition.value * 0.1f)
                scaleY = 1f + (infiniteTransition.value * 0.1f)
            }
            .clip(CircleShape)
            .background(color)
    )
}
