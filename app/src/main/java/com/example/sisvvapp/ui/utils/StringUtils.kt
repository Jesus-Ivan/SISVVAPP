package com.example.sisvvapp.ui.utils

// Esto limpia espacios dobles y pone todo en mayúsculas
fun String.normalizeName(): String = this.uppercase().replace(Regex("\\s+"), " ").trim()