package cl.ferreteria.losmaestros.dto;

import cl.ferreteria.losmaestros.model.Compra;
import java.time.Instant;
import java.util.List;

public record CompraDto(Long id, Instant fecha, int total, String usuarioId, List<DetalleDto> detalles) {

    public record DetalleDto(String productoId, String productoNombre, int precioUnitario, int cantidad, int subtotal) {}

    public static CompraDto de(Compra c) {
        return new CompraDto(
                c.getId(),
                c.getFecha(),
                c.getTotal(),
                c.getUsuario() != null ? c.getUsuario().getId() : null,
                c.getDetalles().stream()
                        .map(d -> new DetalleDto(
                                d.getProductoId(), d.getProductoNombre(), d.getPrecioUnitario(), d.getCantidad(), d.getSubtotal()))
                        .toList());
    }
}
