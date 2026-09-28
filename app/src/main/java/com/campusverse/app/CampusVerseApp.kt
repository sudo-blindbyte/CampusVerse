package com.campusverse.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.campusverse.app.navigation.CampusVerseNavHost
import com.campusverse.app.ui.theme.CampusVerseTheme


@Composable
fun CampusVerseApp() {
    CampusVerseTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CampusVerseNavHost()
        }
    }
}
