package cl.ferreteria.losmaestros.controller;

import cl.ferreteria.losmaestros.dto.UsuarioDto;
import cl.ferreteria.losmaestros.dto.UsuarioRequest;
import cl.ferreteria.losmaestros.model.Usuario;
import cl.ferreteria.losmaestros.service.UsuarioService;
import java.util.List;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Usuarios", description = "Administración de usuarios y roles")
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioDto> listar() {
        return service.listar();
    }

    /** Administrador crea cualquier rol; vendedor solo clientes. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public UsuarioDto crear(@RequestBody UsuarioRequest req, @AuthenticationPrincipal Usuario actual) {
        return service.crear(req, actual);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDto actualizar(
            @PathVariable String id, @RequestBody UsuarioRequest req, @AuthenticationPrincipal Usuario actual) {
        return service.actualizar(id, req, actual);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable String id, @AuthenticationPrincipal Usuario actual) {
        service.eliminar(id, actual);
    }

    @PostMapping("/{id}/desbloquear")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDto desbloquear(@PathVariable String id) {
        return service.desbloquear(id);
    }
}
