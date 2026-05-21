package com.example.sisvvapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SocioEntity::class,
        IntegranteEntity::class,
        ProductoEntity::class,
        GrupoModificadorEntity::class,
        ModificadorEntity::class,
        VentaColaEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun socioDao(): SocioDao
    abstract fun integranteDao(): IntegranteDao
    abstract fun productoDao(): ProductoDao
    abstract fun grupoModificadorDao(): GrupoModificadorDao
    abstract fun modificadorDao(): ModificadorDao
    abstract fun ventaColaDao(): VentaColaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sisvv_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with mock data on first creation
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    prepopulate(database)
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }

        private suspend fun prepopulate(db: AppDatabase) {
            // Socios
            db.socioDao().insertAll(listOf(
                SocioEntity(101, "Alejandro", "Meza", "Gómez", 1254, true,
                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&q=80",
                    "FAM", "MEN", "Familiar"),
                SocioEntity(102, "María José", "Guzmán", "Díaz", 895, false,
                    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80",
                    "IND", "INA", "Individual"),
                SocioEntity(103, "Roberto", "Carlos", "Pérez", 412, true,
                    "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&q=80",
                    "FAM", "CAN", "Familiar"),
                SocioEntity(104, "Claudia", "Ramírez", "Torres", 678, true,
                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&q=80",
                    "FAM", "MEN", "Familiar"),
                SocioEntity(105, "Fernando", "Hernández", "Vega", 330, false,
                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80",
                    "IND", "CAN", "Individual"),
                SocioEntity(106, "Lucía", "Morales", "Fuentes", 987, true,
                    "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&q=80",
                    "ANU", "ANU", "Anual")
            ))
            // Integrantes
            db.integranteDao().insertAll(listOf(
                IntegranteEntity(1, 101, "Sofía", "Meza", "Guzmán", "Hijo/a",
                    "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&q=80"),
                IntegranteEntity(2, 101, "Gabriela", "Guzmán", "López", "Esposa",
                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&q=80"),
                IntegranteEntity(3, 103, "Ana", "Carlos", "Morales", "Hijo/a",
                    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80"),
                IntegranteEntity(4, 104, "Rodrigo", "Ramírez", "Torres", "Hijo/a",
                    "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&q=80")
            ))
            // Productos
            db.productoDao().insertAll(listOf(
                ProductoEntity(201, "Rib Eye 400g", 480.0, 1, "Cocina", 11, "Cortes", true, true,
                    "https://images.unsplash.com/photo-1544025162-d76694265947?w=400&q=80"),
                ProductoEntity(202, "Cerveza Corona", 55.0, 2, "Bebidas", 21, "Cervezas", true, false,
                    "https://images.unsplash.com/photo-1608270586620-248524c67de9?w=400&q=80"),
                ProductoEntity(203, "Limonada Natural", 45.0, 2, "Bebidas", 22, "Refrescos", false, false,
                    "https://images.unsplash.com/photo-1621263764928-df1444c5e859?w=400&q=80"),
                ProductoEntity(204, "Hamburguesa Vista Verde", 185.0, 1, "Cocina", 12, "Hamburguesas", true, true,
                    "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&q=80"),
                ProductoEntity(205, "Pastel de Chocolate", 95.0, 3, "Postres", 31, "Pasteles", true, false,
                    "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400&q=80"),
                ProductoEntity(206, "Agua Mineral", 35.0, 2, "Bebidas", 22, "Refrescos", false, false,
                    "https://images.unsplash.com/photo-1559839914-17aae19cec71?w=400&q=80"),
                ProductoEntity(207, "Ensalada César", 120.0, 1, "Cocina", 13, "Ensaladas", true, false,
                    "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400&q=80"),
                ProductoEntity(208, "Cheesecake Frutos Rojos", 85.0, 3, "Postres", 31, "Pasteles", true, false,
                    "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=400&q=80")
            ))
            // Grupos modificadores del Rib Eye
            db.grupoModificadorDao().insertAll(listOf(
                GrupoModificadorEntity(1, 201, "Término de Cocción", 1, 1, true),
                GrupoModificadorEntity(2, 201, "Guarnición", 1, 1, true),
                GrupoModificadorEntity(3, 204, "Término de Carne", 1, 1, true),
                GrupoModificadorEntity(4, 204, "Extras", 0, 3, false),
                GrupoModificadorEntity(5, 204, "Guarnición", 1, 1, true)
            ))
            // Modificadores del Rib Eye
            db.modificadorDao().insertAll(listOf(
                // Término Rib Eye (grupo 1)
                ModificadorEntity(1, 1, 201, 301, "Término Medio", 0.0, true),
                ModificadorEntity(2, 1, 201, 302, "Tres Cuartos", 0.0, true),
                ModificadorEntity(3, 1, 201, 303, "Bien Cocido", 0.0, true),
                // Guarnición Rib Eye (grupo 2)
                ModificadorEntity(4, 2, 201, 304, "Papas Fritas", 30.0, true),
                ModificadorEntity(5, 2, 201, 305, "Ensalada", 0.0, true),
                // Término Hamburguesa (grupo 3)
                ModificadorEntity(6, 3, 204, 306, "Término Medio", 0.0, true),
                ModificadorEntity(7, 3, 204, 307, "Tres Cuartos", 0.0, true),
                ModificadorEntity(8, 3, 204, 308, "Bien Cocido", 0.0, true),
                // Extras Hamburguesa (grupo 4)
                ModificadorEntity(9, 4, 204, 309, "Queso Extra", 15.0, true),
                ModificadorEntity(10, 4, 204, 310, "Tocino", 20.0, true),
                ModificadorEntity(11, 4, 204, 311, "Aguacate", 25.0, true),
                // Guarnición Hamburguesa (grupo 5)
                ModificadorEntity(12, 5, 204, 312, "Papas Fritas", 0.0, true),
                ModificadorEntity(13, 5, 204, 313, "Ensalada", 0.0, true),
                ModificadorEntity(14, 5, 204, 314, "Aros de Cebolla", 10.0, true)
            ))
        }
    }
}
