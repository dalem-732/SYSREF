package com.sysref.commands.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sysref.commands.resources.Res
import com.sysref.commands.resources.jetbrains_mono_bold
import com.sysref.commands.resources.jetbrains_mono_regular
import org.jetbrains.compose.resources.Font

val TermBg = Color(0xFF000000)
val TermPanel = Color(0xFF071008)
val Phosphor = Color(0xFF3CFF6E)
val PhosphorSoft = Color(0xFFD7FFE0)
val PhosphorDim = Color(0xFF1E7A38)
val PhosphorMuted = Color(0xFF8FCB9C)
val Amber = Color(0xFFFFB000)
val Line = Color(0xFF16331C)

// JetBrains Mono ships as a Compose Multiplatform resource, so Android and desktop use the same files.
val Mono: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(Res.font.jetbrains_mono_bold, FontWeight.Bold),
    )

private val Scheme = darkColorScheme(
    background = TermBg,
    surface = TermBg,
    primary = Phosphor,
    onPrimary = Color.Black,
    secondary = Amber,
    onSecondary = Color.Black,
    onBackground = Phosphor,
    onSurface = Phosphor,
)

@Composable
private fun typography() = Typography(
    bodyLarge = TextStyle(fontFamily = Mono, fontSize = 16.sp, color = Phosphor),
    bodyMedium = TextStyle(fontFamily = Mono, fontSize = 14.sp, color = Phosphor),
    labelLarge = TextStyle(fontFamily = Mono, fontSize = 14.sp, color = Amber),
)

@Composable
fun SysRefTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = typography()) {
        CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides TextStyle(
                fontFamily = Mono,
                color = Phosphor,
                fontSize = 14.sp,
            ),
        ) {
            content()
        }
    }
}
