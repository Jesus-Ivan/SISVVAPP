package com.example.sisvvapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromMembresiaList(value: List<com.example.sisvvapp.network.dto.socios.MembresiaDto>?): String? = gson.toJson(value)

    @TypeConverter
    fun toMembresiaList(value: String?): List<com.example.sisvvapp.network.dto.socios.MembresiaDto>? {
        if (value == null) return null
        val type = object : TypeToken<List<com.example.sisvvapp.network.dto.socios.MembresiaDto>>() {}.type
        return gson.fromJson(value, type)
    }
}
