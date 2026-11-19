package cl.ferreteria.losmaestros.service;

import cl.ferreteria.losmaestros.dto.CategoriaDto;
import cl.ferreteria.losmaestros.dto.CategoriaRequest;
import cl.ferreteria.losmaestros.exception.ApiException;
import cl.ferreteria.losmaestros.exception.ValidacionException;
import cl.ferreteria.losmaestros.model.Categoria;
import cl.ferreteria.losmaestros.repository.CategoriaRepository;
import cl.ferreteria.losmaestros.repository.ProductoRepository;
import cl.ferreteria.losmaestros.util.Validaciones;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private static final String NOMBRE_REPETIDO = "Ya existe una categoría con ese nombre.";

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    public CategoriaService(CategoriaRepository categorias, ProductoRepository productos) {
        this.categorias = categorias;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listar() {
        return categorias.findAll().stream().map(CategoriaDto::de).toList();
    }

    @Transactional
    public CategoriaDto crear(CategoriaRequest r) {
        String nombre = validarNombre(r);
        if (categorias.existsByNombreIgnoreCase(nombre)) throw new ValidacionException(Map.of("nombre", NOMBRE_REPETIDO));
        return CategoriaDto.de(categorias.save(new Categoria("cat-" + System.currentTimeMillis(), nombre)));
    }

    @Transactional
    public CategoriaDto actualizar(String id, CategoriaRequest r) {
        String nombre = validarNombre(r);
        Categoria c = buscar(id);
        if (categorias.existsByNombreIgnoreCaseAndIdNot(nombre, id))
            throw new ValidacionException(Map.of("nombre", NOMBRE_REPETIDO));
        // Los productos referencian la categoría por id: el cambio se refleja en todos
        c.setNombre(nombre);
        return CategoriaDto.de(categorias.save(c));
    }

    @Transactional
    public void eliminar(String id) {
        Categoria c = buscar(id);
        long enUso = productos.countByCategoriaId(id);
        if (enUso > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar: hay " + enUso + " producto(s) en esta categoría. Muévelos o elimínalos primero.");
        }
        categorias.delete(c);
    }

    private Categoria buscar(String id) {
        return categorias.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La categoría ya no existe."));
    }

    private String validarNombre(CategoriaRequest r) {
        String error = Validaciones.nombreCategoria(r.nombre());
        if (error != null) throw new ValidacionException(Map.of("nombre", error));
        return r.nombre().trim();
    }
}
