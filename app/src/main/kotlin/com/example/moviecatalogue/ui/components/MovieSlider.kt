package com.example.moviecatalogue.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.moviecatalogue.domain.Movie
import kotlinx.coroutines.delay

/**
 * Auto-scrolling horizontal image slider for featured/trending movies.
 *
 * HCI Principles:
 * - Visibility of system status: dot indicators show current position
 * - Affordance: Play button clearly invites interaction
 * - Aesthetic & minimalist design: gradient overlay for readable text
 * - User control: touch pauses auto-scroll (interruptibility principle)
 */
@Composable
fun MovieSlider(
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) return

    val pageCount = movies.size
    val infinite = pageCount > 1

    // For infinite scrolling we back the pager with a very large virtual page
    // count and map each virtual page back to a real movie via modulo. Starting
    // near the middle (on a multiple of pageCount) gives room to swipe both ways.
    val virtualCount = if (infinite) pageCount * 1000 else 1
    val startPage = remember(pageCount) {
        if (infinite) (virtualCount / 2).let { it - it % pageCount } else 0
    }
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { virtualCount })
    var userInteracting by remember { mutableStateOf(false) }

    // Auto-scroll every 4 s; always advances forward (wraps around infinitely).
    LaunchedEffect(userInteracting) {
        if (!userInteracting && infinite) {
            while (true) {
                delay(4_000)
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    // Detect drag → pause; resume 5 s after last interaction
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            userInteracting = true
        } else {
            delay(5_000)
            userInteracting = false
        }
    }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val movie = movies[page % pageCount]
            SliderItem(
                movie = movie,
                onClick = { onMovieClick(movie.id) }
            )
        }

        PagerIndicator(
            selectedIndex = pagerState.currentPage % pageCount,
            pageCount = pageCount,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun SliderItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movie.backdropUrl.ifEmpty { movie.posterUrl })
                .crossfade(true)
                .build(),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Multi-stop gradient for text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.45f to Color.Black.copy(alpha = 0.2f),
                            1.0f to Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        // Title + metadata
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 20.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFF5C518),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = movie.formattedRating,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFF5C518),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (movie.releaseYear.isNotBlank()) {
                    Text("•", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = movie.releaseYear,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.LightGray
                    )
                }
            }
        }

        // Play button (bottom-right)
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.White.copy(alpha = 0.25f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "View details",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun PagerIndicator(
    selectedIndex: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = selectedIndex == index
            val width by animateDpAsState(
                targetValue = if (isSelected) 20.dp else 6.dp,
                animationSpec = tween(300),
                label = "indicator_width_$index"
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                    )
            )
        }
    }
}
