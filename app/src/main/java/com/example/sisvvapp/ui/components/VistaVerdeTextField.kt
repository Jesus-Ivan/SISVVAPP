package com.example.sisvvapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sisvvapp.ui.theme.Inter
import com.example.sisvvapp.ui.theme.SISVVAPPTheme

@Composable
fun VistaVerdeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    bgColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        enabled = enabled,
        placeholder = placeholder?.let { { Text(text = it, fontFamily = Inter) } },
        label = {
            Text(
                text = label,
                fontFamily = Inter
            )
        },
        leadingIcon = leadingIcon,
        isError = isError,
        supportingText = supportingText?.let { { Text(text = it, fontFamily = Inter) } },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            // Fondos
            focusedContainerColor = bgColor,
            unfocusedContainerColor = bgColor,
            disabledContainerColor = bgColor.copy(alpha = 0.4f),

            // Rayitas de abajo (transparentes como en tu diseño)
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            // Colores del texto y etiquetas
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),

            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        ),
        singleLine = singleLine,
        minLines = minLines,

        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        visualTransformation = if(isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            if (isPassword) {
                val image =
                    if(passwordVisible) { Icons.Filled.Visibility }
                    else { Icons.Filled.VisibilityOff }
                IconButton(onClick = { passwordVisible = !passwordVisible }){
                    Icon(imageVector = image, contentDescription = "Ver contraseña")
                }
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = Inter
        )
    )
}

@Preview(showBackground = true)
@Composable
fun TextFieldPreview() {
    SISVVAPPTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            // 1. Input Normal
            VistaVerdeTextField(
                value = "",
                onValueChange = {},
                label = "Nombre"
            )

            // 2. Input Password
            VistaVerdeTextField(
                value = "miclave123",
                onValueChange = {},
                label = "Contraseña",
                isPassword = true
            )

            // 3. Input Bloqueado
            VistaVerdeTextField(
                value = "Juan Pérez",
                onValueChange = {},
                label = "Socio Encontrado",
                enabled = false
            )
        }
    }
}