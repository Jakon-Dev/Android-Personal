package com.example.androidpersonal.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidpersonal.ui.theme.OrangeAccent
import kotlinx.coroutines.delay

data class SubApp(
    val id: String,
    val name: String,
    val icon: ImageVector
)

@Composable
fun StartMenu(
    userName: String,
    modifier: Modifier = Modifier,
    onAppClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val showText = remember { mutableStateOf(false) }
    val showGrid = remember { mutableStateOf(false) }

    val subApps = listOf(
        SubApp("finance", "Finance", Icons.Default.ShoppingCart),
        SubApp("routine", "Routine", Icons.AutoMirrored.Filled.List),
        SubApp("media", "Books, Movies & Games", Icons.Default.PlayArrow),
        SubApp("calendar", "Calendar", Icons.Default.DateRange),
        SubApp("todo", "Todo", Icons.Default.CheckCircle),
        SubApp("notes", "Notes", Icons.Default.Edit)
    )

    LaunchedEffect(Unit) {
        delay(100)
        showText.value = true
        delay(300)
        showGrid.value = true
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
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Top, // Stick title to top, or maybe padded
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
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "WELCOME BACK",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sub Apps Grid
            AnimatedVisibility(
                visible = showGrid.value,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 50 },
                modifier = Modifier.weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(subApps) { app ->
                        AppCard(app = app, onClick = { onAppClick(app.id) })
                    }
                }
            }

            // Settings Button at the bottom
            AnimatedVisibility(
                visible = showGrid.value, // Show with grid
                enter = fadeIn(tween(800))
            ) {
                MinimalistButton(
                    text = "Settings",
                    isPrimary = false,
                    onClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
fun AppCard(
    app: SubApp,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square cards
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
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
