package com.example.sisvvapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.ui.navigation.ScreenRoutes
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.*

data class DrawerItem(val route: String, val title: String, val icon: ImageVector)

@Composable
fun AppNavigationDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    viewModel: SisvvViewModel? = null
) {
    val items = listOf(
        DrawerItem(ScreenRoutes.VENTAS, stringResource(R.string.menu_ventas), Icons.Default.ShoppingCart),
        DrawerItem(ScreenRoutes.SOCIOS, stringResource(R.string.menu_socios), Icons.Default.Person),
        DrawerItem(ScreenRoutes.CAJA, stringResource(R.string.menu_caja), Icons.Default.AccountBalanceWallet),
        DrawerItem(ScreenRoutes.AJUSTES, stringResource(R.string.menu_ajustes), Icons.Default.Settings)
    )

    ModalDrawerSheet(
        drawerContainerColor = FondoAppClaro,
        modifier = Modifier
            .width(300.dp)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = TextStyle(
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.weight(1f)) {
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                item.icon, contentDescription = null,
                                tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = TextStyle(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            )
                        },
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route); onCloseDrawer() },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // --- SECCIÓN DE TEMA AL FINAL ---
            if (viewModel != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Apariencia",
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val themeMode = viewModel.themeMode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionSmall(
                        icon = Icons.Default.AutoMode,
                        selected = themeMode == 0,
                        onClick = { viewModel.updateThemeMode(0) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionSmall(
                        icon = Icons.Default.LightMode,
                        selected = themeMode == 1,
                        onClick = { viewModel.updateThemeMode(1) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionSmall(
                        icon = Icons.Default.DarkMode,
                        selected = themeMode == 2,
                        onClick = { viewModel.updateThemeMode(2) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOptionSmall(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = modifier.height(40.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DrawerPreview() {
    SISVVAPPTheme {
        Surface {
            AppNavigationDrawerContent(
                currentRoute = ScreenRoutes.SOCIOS,
                onNavigate = {},
                onCloseDrawer = {}
            )
        }
    }
}
