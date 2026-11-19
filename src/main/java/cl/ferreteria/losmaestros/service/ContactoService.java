package cl.ferreteria.losmaestros.service;

import cl.ferreteria.losmaestros.dto.ContactoRequest;
import cl.ferreteria.losmaestros.dto.MensajeContactoDto;
import cl.ferreteria.losmaestros.model.MensajeContacto;
import cl.ferreteria.losmaestros.repository.MensajeContactoRepository;
import cl.ferreteria.losmaestros.util.Validaciones;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContactoService {

    private final MensajeContactoRepository mensajes;

    public ContactoService(MensajeContactoRepository mensajes) {
        this.mensajes = mensajes;
    }

    public MensajeContactoDto guardar(ContactoRequest r) {
        Validaciones.contacto(r);
        MensajeContacto m = new MensajeContacto();
        m.setNombre(r.nombre().trim());
        m.setCorreo(r.correo().trim());
        m.setMensaje(r.mensaje().trim());
        m.setFecha(Instant.now());
        return MensajeContactoDto.de(mensajes.save(m));
    }

    public List<MensajeContactoDto> listar() {
        return mensajes.findAllByOrderByFechaDesc().stream().map(MensajeContactoDto::de).toList();
    }
}
