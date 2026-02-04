package com.example.vectorscout26

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.vectorscout26.navigation.NavGraph
import com.example.vectorscout26.ui.theme.VectorScout26Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VectorScout26Theme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
