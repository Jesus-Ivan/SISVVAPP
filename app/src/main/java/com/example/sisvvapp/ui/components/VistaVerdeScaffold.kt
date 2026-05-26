package com.example.sisvvapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VistaVerdeScaffold(
    title: String,
    onMenuClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    isOnline: Boolean = true,
    isBackButton: Boolean = false,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            VistaVerdeTopBar(
                title = title,
                onMenuClick = onMenuClick,
                actions = actions,
                isBackButton = isBackButton
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            VistaVerdeConnectivityBanner(isOnline = isOnline)
            content()
        }
    }
}
