package com.example.sisvvapp.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvvapp.R
import com.example.sisvvapp.ui.components.VistaVerdeButton
import com.example.sisvvapp.ui.components.VistaVerdeTextField
import com.example.sisvvapp.ui.theme.Inter
import com.example.sisvvapp.ui.theme.Poppins
import com.example.sisvvapp.ui.theme.SISVVAPPTheme


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val inputModifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
        .fillMaxWidth()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
      //LOGO
        Image(
            painter = painterResource(id= R.drawable.logo),
            contentDescription = "Logo Vista Verde",
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(bottom=50.dp)
        )
        //TITULO PRINCIPAL
        Text(
            text = "Iniciar Sesión",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Ingresa tu correo y contraseña para acceder",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        //INPUT EMAIL
        VistaVerdeTextField(
            value = email,
            onValueChange = {email = it},
            label = "Email",
            keyboardType = KeyboardType.Email,
            modifier = inputModifier
        )
        Spacer(modifier = Modifier.height(16.dp))

        //INPUT PASSWORD
        VistaVerdeTextField(
            value = password,
            onValueChange = {password = it},
            label = "Password",
            isPassword = true,
            modifier = inputModifier
        )
        Spacer(modifier = Modifier.height((32.dp)))

        //BOTÓN
        VistaVerdeButton(
            text = "INGRESAR",
            onClick = {
                // Lógica para Laravel / Auth irá aquí
                onLoginSuccess()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    SISVVAPPTheme {
        LoginScreen(
            onLoginSuccess = {}
        )
    }
}
