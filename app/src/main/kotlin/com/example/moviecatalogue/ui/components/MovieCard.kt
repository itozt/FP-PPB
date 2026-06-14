package com.example.moviecatalogue.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title,
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
        }

        // Title + year below poster
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text     = movie.title,
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
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title,
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
                    text     = movie.title,
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
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f)
        )
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue  = targetValue,
            animationSpec = infiniteRepeatable(
                animation  = tween(durationMillis = 800, easing = LinearEasing),
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
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) { ShimmerMovieCard() }
        }
    }
}
