package cl.ferreteria.losmaestros.controller;

import cl.ferreteria.losmaestros.dto.ProductoDto;
import cl.ferreteria.losmaestros.dto.ProductoRequest;
import cl.ferreteria.losmaestros.dto.StockRequest;
import cl.ferreteria.losmaestros.service.ProductoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Productos", description = "Catálogo y stock")
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    /** Público. Filtros opcionales: ?texto=cemento&categoriaId=cat-pinturas&precioMax=10000 */
    @GetMapping
    public List<ProductoDto> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String categoriaId,
            @RequestParam(required = false) Integer precioMax) {
        return service.listar(texto, categoriaId, precioMax);
    }

    @GetMapping("/{id}")
    public ProductoDto obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ProductoDto crear(@RequestBody ProductoRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ProductoDto actualizar(@PathVariable String id, @RequestBody ProductoRequest req) {
        return service.actualizar(id, req);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public ProductoDto actualizarStock(@PathVariable String id, @RequestBody StockRequest req) {
        return service.actualizarStock(id, req.stock());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
