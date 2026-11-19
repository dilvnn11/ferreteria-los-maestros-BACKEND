package cl.ferreteria.losmaestros.controller;

import cl.ferreteria.losmaestros.dto.CompraDto;
import cl.ferreteria.losmaestros.dto.CompraRequest;
import cl.ferreteria.losmaestros.model.Usuario;
import cl.ferreteria.losmaestros.service.CompraService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService service;

    public CompraController(CompraService service) {
        this.service = service;
    }

    /** Finalizar compra. Pública (invitados incluidos); si viene un token válido la compra queda asociada al usuario. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraDto crear(@RequestBody CompraRequest req, @AuthenticationPrincipal Usuario actual) {
        return service.crear(req, actual);
    }

    @GetMapping("/mias")
    public List<CompraDto> mias(@AuthenticationPrincipal Usuario actual) {
        return service.misCompras(actual);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<CompraDto> todas() {
        return service.todas();
    }
}
