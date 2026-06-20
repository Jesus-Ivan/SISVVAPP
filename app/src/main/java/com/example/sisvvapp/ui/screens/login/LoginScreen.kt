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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.example.sisvvapp.ui.utils.DeviceType
import com.example.sisvvapp.ui.utils.LocalDeviceType

@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    serverError: String? = null,
    onLoginClick: (String, String) -> Unit = { _, _ -> }
) {
    val isTablet = LocalDeviceType.current == DeviceType.TABLET
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current


    val inputModifier = Modifier
        .shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            clip = false
        )
        .fillMaxWidth()

    val errorEmptyFields = stringResource(id = R.string.error_empty_fields)
    val errorInvalidEmail = stringResource(id = R.string.error_invalid_email)
    val errorShortPassword = stringResource(id = R.string.error_short_password)

    val attemptLogin = {
        localError = when {
            email.isBlank() || password.isBlank() -> errorEmptyFields
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errorInvalidEmail
            password.length < 8 -> errorShortPassword
            else -> {
                onLoginClick(email, password)
                ""
            }
        }
    }

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
                .widthIn(max = if (isTablet) 520.dp else 450.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOGO
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.login_logo_desc),
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(bottom = if (isTablet) 64.dp else 50.dp)
            )
            // TITULO PRINCIPAL
            Text(
                text = stringResource(id = R.string.login_title),
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isTablet) 40.sp else 32.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = if (isTablet) 12.dp else 8.dp)
            )
            Text(
                text = stringResource(id = R.string.login_subtitle),
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = if (isTablet) 18.sp else 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = if (isTablet) 48.dp else 32.dp)
            )
            // INPUT EMAIL
            VistaVerdeTextField(
                value = email,
                onValueChange = { email = it; localError = "" },
                label = stringResource(id = R.string.login_email_label),
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = inputModifier,
                bgColor = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 16.dp))

            // INPUT PASSWORD
            VistaVerdeTextField(
                value = password,
                onValueChange = { password = it; localError = "" },
                label = stringResource(id = R.string.login_password_label),
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        attemptLogin() 
                    }
                ),
                modifier = inputModifier,
                bgColor = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(if (isTablet) 48.dp else 32.dp))

            val errorMsg = localError.ifEmpty { serverError ?: "" }

            if (errorMsg.isNotEmpty()) {
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    modifier = Modifier.padding(bottom = if (isTablet) 24.dp else 16.dp)
                )
            }

            // BOTÓN
            VistaVerdeButton(
                text = stringResource(id = R.string.login_button),
                enabled = !isLoading,
                onClick = attemptLogin
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    SISVVAPPTheme {
        LoginScreen(
            onLoginClick = { _, _ -> }
        )
    }
}