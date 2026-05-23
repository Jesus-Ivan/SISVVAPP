package com.example.sisvvapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.ui.theme.Inter
import com.example.sisvvapp.ui.theme.*


@Composable
fun VistaVerdeSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Inter,
            color = TextoPrincipalClaro
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = VerdePrincipal.copy(alpha = 0.5f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VistaVerdeSectionHeaderPreview() {
    SISVVAPPTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                VistaVerdeSectionHeader(text = "Ventas del día")
            }
        }
    }
}