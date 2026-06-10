package com.example.moviecatalogue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2000) // 2 second delay
        // Navigate to Main Screen (replace "home" with your actual main route)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.movflix_logo),
                contentDescription = "MovieFlix Logo",
                modifier = Modifier.size(280.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
