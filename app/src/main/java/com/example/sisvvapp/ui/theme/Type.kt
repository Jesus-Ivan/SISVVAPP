package com.example.sisvvapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R

// 1. AGRUPAMOS LAS FUENTES EN FAMILIAS
val Poppins = FontFamily(
    Font(R.font.poppinsbold, FontWeight.Bold)
)

val Inter = FontFamily(
    Font(R.font.interregular, FontWeight.Normal),
    Font(R.font.intermedium, FontWeight.Medium)
)

val Typography = Typography(

    // Títulos grandes
    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),

    // Textos normales, descripciones, campos de texto
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    // Botones, Badges, Etiquetas
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)