package com.campusverse.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Student / Alumni / Aspirant theme ───────────────────────────────────────
private val CampusLightColorScheme = lightColorScheme(
    primary                = Primary,
    onPrimary              = OnPrimary,
    primaryContainer       = PrimaryContainer,
    onPrimaryContainer     = OnPrimaryContainer,
    secondary              = Secondary,
    onSecondary            = OnSecondary,
    secondaryContainer     = SecondaryContainer,
    onSecondaryContainer   = OnSecondaryContainer,
    tertiary               = Tertiary,
    onTertiary             = OnTertiary,
    tertiaryContainer      = TertiaryContainer,
    onTertiaryContainer    = OnTertiaryContainer,
    background             = Background,
    onBackground           = OnBackground,
    surface                = Surface,
    onSurface              = OnSurface,
    surfaceVariant         = SurfaceVariant,
    onSurfaceVariant       = OnSurfaceVariant,
    outline                = Outline,
    outlineVariant         = OutlineVariant,
    error                  = Error,
    onError                = OnError,
    errorContainer         = ErrorContainer,
    onErrorContainer       = OnErrorContainer,
    inverseSurface         = InverseSurface,
    inverseOnSurface       = InverseOnSurface,
    inversePrimary         = InversePrimary,
    surfaceTint            = SurfaceTint
)

@Composable
fun CampusVerseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color disabled — preserve CampusVerse branding
    val colorScheme = CampusLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = Shapes,
        content     = content
    )
}

// ── Admin Professional dark-navy theme ──────────────────────────────────────
private val AdminDarkColorScheme = darkColorScheme(
    primary                = AdminPrimary,
    onPrimary              = AdminOnPrimary,
    primaryContainer       = AdminPrimaryContainer,
    onPrimaryContainer     = AdminOnPrimaryContainer,
    secondary              = AdminSecondary,
    onSecondary            = AdminOnSecondary,
    background             = AdminBackground,
    onBackground           = AdminOnBackground,
    surface                = AdminSurface,
    onSurface              = AdminOnSurface,
    surfaceVariant         = AdminSurfaceContainer,
    onSurfaceVariant       = AdminOnSurfaceVariant,
    outline                = AdminOutline,
    outlineVariant         = AdminOutlineVariant,
    error                  = AdminError,
    onError                = AdminOnError
)

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    val colorScheme = CampusLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = Shapes,
        content     = content
    )
}