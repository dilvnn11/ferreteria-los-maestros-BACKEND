package cl.ferreteria.losmaestros.service;

import cl.ferreteria.losmaestros.dto.ProductoDto;
import cl.ferreteria.losmaestros.dto.ProductoRequest;
import cl.ferreteria.losmaestros.exception.ApiException;
import cl.ferreteria.losmaestros.exception.ValidacionException;
import cl.ferreteria.losmaestros.model.Categoria;
import cl.ferreteria.losmaestros.model.Producto;
import cl.ferreteria.losmaestros.repository.CategoriaRepository;
import cl.ferreteria.losmaestros.repository.ProductoRepository;
import cl.ferreteria.losmaestros.util.Validaciones;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    /** Mismo filtro que frontend/src/utils/filtros.ts: texto (nombre, código o marca), categoría y precio máximo. */
    @Transactional(readOnly = true)
    public List<ProductoDto> listar(String texto, String categoriaId, Integer precioMax) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        boolean todasLasCategorias = categoriaId == null || categoriaId.isBlank() || categoriaId.equals("todas");

        return productos.findAll().stream()
                .sorted(Comparator.comparing(Producto::getId))
                .filter(p -> t.isEmpty()
                        || p.getNombre().toLowerCase().contains(t)
                        || p.getId().toLowerCase().contains(t)
                        || p.getMarca().toLowerCase().contains(t))
                .filter(p -> todasLasCategorias || p.getCategoria().getId().equals(categoriaId))
                .filter(p -> precioMax == null || p.getPrecio() <= precioMax)
                .map(ProductoDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoDto obtener(String id) {
        return ProductoDto.de(buscar(id));
    }

    @Transactional
    public ProductoDto crear(ProductoRequest r) {
        Validaciones.producto(r, true);
        String codigo = r.id().trim();
        if (productos.existsByIdIgnoreCase(codigo)) {
            throw new ValidacionException(Map.of("id", "Ya existe un producto con ese código."));
        }
        Producto p = new Producto();
        p.setId(codigo);
        copiar(p, r);
        return ProductoDto.de(productos.save(p));
    }

    /** El código no se puede cambiar. */
    @Transactional
    public ProductoDto actualizar(String id, ProductoRequest r) {
        Validaciones.producto(r, false);
        Producto p = buscar(id);
        copiar(p, r);
        return ProductoDto.de(productos.save(p));
    }

    /** Administrador y vendedor pueden ajustar el stock. */
    @Transactional
    public ProductoDto actualizarStock(String id, Integer stock) {
        if (stock == null || stock < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El stock debe ser un número entero igual o mayor a 0.");
        }
        Producto p = buscar(id);
        p.setStock(stock);
        return ProductoDto.de(productos.save(p));
    }

    @Transactional
    public void eliminar(String id) {
        productos.delete(buscar(id));
    }

    private Producto buscar(String id) {
        return productos.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El producto ya no existe."));
    }

    private void copiar(Producto p, ProductoRequest r) {
        Categoria categoria = categorias.findById(r.categoriaId())
                .orElseThrow(() -> new ValidacionException(Map.of("categoriaId", "Debes seleccionar una categoría.")));
        p.setNombre(r.nombre().trim());
        p.setCategoria(categoria);
        p.setSubcategoria(r.subcategoria().trim());
        p.setMarca(r.marca().trim());
        p.setUnidad(r.unidad().trim());
        p.setPrecio(r.precio());
        p.setStock(r.stock());
        p.setStockMinimo(r.stockMinimo());
        p.setDescripcion(r.descripcion().trim());
        p.setImagen(r.imagen() == null ? "" : r.imagen().trim());
    }
}
