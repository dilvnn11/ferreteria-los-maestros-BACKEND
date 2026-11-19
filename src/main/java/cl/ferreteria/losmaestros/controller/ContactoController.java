package cl.ferreteria.losmaestros.controller;

import cl.ferreteria.losmaestros.dto.ContactoRequest;
import cl.ferreteria.losmaestros.dto.MensajeContactoDto;
import cl.ferreteria.losmaestros.service.ContactoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacto")
public class ContactoController {

    private final ContactoService service;

    public ContactoController(ContactoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeContactoDto enviar(@RequestBody ContactoRequest req) {
        return service.guardar(req);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<MensajeContactoDto> listar() {
        return service.listar();
    }
}
