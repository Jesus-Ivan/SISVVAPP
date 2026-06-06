package com.example.sisvvapp.network

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class NetworkError(val message: String = "Sin conexión a internet") : ApiResult<Nothing>()
    data class ServerError(val code: Int, val message: String) : ApiResult<Nothing>()
    object EmptyData : ApiResult<Nothing>()
}
