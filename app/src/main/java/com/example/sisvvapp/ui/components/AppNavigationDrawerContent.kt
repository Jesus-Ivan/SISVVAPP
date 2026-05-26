package com.example.sisvvapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.sisvvapp.ui.theme.*

data class DrawerItem(val route: String, val title: String, val icon: ImageVector)

@Composable
fun AppNavigationDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
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
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = TextStyle(
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    color = VerdePrincipal
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(color = EcoDivider)
            Spacer(modifier = Modifier.height(24.dp))

            items.forEach { item ->
                NavigationDrawerItem(
                    icon = {
                        Icon(item.icon, contentDescription = null,
                            tint = if (currentRoute == item.route) VerdePrincipal else TextoSecundarioClaro)
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
                        selectedContainerColor = EstadoExitoClaro,
                        selectedTextColor = VerdePrincipal,
                        unselectedTextColor = TextoPrincipalClaro
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
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