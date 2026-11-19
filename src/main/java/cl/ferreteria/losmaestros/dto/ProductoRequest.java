package cl.ferreteria.losmaestros.dto;

/** "id" solo se usa al crear; al editar el código no se puede cambiar. */
public record ProductoRequest(
        String id,
        String nombre,
        String categoriaId,
        String subcategoria,
        String marca,
        String unidad,
        Integer precio,
        Integer stock,
        Integer stockMinimo,
        String descripcion,
        String imagen) {}
