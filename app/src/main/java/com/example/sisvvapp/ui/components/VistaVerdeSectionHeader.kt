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
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType

@Composable
fun VistaVerdeSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = text,
            style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = if (isTablet) 0.5.sp else 0.sp
        )
        Spacer(modifier = Modifier.height(if (isTablet) 8.dp else 6.dp))
        HorizontalDivider(
            thickness = if (isTablet) 3.dp else 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
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
