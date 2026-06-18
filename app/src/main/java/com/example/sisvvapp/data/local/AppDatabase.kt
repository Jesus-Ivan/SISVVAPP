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
import com.example.sisvvapp.data.local.dao.VentaGlobalDao
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.sisvvapp.data.local.view.VentaGlobalView

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
    views = [VentaGlobalView::class],
    version = 24,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun socioDao(): SocioDao
    abstract fun productoDao(): ProductoDao
    abstract fun grupoModificadorDao(): GrupoModificadorDao
    abstract fun cajaActivaDao(): CajaActivaDao
    abstract fun ventaColaDao(): VentaColaDao
    abstract fun ventaRecibidaDao(): VentaRecibidaDao
    abstract fun ventaGlobalDao(): VentaGlobalDao
    abstract fun tipoPagoDao(): TipoPagoDao
    abstract fun tipoVentaDao(): TipoVentaDao

    companion object {
        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ventas_cola_estado ON ventas_cola (estado)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ventas_cola_folioExistente ON ventas_cola (folioExistente)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ventas_recibidas_corte_caja ON ventas_recibidas (corte_caja)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ventas_recibidas_estado ON ventas_recibidas (estado)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_productos_descripcion ON productos (descripcion)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cajas_activas_activo ON cajas_activas (activo)")
                database.execSQL("ALTER TABLE ventas_cola ADD COLUMN intentos INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sisvv_db"
                )
                    .addMigrations(MIGRATION_23_24)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
