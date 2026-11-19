package cl.ferreteria.losmaestros.dto;

import cl.ferreteria.losmaestros.model.Rol;
import cl.ferreteria.losmaestros.model.Usuario;
import java.time.Instant;
import java.time.LocalDate;

/** Usuario sin contraseña (equivale a UsuarioPublico del frontend) + indicador de bloqueo. */
public record UsuarioDto(
        String id,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        String correo,
        String direccion,
        String region,
        String genero,
        Rol rol,
        Instant fechaRegistro,
        boolean bloqueado) {

    public static final int MAX_INTENTOS = 3;

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(
                u.getId(),
                u.getNombre(),
                u.getApellido(),
                u.getFechaNacimiento(),
                u.getCorreo(),
                u.getDireccion(),
                u.getRegion(),
                u.getGenero(),
                u.getRol(),
                u.getFechaRegistro(),
                u.getIntentosFallidos() >= MAX_INTENTOS);
    }
}
