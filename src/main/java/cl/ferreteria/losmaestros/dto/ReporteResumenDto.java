package cl.ferreteria.losmaestros.dto;

import java.util.List;

/** Indicadores del panel administrativo calculados desde la base de datos. */
public record ReporteResumenDto(
        long totalOperaciones,
        long montoTotalVendido,
        long unidadesVendidas,
        List<ProductoTopDto> productosMasSolicitados,
        List<PeriodoDto> porPeriodo) {

    public record ProductoTopDto(String productoId, String productoNombre, long cantidadVendida, long monto) {}

    public record PeriodoDto(String etiqueta, long operaciones, long monto) {}
}
