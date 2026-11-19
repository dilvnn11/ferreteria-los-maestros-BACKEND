package cl.ferreteria.losmaestros.dto;

import cl.ferreteria.losmaestros.model.Rol;

/**
 * Se usa en registro, creación, edición (admin) y perfil.
 * Los campos que no aplican a cada operación se ignoran (p. ej. "rol" en el registro y en el perfil).
 * fechaNacimiento llega como texto AAAA-MM-DD para poder responder con un mensaje de validación propio.
 */
public record UsuarioRequest(
        String nombre,
        String apellido,
        String fechaNacimiento,
        String correo,
        String password,
        String direccion,
        String region,
        String genero,
        Rol rol) {}
