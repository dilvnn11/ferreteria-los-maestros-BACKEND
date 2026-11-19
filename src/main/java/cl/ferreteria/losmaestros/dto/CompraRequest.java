package cl.ferreteria.losmaestros.dto;

import java.util.List;

public record CompraRequest(List<ItemCompra> items) {
    /** Igual que ItemCarrito del frontend. */
    public record ItemCompra(String productoId, Integer cantidad) {}
}
