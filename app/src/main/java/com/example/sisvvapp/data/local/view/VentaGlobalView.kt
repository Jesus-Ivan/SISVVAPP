package com.example.sisvvapp.data.local.view

import androidx.room.DatabaseView

@DatabaseView(
    viewName = "v_ventas_global",
    value = """
        SELECT 
            folio as folio,
            cliente_nombre as cliente,
            total as total,
            fecha as fecha,
            fecha_guardado as timestamp,
            'RECIBIDA' as syncStatus,
            NULL as idTemporal,
            corte_caja as corteCaja,
            estado as estadoVenta,
            socio_id as socioId,
            SUBSTR(fecha, 1, 10) as fechaFiltro,
            productos_json as productosJson
        FROM ventas_recibidas
        WHERE folio NOT IN (SELECT folioExistente FROM ventas_cola WHERE folioExistente > 0 AND (estado = 'PENDIENTE' OR estado = 'SYNCING'))
        UNION ALL
        SELECT 
            COALESCE(folioExistente, 0) as folio,
            nombreCliente as cliente,
            (totalVenta + COALESCE((SELECT total FROM ventas_recibidas WHERE folio = folioExistente AND folioExistente > 0), 0.0)) as total,
            CAST(fechaCreacion AS TEXT) as fecha,
            fechaCreacion as timestamp,
            estado as syncStatus,
            idTemporal as idTemporal,
            corteCaja as corteCaja,
            'Abierta' as estadoVenta,
            idSocio as socioId,
            COALESCE((SELECT SUBSTR(fecha, 1, 10) FROM ventas_recibidas WHERE folio = folioExistente AND folioExistente > 0), strftime('%Y-%m-%d', date(fechaCreacion/1000, 'unixepoch', 'localtime'))) as fechaFiltro,
            productosJson as productosJson
        FROM ventas_cola
    """
)
data class VentaGlobalView(
    val folio: Int?,
    val cliente: String?,
    val total: Double,
    val fecha: String,
    val timestamp: Long,
    val syncStatus: String,
    val idTemporal: String?,
    val corteCaja: Int,
    val estadoVenta: String,
    val socioId: Int?,
    val fechaFiltro: String,
    val productosJson: String
)
