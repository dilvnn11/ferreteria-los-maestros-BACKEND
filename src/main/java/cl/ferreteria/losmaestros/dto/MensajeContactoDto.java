package cl.ferreteria.losmaestros.dto;

import cl.ferreteria.losmaestros.model.MensajeContacto;
import java.time.Instant;

public record MensajeContactoDto(Long id, String nombre, String correo, String mensaje, Instant fecha) {
    public static MensajeContactoDto de(MensajeContacto m) {
        return new MensajeContactoDto(m.getId(), m.getNombre(), m.getCorreo(), m.getMensaje(), m.getFecha());
    }
}
