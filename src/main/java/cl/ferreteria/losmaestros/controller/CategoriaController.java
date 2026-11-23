package cl.ferreteria.losmaestros.controller;

import cl.ferreteria.losmaestros.dto.CategoriaDto;
import cl.ferreteria.losmaestros.dto.CategoriaRequest;
import cl.ferreteria.losmaestros.service.CategoriaService;
import java.util.List;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Categorías", description = "Clasificación del catálogo")
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaDto> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CategoriaDto crear(@RequestBody CategoriaRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CategoriaDto actualizar(@PathVariable String id, @RequestBody CategoriaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
