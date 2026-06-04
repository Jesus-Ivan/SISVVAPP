package com.example.sisvvapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sisvvapp.data.local.dao.CajaActivaDao
import com.example.sisvvapp.data.local.dao.GrupoModificadorDao
import com.example.sisvvapp.data.local.dao.ProductoDao
import com.example.sisvvapp.data.local.dao.SocioDao
import com.example.sisvvapp.data.local.dao.TipoPagoDao
import com.example.sisvvapp.data.local.dao.VentaColaDao
import com.example.sisvvapp.data.local.dao.VentaRecibidaDao
import com.example.sisvvapp.data.local.dao.TipoVentaDao
import com.example.sisvvapp.data.local.entity.CajaActivaEntity
import com.example.sisvvapp.data.local.entity.GrupoModificadorEntity
import com.example.sisvvapp.data.local.entity.IntegranteEntity
import com.example.sisvvapp.data.local.entity.ModificadorEntity
import com.example.sisvvapp.data.local.entity.ProductoEntity
import com.example.sisvvapp.data.local.entity.SocioEntity
import com.example.sisvvapp.data.local.entity.TipoPagoEntity
import com.example.sisvvapp.data.local.entity.VentaColaEntity
import com.example.sisvvapp.data.local.entity.VentaRecibidaEntity
import com.example.sisvvapp.data.local.entity.TipoVentaEntity

@Database(
    entities = [
        SocioEntity::class,
        IntegranteEntity::class,
        ProductoEntity::class,
        ModificadorEntity::class,
        GrupoModificadorEntity::class,
        CajaActivaEntity::class,
        VentaColaEntity::class,
        VentaRecibidaEntity::class,
        TipoPagoEntity::class,
        TipoVentaEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun socioDao(): SocioDao
    abstract fun productoDao(): ProductoDao
    abstract fun grupoModificadorDao(): GrupoModificadorDao
    abstract fun cajaActivaDao(): CajaActivaDao
    abstract fun ventaColaDao(): VentaColaDao
    abstract fun ventaRecibidaDao(): VentaRecibidaDao
    abstract fun tipoPagoDao(): TipoPagoDao
    abstract fun tipoVentaDao(): TipoVentaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sisvv_db"
                )
                    // TODO: Reemplazar con migraciones explícitas antes de subir a stores
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
