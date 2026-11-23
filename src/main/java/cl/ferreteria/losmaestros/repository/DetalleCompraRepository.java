package cl.ferreteria.losmaestros.repository;

import cl.ferreteria.losmaestros.model.DetalleCompra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    /**
     * Productos más vendidos: [productoId, productoNombre, cantidadTotal, montoTotal]
     * Ordenado por cantidad descendente.
     */
    @Query("""
            select d.productoId, d.productoNombre, sum(d.cantidad), sum(d.subtotal)
            from DetalleCompra d
            group by d.productoId, d.productoNombre
            order by sum(d.cantidad) desc
            """)
    List<Object[]> topProductos();

    @Query("select coalesce(sum(d.cantidad), 0) from DetalleCompra d")
    long sumaUnidades();
}
