package cl.ferreteria.losmaestros.dto;

import cl.ferreteria.losmaestros.model.Categoria;

public record CategoriaDto(String id, String nombre) {
    public static CategoriaDto de(Categoria c) {
        return new CategoriaDto(c.getId(), c.getNombre());
    }
}
