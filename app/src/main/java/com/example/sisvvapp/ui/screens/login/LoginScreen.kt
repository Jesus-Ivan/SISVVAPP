package com.example.sisvvapp.ui.screens.login

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sisvvapp.R
import com.example.sisvvapp.ui.components.VistaVerdeButton
import com.example.sisvvapp.ui.components.VistaVerdeTextField
import com.example.sisvvapp.ui.state.SisvvViewModel
import com.example.sisvvapp.ui.theme.Inter
import com.example.sisvvapp.ui.theme.Poppins
import com.example.sisvvapp.ui.theme.SISVVAPPTheme
import com.example.sisvvapp.ui.viewmodel.SisvvViewModelFactory

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val viewModel: SisvvViewModel = viewModel(
        factory = SisvvViewModelFactory(context)
    )

    LaunchedEffect(viewModel.loginSuccess) {
        if (viewModel.loginSuccess) onLoginSuccess()
    }

    val inputModifier = Modifier
        .shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            clip = false
        )
        .fillMaxWidth()

    // Obtenemos los strings de validación aquí para usarlos dentro del onClick
    val errorEmptyFields = stringResource(id = R.string.error_empty_fields)
    val errorInvalidEmail = stringResource(id = R.string.error_invalid_email)
    val errorShortPassword = stringResource(id = R.string.error_short_password)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .wrapContentWidth(Alignment.CenterHorizontally),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 450.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOGO
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.login_logo_desc),
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(bottom = 50.dp)
            )
            // TITULO PRINCIPAL
            Text(
                text = stringResource(id = R.string.login_title),
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.login_subtitle),
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            // INPUT EMAIL
            VistaVerdeTextField(
                value = email,
                onValueChange = { email = it; localError = "" },
                label = stringResource(id = R.string.login_email_label),
                keyboardType = KeyboardType.Email,
                modifier = inputModifier,
                bgColor = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // INPUT PASSWORD
            VistaVerdeTextField(
                value = password,
                onValueChange = { password = it; localError = "" },
                label = stringResource(id = R.string.login_password_label),
                isPassword = true,
                modifier = inputModifier,
                bgColor = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height((32.dp)))

            val errorMsg = localError.ifEmpty {
                viewModel.loginError ?: viewModel.networkError ?: ""
            }
            if (errorMsg.isNotEmpty()) {
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // BOTÓN
            VistaVerdeButton(
                text = stringResource(id = R.string.login_button),
                onClick = {
                    localError = when {
                        email.isBlank() || password.isBlank() -> errorEmptyFields
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errorInvalidEmail
                        password.length < 6 -> errorShortPassword
                        else -> {
                            viewModel.login(email, password)
                            ""
                        }
                    }
                }
            )
        }
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