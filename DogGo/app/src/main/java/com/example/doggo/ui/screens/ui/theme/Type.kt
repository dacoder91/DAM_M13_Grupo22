package com.example.doggo.ui.screens.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.example.doggo.R

val YellowPeach = FontFamily(Font(R.font.yellowpeach))

// Set of Material typography styles to start with
val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = YellowPeach, // Aquí aplicamos la fuente 
        fontSize = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = YellowPeach,
        fontSize = 20.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = YellowPeach,
        fontSize = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp
    )
)