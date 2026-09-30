package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class WallpaperTheme(
    val id: String,
    val name: String,
    val description: String,
    val primaryAccent: Color,
    val previewColors: List<Color>,
    val gradientBrush: Brush
)

object WallpaperRegistry {

    val CyberPulse = WallpaperTheme(
        id = "cyber_pulse",
        name = "Cyber Pulse",
        description = "Sleek cyan & navy tech matrix glow",
        primaryAccent = CyanPrimary,
        previewColors = listOf(Color(0xFF06B6D4), Color(0xFF0B192C), Color(0xFF060913)),
        gradientBrush = Brush.verticalGradient(
            listOf(
                Color(0xFF0B1E36),
                Color(0xFF081220),
                Color(0xFF05080E)
            )
        )
    )

    val AmoledBlack = WallpaperTheme(
        id = "amoled_black",
        name = "AMOLED Pitch Black",
        description = "Pure black background for maximum battery life",
        primaryAccent = Color(0xFFE2E8F0),
        previewColors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF000000)),
        gradientBrush = Brush.verticalGradient(
            listOf(
                Color(0xFF050505),
                Color(0xFF000000),
                Color(0xFF000000)
            )
        )
    )

    val ElectricSapphire = WallpaperTheme(
        id = "electric_sapphire",
        name = "Electric Sapphire",
        description = "Deep cosmic cobalt blue and starlight indigo",
        primaryAccent = Color(0xFF38BDF8),
        previewColors = listOf(Color(0xFF0284C7), Color(0xFF1E3A8A), Color(0xFF0A0F24)),
        gradientBrush = Brush.verticalGradient(
            listOf(
                Color(0xFF1E3A8A),
                Color(0xFF0F172A),
                Color(0xFF030712)
            )
        )
    )

    val EmeraldMatrix = WallpaperTheme(
        id = "emerald_matrix",
        name = "Emerald Terminal",
        description = "Cybernetic hacker green and deep forest shade",
        primaryAccent = AccentGreen,
        previewColors = listOf(Color(0xFF10B981), Color(0xFF064E3B), Color(0xFF021B13)),
        gradientBrush = Brush.verticalGradient(
            listOf(
                Color(0xFF064E3B),
                Color(0xFF022C22),
                Color(0xFF02110D)
            )
        )
    )

    val SunsetAurora = WallpaperTheme(
        id = "sunset_aurora",
        name = "Sunset Aurora",
        description = "Twilight magenta, neon purple & warm amber",
        primaryAccent = Color(0xFFF472B6),
        previewColors = listOf(Color(0xFFD946EF), Color(0xFF581C87), Color(0xFF1E0B36)),
        gradientBrush = Brush.verticalGradient(
            listOf(
                Color(0xFF4C1D95),
                Color(0xFF3B0764),
                Color(0xFF0F041C)
            )
        )
    )

    val TitaniumStealth = WallpaperTheme(
        id = "titanium_stealth",
        name = "Titanium Stealth",
        description = "Industrial slate grey and brushed graphite",
        primaryAccent = Color(0xFF94A3B8),
        previewColors = listOf(Color(0xFF64748B), Color(0xFF1E293B), Color(0xFF0A0E17)),
        gradientBrush = Brush.verticalGradient(
            listOf(
                Color(0xFF1E293B),
                Color(0xFF0F172A),
                Color(0xFF080C14)
            )
        )
    )

    val CrimsonOverdrive = WallpaperTheme(
        id = "crimson_overdrive",
        name = "Crimson Overdrive",
        description = "Aggressive ruby laser red and deep shadow",
        primaryAccent = AccentRed,
        previewColors = listOf(Color(0xFFEF4444), Color(0xFF7F1D1D), Color(0xFF1C0407)),
        gradientBrush = Brush.verticalGradient(
            listOf(
                Color(0xFF450A0A),
                Color(0xFF200508),
                Color(0xFF0A0204)
            )
        )
    )

    val allWallpapers = listOf(
        CyberPulse,
        AmoledBlack,
        ElectricSapphire,
        EmeraldMatrix,
        SunsetAurora,
        TitaniumStealth,
        CrimsonOverdrive
    )

    fun getWallpaperById(id: String): WallpaperTheme {
        return allWallpapers.find { it.id == id } ?: CyberPulse
    }
}
