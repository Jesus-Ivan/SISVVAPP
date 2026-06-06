package com.example.sisvvapp.network.exceptions

import java.io.IOException

class ServerUnreachableException(message: String = "El servidor no responde o es inalcanzable") : IOException(message)
