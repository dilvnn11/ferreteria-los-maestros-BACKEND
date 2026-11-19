package cl.ferreteria.losmaestros.service;

import cl.ferreteria.losmaestros.dto.CompraDto;
import cl.ferreteria.losmaestros.dto.CompraRequest;
import cl.ferreteria.losmaestros.dto.CompraRequest.ItemCompra;
import cl.ferreteria.losmaestros.exception.ApiException;
import cl.ferreteria.losmaestros.model.Compra;
import cl.ferreteria.losmaestros.model.DetalleCompra;
import cl.ferreteria.losmaestros.model.Producto;
import cl.ferreteria.losmaestros.model.Usuario;
import cl.ferreteria.losmaestros.repository.CompraRepository;
import cl.ferreteria.losmaestros.repository.ProductoRepository;
import cl.ferreteria.losmaestros.repository.UsuarioRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompraService {

    private final CompraRepository compras;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public CompraService(CompraRepository compras, ProductoRepository productos, UsuarioRepository usuarios) {
        this.compras = compras;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    /**
     * Equivale a "finalizar compra" del carrito: verifica el stock real, lo descuenta y registra la compra.
     * El precio se toma SIEMPRE del producto vigente en la base de datos (el cliente no puede fijarlo).
     * Todo ocurre en una transacción: si un producto no alcanza, no se descuenta nada.
     */
    @Transactional
    public CompraDto crear(CompraRequest req, Usuario actual) {
        if (req == null || req.items() == null || req.items().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito está vacío.");
        }

        // Une líneas repetidas del mismo producto
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (ItemCompra item : req.items()) {
            if (item == null || item.productoId() == null || item.productoId().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Hay un producto sin código en el carrito.");
            }
            if (item.cantidad() == null || item.cantidad() < 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La cantidad debe ser un número entero mayor a 0.");
            }
            cantidades.merge(item.productoId().trim(), item.cantidad(), Integer::sum);
        }

        Map<String, Producto> bloqueados = productos.bloquearPorIds(cantidades.keySet()).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        Compra compra = new Compra();
        compra.setFecha(Instant.now());
        if (actual != null) compra.setUsuario(usuarios.getReferenceById(actual.getId()));

        int total = 0;
        for (Map.Entry<String, Integer> e : cantidades.entrySet()) {
            Producto p = bloqueados.get(e.getKey());
            if (p == null) {
                throw new ApiException(HttpStatus.CONFLICT, "El producto \"" + e.getKey() + "\" ya no existe.");
            }
            if (p.getStock() < e.getValue()) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para \"" + p.getNombre() + "\". Disponible: " + p.getStock());
            }
            p.setStock(p.getStock() - e.getValue());

            DetalleCompra d = new DetalleCompra();
            d.setProductoId(p.getId());
            d.setProductoNombre(p.getNombre());
            d.setPrecioUnitario(p.getPrecio());
            d.setCantidad(e.getValue());
            d.setSubtotal(p.getPrecio() * e.getValue());
            compra.agregarDetalle(d);
            total += d.getSubtotal();
        }
        compra.setTotal(total);

        productos.saveAll(bloqueados.values());
        return CompraDto.de(compras.save(compra));
    }

    @Transactional(readOnly = true)
    public List<CompraDto> misCompras(Usuario actual) {
        return compras.findByUsuarioIdOrderByFechaDesc(actual.getId()).stream().map(CompraDto::de).toList();
    }

    @Transactional(readOnly = true)
    public List<CompraDto> todas() {
        return compras.findAllByOrderByFechaDesc().stream().map(CompraDto::de).toList();
    }
}
