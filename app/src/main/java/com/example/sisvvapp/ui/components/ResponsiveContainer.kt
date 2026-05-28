package com.example.sisvvapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType

@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val deviceType = LocalDeviceType.current
    val isTablet = deviceType == DeviceType.TABLET

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .widthIn(max = if (isTablet) 650.dp else 400.dp)
                .fillMaxWidth()
                .padding(horizontal = if (isTablet) 24.dp else 16.dp),
            content = content
        )
    }
}