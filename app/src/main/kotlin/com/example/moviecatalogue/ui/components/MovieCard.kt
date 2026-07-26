package com.example.moviecatalogue.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moviecatalogue.domain.MediaType
import com.example.moviecatalogue.domain.Movie

// ─── Standard Movie Card (Home / Search) ─────────────────────────────────────

/**
 * Compact poster card used in horizontal category rows and the search grid.
 *
 * HCI: rating badge top-right gives instant quality signal without reading text.
 */
@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier
            .width(130.dp)
            .glassMorphism(cornerRadius = 16.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = movie.displayTitle,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(195.dp)
            )

            // Rating badge — top-right overlay
            if (movie.voteAverage > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(
                            color  = Color.Black.copy(alpha = 0.75f),
                            shape  = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector    = Icons.Filled.Star,
                            contentDescription = null,
                            tint           = Color(0xFFF5C518),
                            modifier       = Modifier.size(10.dp)
                        )
                        Text(
                            text  = movie.formattedRating,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Media type badge — top-left overlay (TV only)
            if (movie.isTvSeries) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(
                            color  = MaterialTheme.colorScheme.primary,
                            shape  = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text  = "TV",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // Title + year below poster
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text     = movie.displayTitle,
                style    = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                minLines = 2,   // always reserve 2 lines so cards stay equal height
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color    = MaterialTheme.colorScheme.onSurface
            )
            if (movie.releaseYear.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = movie.releaseYear,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Watchlist Grid Card ──────────────────────────────────────────────────────

/**
 * 2-column watchlist card with delete button and gradient title overlay.
 *
 * HCI: delete button is visible but not disruptive; uses undo instead of confirmation.
 */
@Composable
fun WatchlistMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .glassMorphism(cornerRadius = 24.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = movie.displayTitle,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )

            // Bottom gradient for readable title overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Delete button — top-right
            IconButton(
                onClick  = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector    = Icons.Filled.Delete,
                    contentDescription = "Remove from Watchlist",
                    tint           = Color.White,
                    modifier       = Modifier.size(16.dp)
                )
            }

            // Title + rating — bottom overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text     = movie.displayTitle,
                    style    = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color    = Color.White
                )
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        Icons.Filled.Star, null,
                        tint     = Color(0xFFF5C518),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text  = movie.formattedRating,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

// ─── Shimmer Brush ────────────────────────────────────────────────────────────

/**
 * Animated shimmer gradient brush for skeleton loading placeholders.
 * Provide [targetValue] larger for wider elements.
 */
@Composable
fun ShimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val baseColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        val highlightColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        
        val shimmerColors = listOf(
            baseColor,
            highlightColor,
            baseColor
        )
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue  = targetValue,
            animationSpec = infiniteRepeatable(
                animation  = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate"
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start  = Offset.Zero,
            end    = Offset(translateAnim, translateAnim)
        )
    } else {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }
}

// ─── Shimmer Placeholders ─────────────────────────────────────────────────────

@Composable
fun ShimmerMovieCard(modifier: Modifier = Modifier) {
    val shimmer = ShimmerBrush()
    Card(
        modifier  = modifier.width(130.dp),
        shape     = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(195.dp).background(shimmer))
            Column(modifier = Modifier.padding(8.dp)) {
                Box(Modifier.fillMaxWidth(0.9f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(shimmer))
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth(0.5f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(shimmer))
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun ShimmerSliderCard(modifier: Modifier = Modifier) {
    val shimmer = ShimmerBrush(targetValue = 1800f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(shimmer)
    )
}

@Composable
fun ShimmerCategorySection(modifier: Modifier = Modifier) {
    val shimmer = ShimmerBrush()
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(150.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmer)
        )
        Spacer(Modifier.height(10.dp))
        // Content section
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) { ShimmerMovieCard() }
        }
    }
}

// ─── Continue Watching Card ──────────────────────────────────────────────────

@Composable
fun ContinueWatchingCard(
    progress: com.example.moviecatalogue.domain.WatchProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(240.dp)
            .height(135.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = progress.backdropUrl.ifBlank { progress.posterUrl }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = progress.title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Play Icon overlay (Center)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Info (Bottom Left)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                Text(
                    text = progress.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val infoText = if (progress.mediaType == MediaType.TV) {
                    "S${progress.season ?: 1} E${progress.episode ?: 1}"
                } else "Movie"

                val remainingMins = ((progress.duration - progress.currentTime) / 60).toInt()
                val timeText = if (remainingMins > 0) "$remainingMins min left" else "Almost done"

                Text(
                    text = "$infoText • $timeText",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            // Progress Indicator (Bottom)
            LinearProgressIndicator(
                progress = { 
                    if (progress.duration > 0.0) {
                        (progress.currentTime / progress.duration).toFloat().coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}
