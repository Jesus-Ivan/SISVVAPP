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
    version = 27,
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

        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Cambiar numComensales de INTEGER a TEXT en ventas_cola
                database.execSQL("ALTER TABLE ventas_cola RENAME TO ventas_cola_old")
                database.execSQL("""
                    CREATE TABLE ventas_cola (
                        idTemporal TEXT NOT NULL PRIMARY KEY,
                        tipoVenta TEXT NOT NULL,
                        idSocio INTEGER,
                        nombreCliente TEXT NOT NULL,
                        corteCaja INTEGER NOT NULL,
                        clavePuntoVenta TEXT NOT NULL,
                        nombreCaja TEXT NOT NULL,
                        productosJson TEXT NOT NULL,
                        fechaCreacion INTEGER NOT NULL,
                        totalVenta REAL NOT NULL,
                        estado TEXT NOT NULL,
                        folioExistente INTEGER,
                        pagosJson TEXT,
                        intentos INTEGER NOT NULL DEFAULT 0,
                        numComensales TEXT
                    )
                """.trimIndent())
                database.execSQL("INSERT INTO ventas_cola SELECT * FROM ventas_cola_old")
                database.execSQL("DROP TABLE ventas_cola_old")

                // Cambiar num_comensales de INTEGER a TEXT en ventas_recibidas
                database.execSQL("ALTER TABLE ventas_recibidas RENAME TO ventas_recibidas_old")
                database.execSQL("""
                    CREATE TABLE ventas_recibidas (
                        folio INTEGER NOT NULL PRIMARY KEY,
                        fecha TEXT NOT NULL,
                        total REAL NOT NULL,
                        estado TEXT NOT NULL,
                        cliente_nombre TEXT,
                        socio_id INTEGER,
                        clave_punto_venta TEXT NOT NULL,
                        corte_caja INTEGER NOT NULL,
                        productos_json TEXT NOT NULL,
                        pagos_json TEXT NOT NULL DEFAULT '[]',
                        num_comensales TEXT,
                        fecha_guardado INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("INSERT INTO ventas_recibidas SELECT * FROM ventas_recibidas_old")
                database.execSQL("DROP TABLE ventas_recibidas_old")

                // Recrear índices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ventas_cola_estado ON ventas_cola (estado)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ventas_cola_folioExistente ON ventas_cola (folioExistente)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ventas_recibidas_corte_caja ON ventas_recibidas (corte_caja)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ventas_recibidas_estado ON ventas_recibidas (estado)")
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
                    .addMigrations(MIGRATION_23_24, MIGRATION_26_27)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
