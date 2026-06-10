package com.example.moviecatalogue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.moviecatalogue.di.ServiceLocator
import com.example.moviecatalogue.ui.navigation.AppNavigation
import com.example.moviecatalogue.ui.theme.FinalProjectTheme

/**
 * Main Activity — single activity host for the entire Compose navigation graph.
 *
 * Edge-to-edge is enabled for an immersive, modern cinematic feel.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display (per spec requirement)
        enableEdgeToEdge()

        val repository = ServiceLocator.provideRepository(applicationContext)

        setContent {
            FinalProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(repository = repository)
                }
            }
        }
    }
}
