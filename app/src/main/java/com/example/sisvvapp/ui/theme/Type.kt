package com.example.sisvvapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R


val Poppins = FontFamily(
    Font(R.font.poppinsbold, FontWeight.Bold),
            Font(R.font.poppinssemibold, FontWeight.SemiBold)
)

val Inter = FontFamily(
    Font(R.font.interregular, FontWeight.Normal),
    Font(R.font.intermedium, FontWeight.Medium),
    Font(R.font.intersemibold, FontWeight.SemiBold)
)

val Typography = Typography(

    // Títulos grandes
    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),

    // Títulos medios
    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),

    // Títulos pequeños
    titleSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),

    // Textos normales, descripciones, campos de texto
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    // Texto medio
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),

    // Texto pequeño
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),

    // Botones, Badges, Etiquetas
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),

    // Etiquetas medianas
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),

    // Etiquetas pequeñas
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
    )
)