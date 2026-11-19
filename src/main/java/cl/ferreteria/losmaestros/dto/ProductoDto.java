package cl.ferreteria.losmaestros.dto;

import cl.ferreteria.losmaestros.model.Producto;

/** Misma forma que la interfaz Producto del frontend. */
public record ProductoDto(
        String id,
        String nombre,
        String categoriaId,
        String subcategoria,
        String marca,
        String unidad,
        int precio,
        int stock,
        int stockMinimo,
        String descripcion,
        String imagen) {

    public static ProductoDto de(Producto p) {
        return new ProductoDto(
                p.getId(),
                p.getNombre(),
                p.getCategoria().getId(),
                p.getSubcategoria(),
                p.getMarca(),
                p.getUnidad(),
                p.getPrecio(),
                p.getStock(),
                p.getStockMinimo(),
                p.getDescripcion(),
                p.getImagen());
    }
}
